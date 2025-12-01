package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.MemrStlmPtService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtRspVO;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/spfnsprtmng/payinf")
public class MemrStlmPtController {

    private final MemrStlmPtService memrStlmPtService;
    private final DateUtil dateUtil;

    // ==============================================================
    // 1. 수기정산내역 리스트 조회
    // ==============================================================

    /** -----------------------------------------
     * 1-1. 수기정산내역 리스트 조회 (검색용 ReqVO)
     * ---------------------------------------- */
    @GetMapping("/memrStlmPt.do")
    public String readMemrStlmPt(
            @ModelAttribute @Valid MemrStlmPtReqVO req,
            String orgCd,
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
        PageDataVO<MemrStlmPtRspVO> contents = memrStlmPtService.readMemrStlmPtPaging(req, orgCd);

        // Model에 조회 결과 및 요청 조건 담기
        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd);

        // 디버깅 출력
        System.out.println("====== [DEBUG] readMemrStlmPt ======");
        if (contents != null) {
            System.out.println("Total Count: " + contents.getTotal());
        }
        System.out.println("Search Condition: " + req);
        System.out.println("=====================================");

        return "/hxz/spfnsprtmng/payinf/memrStlmPt";
    }


    // ==============================================================
    // 2. 수기정산 - 등록/수정/삭제 API 그룹
    // ==============================================================

    /** -----------------------------------------
     * 2-1. 수기정산내역 등록 API
     * ---------------------------------------- */
    @PostMapping(path = "/memrStlmPt/add.do")
    @ResponseBody
    public ResponseEntity<Void> saveMemrStlmPt(
            @RequestBody @Valid MemrStlmPtRspVO form // RspVO 사용
    ) {
        // 기관코드 없을 시 기본 처리
        if (form.getOrgCd() == null || form.getOrgCd().isEmpty()) {
            form.setOrgCd("000000");
        }

        System.out.println("====== [DEBUG] saveMemrStlmPt ======");
        System.out.println("Data: " + form);
        System.out.println("====================================");

        memrStlmPtService.saveMemrStlmPt(form);

        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     * 2-2. 수기정산내역 수정 API
     * ---------------------------------------- */
    @PutMapping(path = "/memrStlmPt/edit.do")
    @ResponseBody
    public ResponseEntity<Void> updateMemrStlmPt(
            @RequestBody @Valid MemrStlmPtRspVO form // RspVO 사용
    ) {
        if (form.getOrgCd() == null || form.getOrgCd().isEmpty()) {
            form.setOrgCd("000000");
        }

        System.out.println("====== [DEBUG] updateMemrStlmPt ======");
        System.out.println("Data: " + form);
        System.out.println("======================================");

        memrStlmPtService.updateMemrStlmPt(form);

        return ResponseEntity.ok().build();
    }

    /** -----------------------------------------
     * 2-3. 수기정산내역 삭제 API (일괄 삭제)
     * [수정] @RequestBody List<...> 로 변경하여 JSON 배열을 받도록 함
     * ---------------------------------------- */
    @PostMapping(path = "/memrStlmPt/delete.do")
    @ResponseBody
    public ResponseEntity<String> deleteMemrStlmPt(
            @RequestBody List<MemrStlmPtRspVO> list
    ) {
        System.out.println("====== [DEBUG] deleteMemrStlmPt List ======");
        System.out.println("Size: " + (list != null ? list.size() : 0));

        if (list == null || list.isEmpty()) {
            return ResponseEntity.badRequest().body("삭제할 데이터가 없습니다.");
        }

        try {
            // 리스트 처리용 서비스 메서드 호출
            memrStlmPtService.deleteMemrStlmPtList(list);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("삭제 중 오류 발생");
        }
    }

    /** -----------------------------------------
     * 2-4. 수기정산내역 선택 승인 API (일괄 지급처리)
     * ---------------------------------------- */
    @PostMapping(path = "/memrStlmPt/approve.do")
    @ResponseBody
    public ResponseEntity<String> approveMemrStlmPtList(
            @RequestBody List<MemrStlmPtRspVO> list
    ) {
        if (list == null || list.isEmpty()) {
            return ResponseEntity.badRequest().body("승인할 데이터가 없습니다.");
        }

        try {
            memrStlmPtService.saveApproveMemrStlmPtList(list);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("승인 처리 중 오류가 발생했습니다.");
        }
    }


    /** -----------------------------------------
     * 수기정산내역 업로드
     * ---------------------------------------- */
    @PostMapping(path = "/memrStlmPt/import.do")
    @ResponseBody
    public ResponseEntity<Void> importMemrStlmPt(
            @RequestBody @Valid List<MemrStlmPtRspVO> list // RspVO 사용
    ) {

        for (MemrStlmPtRspVO form : list) {
            // 기관코드 없을 시 기본 처리
            if (form.getOrgCd() == null || form.getOrgCd().isEmpty()) {
                form.setOrgCd("000000");
            }
    
            memrStlmPtService.saveMemrStlmPt(form);
        }

        return ResponseEntity.ok().build();
    }


}