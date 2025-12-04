//package tmoney.co.kr.hxz.common.otp;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import tmoney.co.kr.hxz.common.otp.service.TotpService;
//import tmoney.co.kr.hxz.common.otp.vo.EnrollRequestVO;
//import tmoney.co.kr.hxz.common.otp.vo.VerifyRequestVO;
//import tmoney.co.kr.hxz.common.util.QrCodeUtil;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Map;
//
//@Controller
//
//public class TotpController {
//
//    private final TotpService totpService;
//    private final String issuer;
//
//    public TotpController(TotpService totpService, @Value("${app.totp.issuer}")String issuer) {
//        this.totpService = totpService;
//        this.issuer = issuer;
//    }
//    @GetMapping("/verify")
//    public String totp(){
//        return "hxz/common/totp/verify";
//    }
//
//    @GetMapping("/enroll")
//    public String totpEnroll(){
//        return "hxz/common/totp/enroll";
//    }
//
//    /** 등록(시크릿 발급, otpauth URI, QR 이미지 data URI) */
//    @ResponseBody
//    @PostMapping("/api/totp/enroll")
//    public Map<String, Object> enroll() {
//        var result = totpService.enroll("admin");
//        String qr = QrCodeUtil.toDataUriPng(result.otpauthUri, 280);
//
//        return Map.of(
//                "issuer", issuer,
//                "mngrId", "admin",
//                "secret", result.secret,
//                "otpauthUri", result.otpauthUri,
//                "qrDataUri", qr
//        );
//    }
//
//    /** 검증(TOTP 6자리) */
//    @ResponseBody
//    @PostMapping("/api/totp/verify")
//    public Map<String, Object> verify(@RequestBody VerifyRequestVO req, HttpServletRequest request) {
//        boolean ok = totpService.verify("admin", Integer.parseInt(req.getCode()));
//        if (ok) {
//            request.getSession(true).setAttribute("TWO_FACTOR_VERIFIED", true);
//        }
//        return Map.of("ok", ok);
//    }
//
//}
