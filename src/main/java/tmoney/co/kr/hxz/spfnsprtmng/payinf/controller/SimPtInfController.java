package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfRspVO;

import javax.validation.Valid;

@Controller
@RequestMapping("/spfnsprtmng/payinf")
// resources/templates/hxz/spfnsprtmng/payinf/simPtInf.html
@RequiredArgsConstructor
public class SimPtInfController {

    /**
     * 시뮬레이션 내역조회
     *  - tbhxzm201 : HXZ_교통복지서비스관리
     *  - tbhxzm202 : HXZ_교통복지서비스유형관리
     */
    private final SimPtInfService simPtInfService;
    private final DateUtil dateUtil;

    // @PreAuthorize("hasPermission ('시뮬레이션내역조회', 'READ')")
    @GetMapping(value = "/simPtInf.do")
    public String readSimPtPaging(
            @Valid @ModelAttribute SimPtInfReqVO req,
            Model model
    ) {

        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        final String orgCd ="0000000";
        PageDataVO<SimPtInfRspVO> contents = simPtInfService.readSimPtPaging(req , orgCd); // TODO: 추후 로그인 연동(orgCd)

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);

        model.addAttribute("orgCd", orgCd);


        return "/hxz/spfnsprtmng/payinf/simPtInf";
    }
}
