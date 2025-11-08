package tmoney.co.kr.hxz.common.otp.service;



import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import tmoney.co.kr.hxz.common.otp.mapper.TotpMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 시크릿 발급, otpauth URI 생성, 코드 검증 담당
 */
@Service
public class TotpService {

    private final TotpMapper totpMapper;
    private final GoogleAuthenticator gAuth;
    private final String issuer;

    public TotpService(TotpMapper totpMapper,
                       @Value("${app.totp.issuer}") String issuer) {
        this.totpMapper = totpMapper;
        this.issuer = issuer;

        // 표준 6자리, 30초 윈도우 기본 설정 (구글 인증앱 호환)
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setCodeDigits(6)
                .setTimeStepSizeInMillis(30_000)
                .build();
        this.gAuth = new GoogleAuthenticator(config);
    }

    /** 시크릿 생성 + 사용자에 저장하고 otpauth URI 반환 */
    public EnrollResult enroll(String mngrId) {
        int exists = totpMapper.existsById(mngrId);

        if(exists ==0 ){
            throw new RuntimeException();
        }

        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();

        totpMapper.updateOtpSecretById(mngrId, secret);


        String uri = buildOtpAuthUri(issuer, mngrId,secret);
        return new EnrollResult(secret, uri);
    }

    /** TOTP 6자리 코드 검증 */
    public boolean verify(String mngrId, int code) {
        String secret = totpMapper.readOtpSecretById(mngrId);

        if (secret == null || secret.isBlank())
            return false;
        return gAuth.authorize(secret, code);
    }

    /** otpauth://totp/{issuer}:{account}?secret=...&issuer=...&digits=6&period=30&algorithm=SHA1 */
    private String buildOtpAuthUri(String issuer, String account, String secret) {
        String label = url(issuer) + ":" + url(account);
        String params = "secret=" + url(secret)
                + "&issuer=" + url(issuer)
                + "&digits=6&period=30&algorithm=SHA1";
        return "otpauth://totp/" + label + "?" + params;
    }

    private static String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static class EnrollResult {
        public final String secret;
        public final String otpauthUri;
        public EnrollResult(String secret, String otpauthUri) {
            this.secret = secret;
            this.otpauthUri = otpauthUri;
        }
    }
}
