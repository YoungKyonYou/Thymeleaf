package tmoney.co.kr.hxz.penstlmng.aplinf.controller;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.springframework.http.ResponseEntity;
import tmoney.co.kr.export.ExportColumn;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.penstlmng.aplinf.service.PenAplPtInfService;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfRspVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfReqVO;

import tmoney.co.kr.hxz.penstlmng.aplinf.service.export.PenApiPtInfExportService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.IOException;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@Controller
@RequiredArgsConstructor
@RequestMapping("/penstlmng/aplinf")
public class PenAplPtInfController {
    private final PenAplPtInfService penAplPtInfService;
    private final PenApiPtInfExportService exportService;
    private final DateUtil dateUtil;

    // @PreAuthorize("hasPermission ('지급금신청내역조회', 'READ')")
    @GetMapping(value = "/penAplPtInf.do")
    public String readPenAplPtInfPaging(
            @ModelAttribute @Valid PenAplPtInfReqVO req,
            Model model
    )
    {
        // 기본 조회일자 설정 (30일 전 ~ 오늘)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        final String orgCd = "0000000";
        PageDataVO<PenAplPtInfRspVO> contents = penAplPtInfService.readPenAplPtInfPaging(req, orgCd); // TODO: 추후 로그인 연동(orgCd)

        model.addAttribute("pageData", contents);

        // 월별/일별 통계 데이터
        model.addAttribute("monthly", penAplPtInfService.readPenAplCntByMonth(req));
        model.addAttribute("daily", penAplPtInfService.readPenAplCntByDay(req));

        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd);

        return "/hxz/penstlmng/aplinf/penAplPtInf";
    }


    /**
     * 신청 승인 처리 
     * 01 : 요청
     * 02 : 반려
     * 03 : 승인
     */
    @PostMapping("/approve")
    @ResponseBody
    public ResponseEntity<Void> approvePenApl(@RequestBody PenAplPtInfRspVO form) {
        form.setAprvStaCd("03");
        penAplPtInfService.updateApprove(form);
        return ResponseEntity.ok().build();
    }

    
    // @PreAuthorize("hasPermission ('지급금신청내역조회', 'READ')")
    @GetMapping(value = "/exportPenAplPtInf")
    public void exportPenAplPtInfPaging(
            @ModelAttribute @Valid PenAplPtInfReqVO req,
            String orgCd,
            HttpServletResponse response
    ) throws IOException {

        // 기본 조회일자 설정 (30일 전 ~ 오늘)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());


        String sheetName = exportService.name();
        String fileName = exportService.name() + "_" + LocalDate.now() + ".xlsx";

        // VO → Map 변환
        Map<String, String> params = req.toMap();
        params.put("orgCd", orgCd);

        // 엑셀 객체 생성
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);

        AtomicInteger rowIdx = new AtomicInteger(0);

        // ===== 1. 헤더 작성 =====
        Row headerRow = sheet.createRow(rowIdx.getAndIncrement());

        List<ExportColumn<PenAplPtInfRspVO>> columns = exportService.columns(); // 컬럼명 리스트라고 가정
        int cellIdx = 0;

        for (ExportColumn<PenAplPtInfRspVO> col : columns)
        {
            Cell cell = headerRow.createCell(cellIdx++);
            cell.setCellValue(col.getHeader());
        }

        // ===== 2. 데이터 스트림으로 Row 채우기 =====
        try (Stream<PenAplPtInfRspVO> stream = exportService.stream(params))
        {
            stream.forEach(vo -> {

                Row row = sheet.createRow(rowIdx.getAndIncrement());
                int c = 0;

                // 정산일자
                row.createCell(c++).setCellValue(vo.getStlmDt());

                // 회원ID
                row.createCell(c++).setCellValue(vo.getMbrsId());

                // 신청일자
                row.createCell(c++).setCellValue(vo.getAplDt());

                // 카드번호
                row.createCell(c++).setCellValue(vo.getCardNo());

                // 은행코드
                row.createCell(c++).setCellValue(vo.getBnkCd());

                // 계좌번호
                row.createCell(c++).setCellValue(vo.getAcntNo());

                // 예금주명
                row.createCell(c++).setCellValue(vo.getOoaNm());

                // 승인자ID
                row.createCell(c++).setCellValue(vo.getAproId());

                // 승인일시
                row.createCell(c++).setCellValue(vo.getAprvDtm());

                // 승인상태코드
                row.createCell(c++).setCellValue(vo.getAprvStaCd());

                // 지원대상유형
                row.createCell(c++).setCellValue(vo.getTpwMbrsTypCd());

                // 첨부파일관리번호
                if (vo.getAtflMngNo() != null)
                {
                    row.createCell(c++).setCellValue(vo.getAtflMngNo());
                }
                else
                {
                    row.createCell(c++).setCellValue("");
                }

                // 신청진행상태
                row.createCell(c++).setCellValue(vo.getTpwAplPrgsStaCd());

                // 서비스명
                row.createCell(c++).setCellValue(vo.getTpwSvcNm());

                // 서비스유형명 (지원유형)
                row.createCell(c++).setCellValue(vo.getTpwSvcTypNm());

                // 서비스유형번호
                row.createCell(c++).setCellValue((RichTextString) vo.getTpwSvcTypSno());

                // 신청년월(YYYY-MM)
                row.createCell(c++).setCellValue(vo.getAplYm());

                // 월별 신청건수
                if (vo.getAplCnt() != null)
                {
                    row.createCell(c++).setCellValue(vo.getAplCnt());
                }
                else
                {
                    row.createCell(c++).setCellValue("");
                }

                // 신청일자(DD)
                row.createCell(c++).setCellValue(vo.getAplDay());

                // 일별 신청건수
                if (vo.getAplCntDay() != null)
                {
                    row.createCell(c++).setCellValue(vo.getAplCntDay());
                }
                else
                {
                    row.createCell(c++).setCellValue("");
                }

            });
        }


        // ===== 3. 다운로드 헤더 설정 =====
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // ===== 4. 파일 출력 =====
        wb.write(response.getOutputStream());
        wb.close();

    }
}
