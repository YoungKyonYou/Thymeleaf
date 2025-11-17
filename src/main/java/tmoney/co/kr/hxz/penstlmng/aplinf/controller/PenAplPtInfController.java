package tmoney.co.kr.hxz.penstlmng.aplinf.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.penstlmng.aplinf.service.PenAplPtInfService;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfRspVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfReqVO;

import javax.validation.Valid;
import java.awt.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/penstlmng/aplinf")
public class PenAplPtInfController {
    private final PenAplPtInfService penAplPtInfService;
    private final DateUtil dateUtil;

    // @PreAuthorize("hasPermission ('지급금신청내역조회', 'READ')")
    @GetMapping(value = "/penAplPtInf.do")
    public String readPenAplPtInfPaging(
            @ModelAttribute @Valid PenAplPtInfReqVO req,
            Model model
    )
    {
        // 기본 조회일자 설정 (30일 전 ~ 오늘)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        final String orgCd = "0000000";
        PageDataVO<PenAplPtInfRspVO> contents = penAplPtInfService.readPenAplPtInfPaging(req, orgCd); // TODO: 추후 로그인 연동(orgCd)

        model.addAttribute("pageData", contents);

        // 월별/일별 통계 데이터
        model.addAttribute("monthly", penAplPtInfService.readPenAplCntByMonth(orgCd, req.getTpwSvcId(),req.getTpwSvcId()));
        model.addAttribute("daily", penAplPtInfService.readPenAplCntByDay(req.getSttDt(), orgCd, req.getTpwSvcId(),req.getTpwSvcId()));


        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd);

        return "/hxz/penstlmng/aplinf/penAplPtInf";
    }
}
