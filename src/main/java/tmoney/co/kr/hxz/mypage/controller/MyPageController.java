package tmoney.co.kr.hxz.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tmoney.co.kr.hxz.mypage.service.MyPageService;
import tmoney.co.kr.hxz.mypage.vo.MyPageVO;
import tmoney.co.kr.hxz.mypage.vo.PwdChangeVO;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 현재 로그인한 관리자 ID 조회
     * - 기본적으로 Spring Security Authentication 의 name 사용
     *
     * @return mngrId (현재 로그인한 관리자 ID)
     */
    private String getCurrentMngrId() {
     /*   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("로그인 정보가 없습니다.");
        }*/
       // return auth.getName();
        return "admin";
    }

    /**
     * 마이페이지 진입 (GET)
     *
     * @param model 스프링 UI 모델
     * @return 마이페이지 템플릿 경로
     */
    @GetMapping
    public String myPageForm(Model model) {
        String mngrId = getCurrentMngrId();

        // 서비스에서 DB → MyPageVO 변환까지 수행
        MyPageVO myPage = myPageService.getMyPage(mngrId);
        model.addAttribute("myPage", myPage);

        return "component/mypage";   // 네가 만든 Thymeleaf 파일명
    }

    /**
     * 마이페이지 기본정보 저장 (POST)
     *  - 이름 / 이메일 / 전화 / 휴대전화
     *
     * @param myPageVO        화면에서 넘어온 마이페이지 폼 데이터
     * @param bindingResult   Spring Validation 결과
     * @param model           UI 모델
     * @param redirectAttributes 리다이렉트 시 플래시 속성
     * @return 성공 시 리다이렉트, 실패 시 마이페이지 화면 그대로 반환
     */
    @PostMapping
    public String updateMyPage(@Valid @ModelAttribute("myPage") MyPageVO myPageVO,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        String mngrId = "admin";

        // 2) 서비스 호출 (DB 업데이트)
        myPageService.updateMyPage(mngrId, myPageVO);

        // 3) 성공 메시지 플래시로 넘겨서 화면에서 Common.modalShow로 사용해도 되고, toast로 써도 됨
        redirectAttributes.addFlashAttribute("myPageSuccessMessage", "마이페이지 정보가 저장되었습니다.");

        return "redirect:/mypage";
    }

    /**
     * 비밀번호 변경 (AJAX, JSON)
     *  - front: Common.sendSafe('/mypage/password', { method: 'POST', data: { currentPassword, newPassword } })
     *
     * @param request 비밀번호 변경 요청 VO
     * @return JSON 응답 (성공/실패 메시지 포함)
     */
    @PostMapping("/password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody @Valid PwdChangeVO request) {
        String mngrId = getCurrentMngrId();

        Map<String, Object> body = new HashMap<>();
        try {
            myPageService.changePassword(mngrId, request);

            body.put("success", true);
            body.put("message", "비밀번호가 변경되었습니다.");
            return ResponseEntity.ok(body);

        } catch (IllegalArgumentException ex) {
            // 서비스에서 던진 도메인 예외 코드에 따라 메시지 분기
            String code = ex.getMessage();
            String msg;

            if ("CURRENT_PASSWORD_NOT_MATCH".equals(code)) {
                msg = "현재 비밀번호가 일치하지 않습니다.";
            } else if ("SAME_AS_OLD_PASSWORD".equals(code)) {
                msg = "이전과 동일한 비밀번호는 사용할 수 없습니다.";
            } else {
                msg = "비밀번호 변경에 실패했습니다.";
            }

            body.put("success", false);
            body.put("message", msg);
            return ResponseEntity.badRequest().body(body);

        } catch (Exception e) {
            body.put("success", false);
            body.put("message", "비밀번호 변경 처리 중 오류가 발생했습니다.");
            // 500 으로 보내도 되지만, Common.sendSafe 에서 clientErrorMsg/otherErrorMsg 쓰는 구조니까 500 그대로 던져도 무방
            return ResponseEntity.status(500).body(body);
        }
    }
}