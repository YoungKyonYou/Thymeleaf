package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimReqMngService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngRspVO;

import javax.validation.Valid;

@Controller
@RequestMapping("/spfnsprtmng/payinf")
// resources/templates/hxz/spfnsprtmng/payinf/simReqMng.html
@RequiredArgsConstructor
public class SimReqMngController {

    private final SimReqMngService simreqmngservice;
    private final DateUtil dateUtil;

    /**
     * 시뮬레이션요청관리
     *  - tbhxzm201 : HXZ_교통복지서비스관리
     *  - tbhxzm202 : HXZ_교통복지서비스유형관리
     */
    // @PreAuthorize("hasPermission ('시뮬레이션요청관리', 'READ')")
    @GetMapping(value = "/simReqMng.do")
    public String simReqMngPaging(
            @Valid @ModelAttribute SimReqMngReqVO req,
            Model model
    ) {

        req.updateDefaultDate(dateUtil.thirtyDaysAgo(), dateUtil.today());

        final String orgCd ="0000000";
        PageDataVO<SimReqMngRspVO> contents = simreqmngservice.readSimReqMngPaging(req , orgCd); // TODO: 추후 로그인 연동(orgCd)

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);

        model.addAttribute("orgCd", orgCd);


        return "/hxz/spfnsprtmng/payinf/simReqMng";
    }



    /** -----------------------------------------
     * 3. 신규 등록
     * ---------------------------------------- */
    @PostMapping("/simReqMng/save")  // URL 그대로 유지, POST 방식
    @ResponseBody
    public ResponseEntity<Void> saveSimReqMng(
            @RequestBody SimReqMngRspVO form
    ) {
        simreqmngservice.saveSimReqMng(form);
        return ResponseEntity.ok().build();
    }

}
