package tmoney.co.kr.hxz.common.security.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TwoFactorEnforceFilter extends OncePerRequestFilter {

    public static final String ATTR_TWO_FACTOR_VERIFIED = "TWO_FACTOR_VERIFIED";

    private final Set<String> exactAllowPaths;   // 정확히 일치해야 허용되는 경로들
    private final List<String> prefixAllowPaths; // 접두사가 일치하면 허용되는 경로들
    private final String redirectPathWhenUnverified; // 미검증시 보낼 경로(예: "/verify")

    public TwoFactorEnforceFilter(
            Collection<String> exactAllowPaths,
            Collection<String> prefixAllowPaths,
            String redirectPathWhenUnverified
    ) {
        this.exactAllowPaths = Set.copyOf(exactAllowPaths);
        this.prefixAllowPaths = List.copyOf(prefixAllowPaths);
        this.redirectPathWhenUnverified = redirectPathWhenUnverified;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String path = request.getRequestURI();

        // 1) 정확히 허용되는 경로는 통과
        if (exactAllowPaths.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) 접두사로 허용되는 경로는 통과 (정적리소스, API prefix 등)
        for (String prefix : prefixAllowPaths) {
            if (path.startsWith(prefix)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 3) 2FA 검증 여부 확인
        HttpSession session = request.getSession(false);
        boolean verified = (session != null)
                && Boolean.TRUE.equals(session.getAttribute(ATTR_TWO_FACTOR_VERIFIED));

        if (!verified) {
            response.sendRedirect(redirectPathWhenUnverified);
            return;
        }

        // 4) 통과
        filterChain.doFilter(request, response);
    }

}

/*

@Bean
TwoFactorEnforceFilter twoFactorEnforceFilter() {
    return new TwoFactorEnforceFilter(
            Set.of("/login", "/enroll", "/verify"),           // 정확히 허용
            List.of("/api/totp/", "/css/", "/js/", "/images/"), // 접두사 허용
            "/verify"                                         // 미검증시 리다이렉트
    );
}

http.addFilterAfter(twoFactorEnforceFilter(), UsernamePasswordAuthenticationFilter.class);
*/
