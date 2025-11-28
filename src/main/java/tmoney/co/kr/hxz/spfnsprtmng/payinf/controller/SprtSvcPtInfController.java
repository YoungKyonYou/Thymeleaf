package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SprtSvcPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcDtlRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcTypRspVO;

import javax.validation.Valid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



@Controller
@RequiredArgsConstructor
@RequestMapping("/spfnsprtmng/payinf")
public class SprtSvcPtInfController {

    private final SprtSvcPtInfService sprtSvcPtInfService;
    private final DateUtil dateUtil;


    @Autowired
    private ObjectMapper objectMapper;

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


        // 1. 🎯 orgCd Null/Empty 체크 및 기본값 "0000000" 설정
        //    요청 파라미터 orgCd가 null/empty일 경우, 값을 "0000000"로 덮어씁니다.
        if (orgCd == null || orgCd.trim().isEmpty()) {
            orgCd = "0000000"; // ⚠️ 기본값 "0000000" 적용
        }

        // 2. req 객체에 최종 orgCd 값 설정 (검색 조건 일관성 유지)
        req.setOrgCd(orgCd);



        // 기본 검색기간 세팅 (최근 30일)
        // 서비스 기간
//        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        if (req.getSttDt() == null || req.getSttDt().isEmpty()) {
            req.setSttDt(dateUtil.thirtyDaysAgo());
        }
        if (req.getEndDt() == null || req.getEndDt().isEmpty()) {
            req.setEndDt(dateUtil.today());
        }


        // 페이징 리스트 조회
        PageDataVO<SprtSvcDtlRspVO> contents = sprtSvcPtInfService.readSprtSvcPtInfList(req,  orgCd);

        try {
            // contents 전체 JSON 변환 및 이쁘게 출력
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(contents);
            System.out.println("===== SprtSvcPtInf contents =====");
            System.out.println(json);
        } catch (Exception e) {
            System.out.println("JSON 변환 실패: " + e.getMessage());
        }


        // Model에 조회 결과 및 요청 조건 담기
        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd); // 요청 파라미터로 받은 orgCd를 Model에 추가


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
        SprtSvcDtlRspVO contents = sprtSvcPtInfService.readSprtSvcPtInf(tpwSvcId, orgCd);

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
        return sprtSvcPtInfService.readSprtSvcTypList(tpwSvcId);
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
        SprtSvcTypRspVO contents = sprtSvcPtInfService.readSprtSvcTyp(tpwSvcTypId, tpwSvcTypSno, tpwSvcId);
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


        return "/hxz/spfnsprtmng/payinf/sprtSvcTypForm";
    }

}
