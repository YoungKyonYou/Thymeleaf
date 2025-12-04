package tmoney.co.kr.hxz.spfnsprtmng.payinf.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.common.util.DateUtil;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimReqMngService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngRspVO;

import java.util.List;

import javax.validation.Valid;

@Controller
@RequestMapping("/spfnsprtmng/payinf")
@RequiredArgsConstructor
public class SimReqMngController {

    private final SimReqMngService simReqMngService;
    private final DateUtil dateUtil;

    /**
     * 시뮬레이션요청관리 조회 화면
     */
    @GetMapping(value = "/simReqMng.do")
    public String simReqMngPaging(
            @Valid @ModelAttribute("req") SimReqMngReqVO req,
            Model model
    ) {
        if (req.getSttDt() == null || req.getSttDt().isEmpty()) {
            req.setSttDt(dateUtil.thirtyDaysAgo());
        }
        if (req.getEndDt() == null || req.getEndDt().isEmpty()) {
            req.setEndDt(dateUtil.today());
        }

        final String orgCd = "0000000";
        PageDataVO<SimReqMngRspVO> contents = simReqMngService.readSimReqMngPaging(req, orgCd);

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);
        model.addAttribute("orgCd", orgCd);

        return "/hxz/spfnsprtmng/payinf/simReqMng";
    }

    /**
     * 1. 신규 등록 (POST)
     */
    @PostMapping("/simReqMng/add.do") // JS에서 호출하는 URL과 일치시킴 (/save -> /regist)
    @ResponseBody
    public ResponseEntity<?> saveSimReqMng(@RequestBody SimReqMngRspVO form) {
        try {
            simReqMngService.saveSimReqMng(form);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 2. 수정 (PUT)
     */
    @PutMapping("/simReqMng/update")
    @ResponseBody
    public ResponseEntity<?> updateSimReqMng(@RequestBody SimReqMngRspVO form) {
        try {
            // 통합 수정 메서드 호출 (내부에서 208->207 순차 실행 및 건수 체크)
            simReqMngService.updateSimReqMng(form);
            return ResponseEntity.ok("SUCCESS");
        } catch (RuntimeException re) {
            // "조건 불일치" 등의 예외 메시지를 프론트로 전달
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(re.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 3. 삭제 (POST)
     */
    @PostMapping("/simReqMng/delete")
    @ResponseBody
    public ResponseEntity<?> deleteSimReqMng(@RequestBody SimReqMngRspVO form) {
        try {
            // 통합 삭제 메서드 호출 (내부에서 207->208 순차 실행)
            simReqMngService.deleteSimReqMng(form);
            return ResponseEntity.ok("SUCCESS");
        } catch (RuntimeException re) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(re.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중 오류가 발생했습니다.");
        }
    }


    /**
     * 엑셀 업로드 데이터 받기
     */
    @PostMapping("/simReqMng/import")
    @ResponseBody
    public ResponseEntity<?> importSimReqMng(@RequestBody List<SimReqMngRspVO> list)
    {
        for (SimReqMngRspVO form : list) {
            simReqMngService.saveSimReqMng(form);
        }
        return ResponseEntity.ok("SUCCESS");
    }
}