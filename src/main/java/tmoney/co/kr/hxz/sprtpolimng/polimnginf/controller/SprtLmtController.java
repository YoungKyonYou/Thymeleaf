package tmoney.co.kr.hxz.sprtpolimng.polimnginf.controller;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.service.SprtLmtService;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.AmtLstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.NcntLstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sprtpolimng/polimnginf")
public class SprtLmtController {

    private final SprtLmtService sprtLmtService;

    /**
     * 지원 한도 내역 조회
     *  - tbhxzd208  HXZ_지원금한도관리
     *  - tbhxzm201  HXZ_교통복지서비스관리
     *  - tbhxzm202  HXZ_교통복지서비스유형관리
     *
     * [process]
     *  1. HXZ_지원금한도관리 테이블 내 서비스 유형 ID(tpwSvcTypId)를 기준으로 한도 관리 내역 호출
     *  2. 교통복지서비스관리  HXZ_교통복지서비스관리  내 교통복지서비스명 매칭
     *  3. 교통복지서비스유형관리  HXZ_교통복지서비스유형관리 내 교통복지서비스유형명 호출
     *
     * @return return String
     */

    @GetMapping(value = "/sprtLmtPt.do")
    public String readSprtLmtPtPaging(
            @ModelAttribute SprtLmtSrchReqVO req,
            Model model
    ) {
        PageDataVO<SprtLmtRspVO> contents = sprtLmtService.readSprtLmtPtPaging(req);

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);

        return "hxz/sprtpolimng/polimnginf/sprtLmtPt";
    }

    /**
     * 지원 한도(금액) 신규 모달
     *
     * @return return String
     */


    @GetMapping(value = "/sprtLmtDtl/new.do")
    public String newSprtLmt(

            Model model
    ) {
        // 분기/월은 신규일 때 빈 리스트로
        List<AmtReqVO> qt  = new ArrayList<>();
        List<AmtReqVO> mon = new ArrayList<>();

        model.addAttribute("mode", "new-3in1");
        model.addAttribute("dvsCd", "01");   // 기본 금액
        model.addAttribute("typCd", "02");   // 기본 분기

        model.addAttribute("qt", qt);
        model.addAttribute("mon", mon);
        model.addAttribute("amtQt", new AmtLstVO(qt));
        model.addAttribute("amtMon", new AmtLstVO(mon));

        return "hxz/sprtpolimng/polimnginf/sprtLmtPt :: amt-modal";
    }

    /**
     * 지원 한도(건수) 신규 모달
     *
     * @return return String
     */
    @GetMapping("/sprtLmtDtl/{tpwSvcId}/{tpwSvcTypId}/edit.do")
    public String openEdit3In1(
            @PathVariable("tpwSvcId") String tpwSvcId,
            @PathVariable("tpwSvcTypId") String tpwSvcTypId,
            Model model
    ) {
        SprtLmtModalDtlVO vo = sprtLmtService.readSprtLmtByTpwSvcTypId(tpwSvcId, tpwSvcTypId);

        model.addAttribute("mode", "edit-3in1");
        model.addAttribute("tpwSvcTypId", tpwSvcTypId);
        model.addAttribute("dvsCd", vo.getDvsCd());
        model.addAttribute("typCd", vo.getTypCd());

        model.addAttribute("qt", vo.getQt());
        model.addAttribute("mon", vo.getMon());
        model.addAttribute("arr", vo.getArr());

        model.addAttribute("amtQt", new AmtLstVO(vo.getQt()));
        model.addAttribute("amtMon",  new AmtLstVO(vo.getMon()));
        model.addAttribute("ncnt", new NcntLstVO(vo.getArr(), ""));

        return "hxz/sprtpolimng/polimnginf/sprtLmtPt :: amt-modal";
    }

    @PostMapping("/sprtLmt/edit.do")
    @ResponseBody
    public ResponseEntity<?> saveSprtLmt(@RequestBody InstReqVO req) {
        sprtLmtService.insertSprtLmtAmt(req);

        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        return ResponseEntity.ok(body);
    }


    /**
     * 지원 한도(금액) 수정 API
     *  - tbhxzd208 HXZ_지원금한도관리
     *
     * @return return ResponseEntity
     */

    @GetMapping("/sprtLmtDtl/check-exist.do")
    @ResponseBody
    public ResponseEntity<?> checkExistingLimit(
            @RequestParam String tpwSvcId,
            @RequestParam String tpwSvcTypId
    ) {
        // 기존 한도 중 "분기" 타입만 뽑아오는 메서드 하나 만들면 좋음
        List<QuarterRangeVO> qtRanges =
                sprtLmtService.readQuarterRanges(tpwSvcId, tpwSvcTypId);

        boolean exists = !qtRanges.isEmpty();

        Map<String, Object> body = new HashMap<>();
        body.put("exists", exists);
        body.put("qtRanges", qtRanges);

        return ResponseEntity.ok(body);
    }
}
