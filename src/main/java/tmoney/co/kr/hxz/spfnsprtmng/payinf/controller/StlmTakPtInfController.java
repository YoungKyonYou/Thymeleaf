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

@Controller
@RequiredArgsConstructor
@RequestMapping("/hxz/spfnsprtmng/payinf")
public class StlmTakPtInfController {

    private final StlmTakPtInfService stlmTakPtInfService;
    private final DateUtil dateUtil;

    /** -----------------------------------------
     * 1. 정산작업내역 리스트 조회 (검색용 ReqVO)
     * - tbhxzd218 / tbhxzd219
     * - 검색 조건: 날짜, 페이지, size 등
     * - 페이징 처리 후 Model에 전달
     * ---------------------------------------- */
    @GetMapping("/stlmTakPtInf.do")
    public String readStlmTakPtInf(
            @ModelAttribute @Valid StlmTakPtInfReqVO req,
            Model model
    ) {
        // 기본 검색기간 세팅 (최근 30일)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        // 페이징 리스트 조회
        PageDataVO<StlmTakPtInfRspVO> contents = stlmTakPtInfService.readStlmTakPtPaging(req);

        // Model에 조회 결과 및 요청 조건 담기
        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);

        return "/hxz/spfnsprtmng/payinf/stlmTakPtInf";
    }

    /** -----------------------------------------
     * 2. 신규 등록 폼 이동 (RspVO)
     * - 신규 버튼 클릭 시 호출
     * - 빈 VO 객체를 Model에 전달
     * ---------------------------------------- */
    @GetMapping(path = "/new", produces = MediaType.TEXT_HTML_VALUE)
    public String newStlmTakPtInfForm(Model model) {
        model.addAttribute("stlmTakPtInf", new StlmTakPtInfRspVO());
        return "/hxz/spfnsprtmng/payinf/stlmTakPtInfForm";
    }

    /** -----------------------------------------
     * 3. 상세보기 폼 이동 (서비스ID + 서비스번호 기준, RspVO)
     * - 리스트 내 상세보기 클릭 시 호출
     * - exeDiv(PERD/SIM) 기준으로 테이블 분기 후 단건 조회
     * ---------------------------------------- */
    @GetMapping(path = "/edit")
    public String editStlmTakPtInfForm(
            @RequestParam("tpwSvcTypId") String tpwSvcTypId,
            @RequestParam("tpwSvcTypSno") String tpwSvcTypSno,
            @RequestParam(value = "exeDiv", defaultValue = "PERD") String exeDiv,
            Model model
    ) {
        // 서비스ID + 서비스번호 기준 단건 조회
        StlmTakPtInfRspVO rsp = stlmTakPtInfService.findStlmTakPtInfByService(tpwSvcTypId, tpwSvcTypSno, exeDiv);

        // Model에 단건 조회 결과 전달
        model.addAttribute("stlmTakPtInf", rsp);
        return "/hxz/spfnsprtmng/payinf/stlmTakPtInfForm";
    }

    /** -----------------------------------------
     * 4. 등록 API (RspVO)
     * - form 데이터 JSON으로 전달
     * - exeDiv 기준 PERD/SIM 테이블 분기
     * ---------------------------------------- */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> saveStlmTakPtInf(
            @RequestBody @Valid StlmTakPtInfRspVO form
    ) {
        stlmTakPtInfService.saveStlmTakPtInf(form);
        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     * 5. 수정 API (RspVO)
     * - 서비스ID + 서비스번호 기준으로 단건 수정
     * - exeDiv 기준 PERD/SIM 테이블 분기
     * ---------------------------------------- */
    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> updateStlmTakPtInf(
            @RequestBody @Valid StlmTakPtInfRspVO form
    ) {
        stlmTakPtInfService.updateStlmTakPtInfByService(form);
        return ResponseEntity.ok().build();
    }
}
