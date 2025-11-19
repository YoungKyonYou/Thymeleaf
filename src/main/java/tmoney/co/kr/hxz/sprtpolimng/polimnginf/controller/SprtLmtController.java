package tmoney.co.kr.hxz.sprtpolimng.polimnginf.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.service.SprtLmtService;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.AmtLstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.NcntLstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtModalDtlVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtModalVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtRspVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtSrchReqVO;

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
        SprtLmtModalVO vo = sprtLmtService.initModal();

        model.addAttribute("qt", vo.getQt());
        model.addAttribute("mon", vo.getMon());
        model.addAttribute("arr", vo.getArr());

        model.addAttribute("amtQt", new AmtLstVO(vo.getQt()));
        model.addAttribute("amtMon", new AmtLstVO(vo.getMon()));
        model.addAttribute("ncnt", new NcntLstVO(vo.getArr(), ""));
        return "hxz/sprtpolimng/polimnginf/sprtLmtPt :: amt-modal";
    }

    /**
     * 설정하기(편집) 눌렀을 때 3in1 모달을 띄운다.
     * - 탭: 분기/월/건수 모두 enable
     * - 현재 서비스 유형에 이미 설정된 타입(월/분기/건수)이 있으면 해당 탭이 기본 선택되도록 typ/dvs 코드를 내려준다.
     * - 선택된 탭 데이터는 "기존값", 나머지 탭은 "신규 기본값"으로 채운다.
     */
    @GetMapping("/sprtLmtDtl/{tpwSvcTypId}/edit.do")
    public String openEdit3In1(
            @PathVariable("tpwSvcTypId") String tpwSvcTypId,
            Model model
    ) {
        SprtLmtModalDtlVO vo = sprtLmtService.readSprtLmtByTpwSvcTypId(tpwSvcTypId);

        // 3) 모델 채우기 (3in1 신규 모달과 동일 fragment 사용: amt-modal)
        model.addAttribute("mode", "edit-3in1");     // JS에서 3in1 편집 모드로 인지
        model.addAttribute("tpwSvcTypId", tpwSvcTypId);
        model.addAttribute("dvsCd", vo.getDvsCd());
        model.addAttribute("typCd", vo.getTypCd());

        model.addAttribute("qt", vo.getQt());
        model.addAttribute("mon", vo.getMon());
        model.addAttribute("arr", vo.getArr());

        model.addAttribute("amtQt", new AmtLstVO(vo.getQt()));     // 분기 form backing
        model.addAttribute("amtMon", new AmtLstVO(vo.getMon()));   // 월 form backing
        model.addAttribute("ncnt",  new NcntLstVO(vo.getArr(), "")); // 건수 form backing

        // 모든 탭을 enable 하고, 선택 탭은 JS에서 dvsCd/typCd 보고 선택
        return "hxz/sprtpolimng/polimnginf/sprtLmtPt :: amt-modal";
    }

    /**
     * 지원 한도(금액) 수정 API
     *  - tbhxzd208 HXZ_지원금한도관리
     *
     * @return return ResponseEntity
     */

    @PostMapping(path = "/sprtLmt/edit.do")
    @ResponseBody
    public ResponseEntity<?> updateSprtLmtAmt(
            @RequestBody InstReqVO req
    ) {
        sprtLmtService.insertSprtLmtAmt(req);
        return ResponseEntity.ok().build();
    }
}
