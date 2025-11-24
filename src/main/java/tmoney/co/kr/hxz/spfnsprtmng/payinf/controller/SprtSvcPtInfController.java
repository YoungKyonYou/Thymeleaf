package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SprtSvcPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export.SprtSvcPtInfExportService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcDtlRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcTypRspVO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;



@Controller
@RequiredArgsConstructor
@RequestMapping("/spfnsprtmng/payinf")
public class SprtSvcPtInfController {

    private final SprtSvcPtInfService sprtSvcPtInfService;
    private final DateUtil dateUtil;
    private final SprtSvcPtInfExportService exportService;


    /** -----------------------------------------
     * 1. 지원서비스내역조회
     * - 
     * - 검색 조건: 날짜, 페이지, size 등
     * - 페이징 처리 후 Model에 전달
     * ---------------------------------------- */
    @GetMapping("/sprtSvcPtInf.do")
    public String readSprtSvcPtInf(
            @ModelAttribute @Valid SprtSvcPtInfReqVO req,
            String orgCd,
            Model model
    ) {
        // 기본 검색기간 세팅 (최근 30일)
        // 서비스 기간
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        // 페이징 리스트 조회
        PageDataVO<SprtSvcDtlRspVO> contents = sprtSvcPtInfService.readSprtSvcPtInfList(req,  orgCd);

        // Model에 조회 결과 및 요청 조건 담기
        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);


        return "/hxz/spfnsprtmng/payinf/sprtSvcPtInf";
    }

    /** -----------------------------------------
     * 2. 서비스 신규 등록 폼 이동 (RspVO)
     * - 신규 버튼 클릭 시 호출
     * - 빈 VO 객체를 Model에 전달
     * ---------------------------------------- */
    @GetMapping(path = "/newSprtSvcPtInfForm.do")
    public String newSprtSvcPtInfForm(Model model) {

        // 신규 등록용 VO 객체 초기화
        SprtSvcDtlRspVO contents = new SprtSvcDtlRspVO();
        contents.setSvcTypList(new ArrayList<>());
        contents.setUseYn("Y");  // 기본값 세팅 가능

        // 하위 서비스유형 리스트 초기화
        contents.setSvcTypList(null);

        model.addAttribute("detail", contents);
        return "/hxz/spfnsprtmng/payinf/sprtSvcPtInfForm";
    }

    /**
     * -----------------------------------------
     * 3. 상세보기 폼 이동 (서비스ID + 서비스번호 기준, RspVO)
     * - 리스트 내 상세보기 클릭 시 호출
     * ----------------------------------------
     */
    @GetMapping(path = "/sprtSvcInfDetail.do")
    public String detailSprtSvcPtInfForm(
            @RequestParam("tpwSvcId") String tpwSvcId,
            @RequestParam("orgCd") String orgCd,
            Model model
    ) {

        // ✅ 타입 수정됨 (DtlRspVO로 받기)
        SprtSvcDtlRspVO contents = sprtSvcPtInfService.findSprtSvcPtInf(tpwSvcId, orgCd);

        model.addAttribute("detail", contents);

        return "/hxz/spfnsprtmng/payinf/sprtSvcPtInfForm";
    }

    /** -----------------------------------------
     * 4. 등록 API (RspVO)
     * - form 데이터 JSON으로 전달
     * ---------------------------------------- */
    @PostMapping( path = "/Sprtsvcptinfadd.do")
    @ResponseBody
    public ResponseEntity<Void> saveSprtSvcPtInf(
        @RequestBody @Valid SprtSvcPtInfRspVO form
    ) {
        sprtSvcPtInfService.saveSprtSvcPtInf(form);
        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     * 5. 수정 API (RspVO)
     * - 서비스ID + 서비스번호 기준으로 단건 수정
     * ---------------------------------------- */
   @PutMapping(path = "/Sprtsvcptinfupdate.do")
   @ResponseBody
   public ResponseEntity<Void> updateSprtSvcPtInf(
        @RequestBody @Valid SprtSvcPtInfRspVO form
   ) {
       sprtSvcPtInfService.updateSprtSvcPtInfByService(form);
       return ResponseEntity.ok().build();
   }


    /** -----------------------------------------
     * 1. 지원유형관리 리스트 (sprtSvcPtInfForm에서 포함)
     * ---------------------------------------- */
    @GetMapping("/list")
    @ResponseBody
    public List<SprtSvcTypRspVO> getSvcTypList(
            @RequestParam("tpwSvcId") String tpwSvcId
    ) {
        return sprtSvcPtInfService.findSprtSvcTypList(tpwSvcId);
    }


    /** -----------------------------------------
     * 2. 지원유형관리 상세보기 (단건)
     * ---------------------------------------- */
    @GetMapping("/SprtSvcTypDetail.do")
    public String editSvcTypForm(
            @RequestParam("tpwSvcTypId") String tpwSvcTypId,
            @RequestParam("tpwSvcTypSno") BigDecimal tpwSvcTypSno,
            @RequestParam("tpwSvcId") String tpwSvcId,
            Model model
    ) {
        SprtSvcTypRspVO contents = sprtSvcPtInfService.findSprtSvcTyp(tpwSvcTypId, tpwSvcTypSno, tpwSvcId);
        model.addAttribute("typDetail", contents);
        return "/hxz/spfnsprtmng/payinf/sprtSvcTypForm";
    }


    /** -----------------------------------------
     * 3. 신규 등록
     * ---------------------------------------- */
    @PostMapping("/SprtsvcTypadd")
    @ResponseBody
    public ResponseEntity<Void> saveSvcTyp(
            @RequestBody SprtSvcTypRspVO form
    ) {
        sprtSvcPtInfService.saveSprtSvcTyp(form);
        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     * 4. 수정
     * ---------------------------------------- */
    @PutMapping("/updateSvcTyp")
    @ResponseBody
    public ResponseEntity<Void> updateSvcTyp(
            @RequestBody SprtSvcTypRspVO form
    ) {
        // 1. 업데이트 처리(변경된내용)
        // 사용여부 n으로 업데이트
        sprtSvcPtInfService.updateUseYnN(form);

        // 2. sno를 +1 해서 새롭게 insert(바뀐내용 가지고 insert)
        sprtSvcPtInfService.updateSprtSvcTyp(form);




        return ResponseEntity.ok().build();
    }



    /** -----------------------------------------
     * 5. 지원유형관리 신규 등록 폼 이동 (RspVO)
     * - 지원서비스 상세보기에서 하위 유형 신규 등록 버튼 클릭 시 호출
     * - 빈 VO 객체를 Model에 전달
     * ---------------------------------------- */
    @GetMapping(path = "/newSprtSvcTypForm.do")
    public String newSprtSvcTypForm(
            @RequestParam("tpwSvcId") String tpwSvcId, // 상위 서비스ID
            Model model
    ) {
        // 1. 신규 등록용 VO 객체 초기화
        SprtSvcTypRspVO typDetail = new SprtSvcTypRspVO();

        // 상위 서비스ID를 VO에 세팅
        typDetail.setTpwSvcId(tpwSvcId); // 상위 서비스 ID 세팅
        typDetail.setUseYn("Y"); // 기본값 세팅

        model.addAttribute("typDetail", typDetail);


        typDetail.setTpwSvcId(tpwSvcId);
        typDetail.setUseYn("Y"); // 기본값 세팅 가능


        System.out.println("==================================================");


        return "/hxz/spfnsprtmng/payinf/sprtSvcTypForm";
    }



    @GetMapping(path = "/exportSprtSvcPtInf")
    public void exportSprtSvcPtInf(
            @ModelAttribute @Valid SprtSvcPtInfReqVO req,
            String orgCd,
            HttpServletResponse response
    ) throws IOException {

        // 기본 검색기간 세팅
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        // SprtSvcPtInfExportService export = new SprtSvcPtInfExportService();

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

        List<ExportColumn<SprtSvcDtlRspVO>> columns = exportService.columns(); // 컬럼명 리스트라고 가정
        int cellIdx = 0;

        for (ExportColumn<SprtSvcDtlRspVO> col : columns)
        {
            Cell cell = headerRow.createCell(cellIdx++);
            cell.setCellValue(col.getHeader());
        }

        // ===== 2. 데이터 스트림으로 Row 채우기 =====
        try (Stream<SprtSvcDtlRspVO> stream = exportService.stream(params))
        {
            stream.forEach(vo -> {

                Row row = sheet.createRow(rowIdx.getAndIncrement());
                int c = 0;

                // 기관 코드
                row.createCell(c++).setCellValue(vo.getTpwOrgNm());
                // 서비스 내용
                row.createCell(c++).setCellValue(vo.getTpwSvcCtt());
                // 서비스 시작일자
                row.createCell(c++).setCellValue(vo.getTpwSvcSttDt());
                // 서비스 종료일자
                row.createCell(c++).setCellValue(vo.getTpwSvcEndDt());
                // 서비스명
                row.createCell(c++).setCellValue(vo.getTpwSvcNm());

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
