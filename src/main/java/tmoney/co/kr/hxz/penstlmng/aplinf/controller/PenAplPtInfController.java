package tmoney.co.kr.hxz.penstlmng.aplinf.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import tmoney.co.kr.hxz.common.file.domain.FilesStorageProperties;
import tmoney.co.kr.hxz.common.file.service.AttachmentService;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.penstlmng.aplinf.service.PenAplPtInfService;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfRspVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfReqVO;

import javax.validation.Valid;



@Controller
@RequiredArgsConstructor
@RequestMapping("/penstlmng/aplinf")
public class PenAplPtInfController {
    private final PenAplPtInfService penAplPtInfService;
    private final DateUtil dateUtil;
    
    //파일미리보기 서비스 주입
    private final AttachmentService attachmentService;
    private final FilesStorageProperties filesStorageProperties;

    // @PreAuthorize("hasPermission ('지급금신청내역조회', 'READ')")
    @GetMapping(value = "/penAplPtInf.do")
    public String readPenAplPtInfPaging(
            @ModelAttribute @Valid PenAplPtInfReqVO req,
            Model model
    )
    {

        // 검색폼에 endDt만 있어서 적용
        if ( req.getEndDt() != null && ! req.getEndDt().isEmpty() )
            req.setSttDt(req.getEndDt());

        // 기본 조회일자 설정 (30일 전 ~ 오늘)
        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        final String orgCd = "0000000";
        req.setOrgCd(orgCd);
        PageDataVO<PenAplPtInfRspVO> contents = penAplPtInfService.readPenAplPtInfPaging(req, orgCd); // TODO: 추후 로그인 연동(orgCd)

        model.addAttribute("pageData", contents);

        // 월별/일별 통계 데이터
        model.addAttribute("monthly", penAplPtInfService.readPenAplCntByMonth(req));
        model.addAttribute("daily", penAplPtInfService.readPenAplCntByDay(req));

        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd);

        return "/hxz/penstlmng/aplinf/penAplPtInf";
    }


    /**
     * 신청 승인 처리 
     * 01 : 요청
     * 02 : 반려
     * 03 : 승인
     */
    @PostMapping("/approve")
    @ResponseBody
    public ResponseEntity<Void> approvePenApl(@RequestBody PenAplPtInfRspVO form) {
        form.setAprvStaCd("03");
        penAplPtInfService.updateApprove(form);
        return ResponseEntity.ok().build();
    }

}