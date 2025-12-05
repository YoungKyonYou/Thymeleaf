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
@RequiredArgsConstructor
public class BatTakPtInfController {

    private final BatTakPtInfService batTakPtInfService;
    private final DateUtil dateUtil;

    @GetMapping(value = "/batTakPtInf.do")
    public String readBatTakPtPaging(
            @ModelAttribute @Valid BatTakPtInfReqVO req,
            Model model
    ) {
        // 사용자가 날짜를 입력하지 않았을 때(초기 진입)만 기본값(30일 전 ~ 오늘) 설정
        // 입력값(20251127)이 있으면 이 if문을 건너뛰어 입력값이 유지됨
        if (req.getSttDt() == null || req.getSttDt().isEmpty()) {
            req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());
        }

        // 로그인 사용자 조직 코드 (임시)
        final String orgCd = "0000000";

        // 서비스 호출
        PageDataVO<BatTakPtInfRspVO> contents = batTakPtInfService.readBatTakPtPaging(req, orgCd);

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd);

        return "/hxz/spfnsprtmng/payinf/batTakPtInf";
    }
}