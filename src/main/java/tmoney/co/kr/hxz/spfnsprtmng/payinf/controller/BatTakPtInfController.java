package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.BatTakPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export.BatTakPtInfExportService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfRspVO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/spfnsprtmng/payinf")
// resources/templates/hxz/spfnsprtmng/payinf/BatTakPtInf.html
@RequiredArgsConstructor
public class BatTakPtInfController {

    /**
     * 배치작업 내역조회
     *  - tbhxzm201 : HXZ_교통복지서비스관리
     *  - tbhxzm202 : HXZ_교통복지서비스유형관리
     */
    private final BatTakPtInfService batTakPtInfService;
    private final DateUtil dateUtil;
    private final BatTakPtInfExportService exportService;

    // @PreAuthorize("hasPermission ('배치작업내역조회', 'READ')")
    @GetMapping(value = "/batTakPtInf.do")
    public String readBatTakPtPaging(
            @ModelAttribute @Valid BatTakPtInfReqVO req,
            Model model
    ) {

        // 기본 조회일자 설정 (30일 전 ~ 오늘)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        final String orgCd = "0000000";
        PageDataVO<BatTakPtInfRspVO> contents = batTakPtInfService.readBatTakPtPaging(req, orgCd); // TODO: 추후 로그인 연동(orgCd)

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd);

        return "/hxz/spfnsprtmng/payinf/batTakPtInf";
    }



    @GetMapping(path = "/exportBatTakPtInf")
    public void exportSprtSvcPtInf(
            @ModelAttribute @Valid BatTakPtInfReqVO req,
            String orgCd,
            HttpServletResponse response
    ) throws IOException {

        // 기본 검색기간 세팅
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        String sheetName = exportService.name();
        String fileName = exportService.name() + "_" + LocalDate.now() + ".xlsx";

        // VO → Map 변환
        Map<String, String> params = req.toMap();
        params.put("orgCd", orgCd);

        // 엑셀 객체 생성
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);

        AtomicInteger rowIdx = new AtomicInteger(1);

        // ===== 1. 헤더 작성 =====
        Row headerRow = sheet.createRow(rowIdx.getAndIncrement());

        List<ExportColumn<BatTakPtInfRspVO>> columns = exportService.columns(); // 컬럼명 리스트라고 가정
        int cellIdx = 0;

        for (ExportColumn<BatTakPtInfRspVO> col : columns)
        {
            Cell cell = headerRow.createCell(cellIdx++);
            cell.setCellValue(col.getHeader());
        }

        // ===== 2. 데이터 스트림으로 Row 채우기 =====
        try (Stream<BatTakPtInfRspVO> stream = exportService.stream(params))
        {
            stream.forEach(vo -> {

                // VO 내용 찍기 (디버그용)
                System.out.println("VO = " + vo);

                Row row = sheet.createRow(rowIdx.getAndIncrement());
                int c = 0;


                // 1. 작업일자 (batTakDt)
                row.createCell(c++).setCellValue(vo.getBatTakDt() != null ? vo.getBatTakDt() : "");

                // 2. 작업ID (batTakId)
                row.createCell(c++).setCellValue(vo.getBatTakId() != null ? vo.getBatTakId() : "");

                // 3. 배치유형코드 (tpwBatTypCd)
                row.createCell(c++).setCellValue(vo.getTpwBatTypCd() != null ? vo.getTpwBatTypCd() : "");

                // 4. 배치명 (batTakNm)
                row.createCell(c++).setCellValue(vo.getBatTakNm() != null ? vo.getBatTakNm() : "");

                // 5. 배치시작일시 (batTakSttDtm) - yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss
                String sttDtm = vo.getBatTakSttDtm();
                if (sttDtm != null && sttDtm.length() == 14)
                {
                    sttDtm = sttDtm.substring(0, 4) + "-" +
                            sttDtm.substring(4, 6) + "-" +
                            sttDtm.substring(6, 8) + " " +
                            sttDtm.substring(8, 10) + ":" +
                            sttDtm.substring(10, 12) + ":" +
                            sttDtm.substring(12, 14);
                }
                row.createCell(c++).setCellValue(sttDtm != null ? sttDtm : "");

                // 6. 배치종료일시 (batTakEndDtm) - yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss
                String endDtm = vo.getBatTakEndDtm();
                if (endDtm != null && endDtm.length() == 14)
                {
                    endDtm = endDtm.substring(0, 4) + "-" +
                            endDtm.substring(4, 6) + "-" +
                            endDtm.substring(6, 8) + " " +
                            endDtm.substring(8, 10) + ":" +
                            endDtm.substring(10, 12) + ":" +
                            endDtm.substring(12, 14);
                }
                row.createCell(c++).setCellValue(endDtm != null ? endDtm : "");

                // 7. 처리건수 (prcgNcnt)
                Integer prcgNcnt = vo.getPrcgNcnt();
                row.createCell(c++).setCellValue(prcgNcnt != null ? prcgNcnt : 0);

                // 8. 배치처리상태코드 (batPrcgStaCd)
                row.createCell(c++).setCellValue(vo.getBatPrcgStaCd() != null ? vo.getBatPrcgStaCd() : "");

                // 9. 등록자ID (rgsrId)
                row.createCell(c++).setCellValue(vo.getRgsrId() != null ? vo.getRgsrId() : "");

                // 10. 등록일시 (rgtDtm) - yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss
                String rgtDtm = vo.getRgtDtm();
                if (rgtDtm != null && rgtDtm.length() == 14)
                {
                    rgtDtm = rgtDtm.substring(0, 4) + "-" +
                            rgtDtm.substring(4, 6) + "-" +
                            rgtDtm.substring(6, 8) + " " +
                            rgtDtm.substring(8, 10) + ":" +
                            rgtDtm.substring(10, 12) + ":" +
                            rgtDtm.substring(12, 14);
                }
                row.createCell(c++).setCellValue(rgtDtm != null ? rgtDtm : "");

                // 11. 수정자ID (updrId)
                row.createCell(c++).setCellValue(vo.getUpdrId() != null ? vo.getUpdrId() : "");

                // 12. 수정일시 (updDtm) - yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss
                String updDtm = vo.getUpdDtm();
                if (updDtm != null && updDtm.length() == 14)
                {
                    updDtm = updDtm.substring(0, 4) + "-" +
                            updDtm.substring(4, 6) + "-" +
                            updDtm.substring(6, 8) + " " +
                            updDtm.substring(8, 10) + ":" +
                            updDtm.substring(10, 12) + ":" +
                            updDtm.substring(12, 14);
                }
                row.createCell(c++).setCellValue(updDtm != null ? updDtm : "");

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
