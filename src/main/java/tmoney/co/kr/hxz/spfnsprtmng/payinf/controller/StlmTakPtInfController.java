package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.StlmTakPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfRspVO;
import tmoney.co.kr.hxz.common.util.DateUtil;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/spfnsprtmng/payinf")
public class StlmTakPtInfController {

    private final StlmTakPtInfService stlmTakPtInfService;
    private final DateUtil dateUtil;

    /** -----------------------------------------
     * 1. 정산작업내역 리스트 조회 (검색용 ReqVO)
     * ---------------------------------------- */
    @GetMapping("/stlmTakPtInf.do")
    public String readStlmTakPtInf(
            @ModelAttribute @Valid StlmTakPtInfReqVO req,
            String orgCd,
            String exeDiv,
            Model model
    ) {
        // 요청 파라미터 orgCd가 null/empty일 경우 기본값 적용
        if (orgCd == null || orgCd.trim().isEmpty()) {
            orgCd = "0000000";
        }

        // req 객체에 최종 orgCd 값 설정 (검색 조건 일관성 유지)
        req.setOrgCd(orgCd);

        // 기본 검색기간 세팅 (최근 30일)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        // 페이징 리스트 조회
        PageDataVO<StlmTakPtInfRspVO> contents = stlmTakPtInfService.readStlmTakPtPaging(req,  orgCd, exeDiv);

        // 검색 조건 유지를 위해 req에 exeDiv 세팅
        req.setExeDiv(exeDiv);

        // Model에 조회 결과 및 요청 조건 담기
        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);

        // 디버깅 출력 (간략하게)
        System.out.println("====== [DEBUG] readStlmTakPtInf ======");
        if (contents != null) {
            System.out.println("Total Count: " + contents.getTotal());
        }
        System.out.println("=======================================");

        return "/hxz/spfnsprtmng/payinf/stlmTakPtInf";
    }

    // ==============================================================
    // 2. 정기(PERD) - 등록/수정 전용 메서드 그룹
    // ==============================================================

    /** -----------------------------------------
     * 2-1. 정기(PERD) 신규 등록 폼 이동
     * ---------------------------------------- */
    @GetMapping(path = "/perd/new", produces = MediaType.TEXT_HTML_VALUE)
    public String newPerdStlmTakPtForm(Model model) {
        StlmTakPtInfRspVO newVO = new StlmTakPtInfRspVO();
        newVO.setExeDiv("PERD");
        model.addAttribute("pageData", newVO);
        return "/hxz/spfnsprtmng/payinf/perdStlmTakPtForm.html";
    }

    /** -----------------------------------------
     * 2-2. 정기(PERD) 상세보기/수정 폼 이동
     * ---------------------------------------- */
    @GetMapping(path = "/perd/edit")
    public String editPerdStlmTakPtForm(
            @RequestParam("targetTpwSvcTypId") String tpwSvcTypId,   // 조회용 ID (target 접두어 사용)
            @RequestParam("targetTpwSvcTypSno") BigDecimal tpwSvcTypSno,
            @RequestParam(value = "exeDiv", defaultValue = "PERD") String exeDiv,
            @RequestParam("targetTpwSvcId") String tpwSvcId,
            @RequestParam("targetStlmDt") String stlmDt,
            @ModelAttribute("searchReq") StlmTakPtInfReqVO searchReq, // 목록 복귀용 검색 조건 (원래 이름 사용)
            Model model
    ) {
        // 서비스 호출 (조회용 ID 사용)
        StlmTakPtInfRspVO contents = stlmTakPtInfService.readPerdTakPtInf(tpwSvcTypId, tpwSvcTypSno, exeDiv, tpwSvcId, stlmDt);

        model.addAttribute("exeDiv", exeDiv);
        model.addAttribute("pageData", contents);

        // 로그
        System.out.println("====== [DEBUG] editPerdStlmTakPtForm ======");
        if (contents != null) {
            System.out.println("Selected Service ID: " + contents.getTpwSvcId());
        }
        System.out.println("Search Condition: " + searchReq); // 검색 조건 확인
        System.out.println("===========================================");

        return "/hxz/spfnsprtmng/payinf/perdStlmTakPtForm";
    }

    /** -----------------------------------------
     * 2-3. 정기(PERD) 등록 API
     * ---------------------------------------- */
    @PostMapping( path = "/perd/add")
    @ResponseBody
    public ResponseEntity<Void> savePerdStlmTakPtInf(
            @RequestBody @Valid StlmTakPtInfRspVO form
    ) {
        form.setExeDiv("PERD");
        stlmTakPtInfService.saveStlmTakPtInf(form);
        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     * 2-4. 정기(PERD) 수정 API
     * ---------------------------------------- */
    @PutMapping(path = "/perd/update")
    @ResponseBody
    public ResponseEntity<Void> updatePerdStlmTakPtInf(
            @RequestBody @Valid StlmTakPtInfRspVO form
    ) {
        form.setExeDiv("PERD");
        stlmTakPtInfService.updateStlmTakPtInfByService(form);
        return ResponseEntity.ok().build();
    }


    // ==============================================================
    // 3. 시뮬레이션(SIM) - 등록/수정 전용 메서드 그룹
    // ==============================================================

    /** -----------------------------------------
     * 3-1. 시뮬레이션(SIM) 신규 등록 폼 이동
     * ---------------------------------------- */
    @GetMapping(path = "/sim/new", produces = MediaType.TEXT_HTML_VALUE)
    public String newSimStlmTakPtForm(Model model) {
        StlmTakPtInfRspVO newVO = new StlmTakPtInfRspVO();
        newVO.setExeDiv("SIM");
        model.addAttribute("pageData", newVO);
        return "/hxz/spfnsprtmng/payinf/simStlmTakPtForm";
    }

    /** -----------------------------------------
     * 3-2. 시뮬레이션(SIM) 상세보기/수정 폼 이동
     * ---------------------------------------- */
    @GetMapping(path = "/sim/edit")
    public String editSimStlmTakPtForm(
            @RequestParam("targetTpwSvcTypId") String tpwSvcTypId,
            @RequestParam("targetTpwSvcTypSno") BigDecimal tpwSvcTypSno,
            @RequestParam(value = "exeDiv", defaultValue = "SIM") String exeDiv,
            @RequestParam("targetTpwSvcId") String tpwSvcId,
            @RequestParam("targetAplDt") String aplDt,               // SIM은 aplDt 사용
            @ModelAttribute("searchReq") StlmTakPtInfReqVO searchReq,
            Model model
    ) {
        StlmTakPtInfRspVO contents = stlmTakPtInfService.readSimTakPtInf(tpwSvcTypId, tpwSvcTypSno, exeDiv, tpwSvcId, aplDt);

        model.addAttribute("exeDiv", exeDiv);
        model.addAttribute("pageData", contents);

        System.out.println("====== [DEBUG] editSimStlmTakPtForm ======");
        if (contents != null) {
            System.out.println("Selected Service ID: " + contents.getTpwSvcId());
        }
        System.out.println("Search Condition: " + searchReq);
        System.out.println("==========================================");

        return "/hxz/spfnsprtmng/payinf/simStlmTakPtForm";
    }

    /** -----------------------------------------
     * 3-3. 시뮬레이션(SIM) 등록 API
     * ---------------------------------------- */
    @PostMapping( path = "/sim/add")
    @ResponseBody
    public ResponseEntity<Void> saveSimStlmTakPtInf(
            @RequestBody @Valid StlmTakPtInfRspVO form
    ) {
        form.setExeDiv("SIM");
        stlmTakPtInfService.saveStlmTakPtInf(form);
        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     * 3-4. 시뮬레이션(SIM) 수정 API
     * ---------------------------------------- */
    @PutMapping(path = "/sim/update")
    @ResponseBody
    public ResponseEntity<Void> updateSimStlmTakPtInf(
            @RequestBody @Valid StlmTakPtInfRspVO form
    ) {
        form.setExeDiv("SIM");
        stlmTakPtInfService.updateStlmTakPtInfByService(form);
        return ResponseEntity.ok().build();
    }
}