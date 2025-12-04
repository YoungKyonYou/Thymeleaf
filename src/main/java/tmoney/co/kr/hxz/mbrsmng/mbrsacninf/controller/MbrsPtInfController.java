package tmoney.co.kr.hxz.mbrsmng.mbrsacninf.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.service.MbrsPtInfService;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfReqVO;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfRspVO;

import javax.validation.Valid;

@Controller
@RequestMapping("/mbrsmng/mbrsacninf")
@RequiredArgsConstructor
public class MbrsPtInfController {

    //회원정보
    //MbrsPtInf
    private final MbrsPtInfService mbrsPtInfService;
    private final DateUtil dateUtil;

    /**
     * 회원정보내역조회 조회 화면
     */
    @GetMapping(value = "/mbrsPtInf.do")
    public String readMbrsPtInfPaging(
            @Valid @ModelAttribute("req") MbrsPtInfReqVO req,
            Model model
    ) {
        if (req.getSttDt() == null || req.getSttDt().isEmpty()) {
            req.setSttDt(dateUtil.thirtyDaysAgo());
        }
        if (req.getEndDt() == null || req.getEndDt().isEmpty()) {
            req.setEndDt(dateUtil.today());
        }

         final String orgCd = "0000000";
         PageDataVO<MbrsPtInfRspVO> contents = mbrsPtInfService.readMbrsPtInfPaging(req, orgCd);

         model.addAttribute("pageData", contents);
        model.addAttribute("req", req);
         model.addAttribute("orgCd", orgCd);

        return "/hxz/mbrsmng/mbrsacninf/mbrsPtInf";
    }






}