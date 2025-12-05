package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.BatTakPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfRspVO;

import javax.validation.Valid;

@Controller
@RequestMapping("/spfnsprtmng/payinf")
// resources/templates/hxz/spfnsprtmng/payinf/BatTakPtInf.html
@RequiredArgsConstructor
public class BatTakPtInfController {

    /**
     * 배치작업 내역조회
     *  - tbhxzm201 : HXZ_교통복지서비스관리
     *  - tbhxzm202 : HXZ_교통복지서비스유형관리
     */
    private final BatTakPtInfService batTakPtInfService;
    private final DateUtil dateUtil;

    // @PreAuthorize("hasPermission ('배치작업내역조회', 'READ')")
    @GetMapping(value = "/batTakPtInf.do")
    public String readBatTakPtPaging(
            @ModelAttribute @Valid BatTakPtInfReqVO req,
            Model model
    ) {

        // 기본 조회일자 설정 (30일 전 ~ 오늘)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        final String orgCd = "0000000";
        PageDataVO<BatTakPtInfRspVO> contents = batTakPtInfService.readBatTakPtPaging(req, orgCd); // TODO: 추후 로그인 연동(orgCd)

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd);

        return "/hxz/spfnsprtmng/payinf/batTakPtInf";
    }
}
