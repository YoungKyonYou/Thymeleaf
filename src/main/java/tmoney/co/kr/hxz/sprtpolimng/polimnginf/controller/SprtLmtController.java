package tmoney.co.kr.hxz.sprtpolimng.polimnginf.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.service.SprtLmtService;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtInstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.AmtLstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.LstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.NcntLstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtDtlRspVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtRspVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtSrchReqVO;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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

    @GetMapping(value = "/sprtLmtDtl/new/amt")
    public String newSprtLmtAmt(
            Model model
    ) {
        List<AmtReqVO> qt = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            qt.add(new AmtReqVO());
        }

        List<AmtReqVO> mon = new ArrayList<>();
        int year = LocalDate.now().getYear();
        for (int i = 1; i <= 12; i++) {
            String yyyymm = String.format("%d%02d", year, i);
            mon.add(new AmtReqVO("", yyyymm, yyyymm, 0));
        }

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

    @GetMapping(value = "/sprtLmtDtl/new/ncnt")
    public String newSprtLmtNcnt(
            Model model
    ) {
        List<NcntReqVO> arr = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            arr.add(new NcntReqVO());
        }

        model.addAttribute("ncnt", new NcntLstVO(arr, ""));
        model.addAttribute("arr", arr);

        return "hxz/sprtpolimng/polimnginf/sprtLmtPt :: ncnt-modal";
    }

    /**
     * 지원 한도 상세 조회 모달
     *  - tbhxzd208 HXZ_지원금한도관리
     *
     * [process]
     *  1. 서비스 유형 ID(tpwSvcTypId)로 HXZ_지원금한도관리 테이블 내 한도 관리 내역 호출
     *
     * @return return String
     */

    @GetMapping(value = "/sprtLmtDtl/{useYn}/{tpwSvcTypId}")
    public String readSprtLmtDtl(
            @PathVariable("tpwSvcTypId") String tpwSvcTypId,
            @PathVariable("useYn") String useYn,
            Model model
    ) {
        SprtLmtDtlRspVO contents = sprtLmtService.readSprtLmtDtl(tpwSvcTypId, useYn);
        List<SprtLmtReqVO> form = new ArrayList<>(contents.getSprtLmtReqList());

        model.addAttribute("sprtLmtDtlList", form);
        model.addAttribute("typCd", contents.getTpwLmtTypCd());
        model.addAttribute("dvsCd", contents.getTpwLmtDvsCd());
        model.addAttribute("useYn", contents.getUseYn());
        model.addAttribute("tpwSvcTypId", tpwSvcTypId);

        model.addAttribute("form", new LstVO(form));

        return "/hxz/sprtpolimng/polimnginf/sprtLmtPt :: modal-detail";
    }

    /**
     * 지원 한도(금액) 추가 API
     *  - tbhxzd208 HXZ_지원금한도관리
     *
     * @return return ResponseEntity
     */

    @PostMapping(path = "/sprtLmt/amt")
    @ResponseBody
    public ResponseEntity<?> insertSprtLmtAmt(
            @Valid @RequestBody AmtInstReqVO req
    ) {
        sprtLmtService.insertSprtLmtAmt(req);
        return ResponseEntity.ok().build();
    }

    /**
     * 지원 한도(금액) 수정 API
     *  - tbhxzd208 HXZ_지원금한도관리
     *
     * @return return ResponseEntity
     */

    @PostMapping(path = "/sprtLmt/amt/{tpwSvcTypId}/edit")
    @ResponseBody
    public ResponseEntity<?> updateSprtLmtAmt(
            @PathVariable("tpwSvcTypId") String tpwSvcTypId,
            @RequestBody List<AmtReqVO> req
    ) {
        sprtLmtService.updateSprtLmtAmt(req, tpwSvcTypId);
        return ResponseEntity.ok().build();
    }
}
