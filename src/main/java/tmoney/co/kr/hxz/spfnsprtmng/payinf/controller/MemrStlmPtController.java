package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
//import tmoney.co.kr.hxz.common.vo.MngrVO; // @MgrVO가 포함된 패키지라고 가정
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.MemrStlmPtService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtRspVO;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/spfnsprtmng/payinf")
public class MemrStlmPtController {

    private final MemrStlmPtService memrStlmPtService;
    private final DateUtil dateUtil; // DateUtil 클래스도 필요

    // 땜빵
    public class MngrDTO
    {
        private String orgCd;

        public String getOrgCd()
        {
            return orgCd;
        }

        public void setOrgCd(String orgCd)
        {
            this.orgCd = orgCd;
        }
    }

    /**
     * 수기정산내역 조회 API
     * tbhxzd216 d216 - tbhxzd_수기정산내역 테이블
     * [process]
     * 1. tbhxzd_수기정산내역 테이블 레코드 리스트 추출
     *
     * @return return String
     */
    // @PreAuthorize("hasPermission(orgCd, '수기정산내역조회', 'READ')") // 지원금지급현황 조회
//    @PreAuthorize("hasPermission(\"수기정산내역조회\", 'READ')")
    @GetMapping(value = "/memrStlmPt.do")
    public String readTrCUsePtPaging(
            @ModelAttribute @Valid MemrStlmPtReqVO req,
//            @MgrVO MngrVO mngrVO,
            String orgCd,
            Model model
    ) {

        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        PageDataVO<MemrStlmPtRspVO> contents = memrStlmPtService.readMemrStlmPtPaging(req, orgCd);// mngrVO.getOrgCd()

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);
        model.addAttribute("orgCd", "000000");
        MngrDTO mngr = new MngrDTO();
        model.addAttribute("mngr", mngr);

        return "hxz/spfnsprtmng/payinf/memrStlmPt";
    }
}