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
     * - tbhxzd218 / tbhxzd219
     * - 검색 조건: 날짜, 페이지, size 등
     * - 페이징 처리 후 Model에 전달
     * ---------------------------------------- */
    @GetMapping("/stlmTakPtInf.do")
    public String readStlmTakPtInf(
            @ModelAttribute @Valid StlmTakPtInfReqVO req,
//            @Mngr MngrVO mngrVO,
            String orgCd,
            String exeDiv,
            Model model
    ) {
        // 기본 검색기간 세팅 (최근 30일)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        // 페이징 리스트 조회
        PageDataVO<StlmTakPtInfRspVO> contents = stlmTakPtInfService.readStlmTakPtPaging(req,  orgCd, exeDiv);

        // Model에 조회 결과 및 요청 조건 담기
        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);


        System.out.println("====== [DEBUG] 1. Request VO (req) 상태 확인 ======");
        System.out.println("req 객체 전체: " + req);
        System.out.println("검색 시작일(sttDt): " + req.getSttDt());
        System.out.println("검색 종료일(endDt): " + req.getEndDt());
        System.out.println("검색 유형(searchType): " + req.getSearchType());
        System.out.println("실행 구분(exeDiv): " + req.getExeDiv());
        System.out.println("페이지/사이즈: " + req.getPage() + " / " + req.getSize());
        System.out.println("=====================================================");


        System.out.println("====== [DEBUG] 2. Response VO (contents) 상태 확인 ======");
        if (contents != null) {
            System.out.println("총 건수 (Total): " + contents.getTotal());
            System.out.println("리스트 크기 (Content Size): " + contents.getContent().size());
            if (!contents.getContent().isEmpty()) {
                System.out.println("첫 번째 항목 (First Item): " + contents.getContent().get(0));
            }
        } else {
            System.out.println("Contents 객체가 null입니다.");
        }
        System.out.println("=====================================================");


        return "/hxz/spfnsprtmng/payinf/stlmTakPtInf";
    }

    /** -----------------------------------------
     * 2. 신규 등록 폼 이동 (RspVO)
     * - 신규 버튼 클릭 시 호출
     * - 빈 VO 객체를 Model에 전달
     * ---------------------------------------- */
    @GetMapping(path = "/new", produces = MediaType.TEXT_HTML_VALUE)
    public String newStlmTakPtInfForm(Model model) {

        // StlmTakPtInfRspVO pageData;

        // if (id != null) {
        //     pageData = stlmTakPtInfService.findTakPtInfById(id);
        // }

        // // 신규 등록 시 null이면 기본 객체 생성
        // if (pageData == null) {
        //     pageData = new StlmTakPtInfRspVO();
        //     pageData.setExeDiv("");  // 기본값 지정 가능
        //     pageData.setAplSttDt(LocalDate.now().toString()); // 필요시
        //     pageData.setAplEndDt(LocalDate.now().toString());
        //     pageData.setAplDt(LocalDate.now().toString());
        // }

        model.addAttribute("pageData", new StlmTakPtInfRspVO());
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
            @RequestParam("tpwSvcTypSno") BigDecimal tpwSvcTypSno,
            @RequestParam(value = "exeDiv", defaultValue = "PERD") String exeDiv,
            @RequestParam("tpwSvcId") String tpwSvcId,
            Model model
    ) {

        StlmTakPtInfRspVO contents = stlmTakPtInfService.findTakPtInf(tpwSvcTypId, tpwSvcTypSno, exeDiv, tpwSvcId);
        
        // 조회 결과를 모델에 추가
        model.addAttribute("exeDiv", exeDiv); // 화면단에서 구분용
        model.addAttribute("pageData", contents);

        // 콘솔 출력 (System.out)
        System.out.println("==================================================");
        System.out.println("exeDiv: " + exeDiv);
        
        if (contents == null)
        {
            System.out.println("contents: null");
        }
        else
        {
            System.out.println("contents: " + contents.toString());
            
            // 만약 VO에 toString()이 제대로 정의 안돼있다면 아래로 필드별 출력
            try
            {
                // Jackson 사용 (JSON처럼 보기 좋게)
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                System.out.println("contents JSON: " + mapper.writeValueAsString(contents));
            }
            catch (Exception e)
            {
                System.out.println("ObjectMapper 변환 실패: " + e.getMessage());
            }
        }
        System.out.println("==================================================");



        return "/hxz/spfnsprtmng/payinf/stlmTakPtInfForm";
    }

    /** -----------------------------------------
     * 4. 등록 API (RspVO)
     * - form 데이터 JSON으로 전달
     * - exeDiv 기준 PERD/SIM 테이블 분기
     * ---------------------------------------- */
    @PostMapping( path = "/add")
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
    @PutMapping(path = "/update")
    @ResponseBody
    public ResponseEntity<Void> updateStlmTakPtInf(
            @RequestBody @Valid StlmTakPtInfRspVO form
    ) {
        stlmTakPtInfService.updateStlmTakPtInfByService(form);
        return ResponseEntity.ok().build();
    }
}
