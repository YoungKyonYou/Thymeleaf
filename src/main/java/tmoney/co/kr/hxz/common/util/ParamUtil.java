package tmoney.co.kr.hxz.common.util;
public final class ParamUtil {

    private ParamUtil() {
    }

    /**
     * 전화번호를 3파트로 나눈 결과를 담는 간단한 DTO
     */
    public static class PhoneParts {
        private final String p1;
        private final String p2;
        private final String p3;

        public PhoneParts(String p1, String p2, String p3) {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }

        public String getP1() {
            return p1;
        }

        public String getP2() {
            return p2;
        }

        public String getP3() {
            return p3;
        }
    }

    /**
     * "010-1234-5678" → PhoneParts("010","1234","5678")
     *
     * @param src 원본 전화번호 문자열
     * @return 분리된 전화번호 파트
     */
    public static PhoneParts splitPhone(String src) {
        String a = null, b = null, c = null;
        if (src != null && !src.isEmpty()) {
            String[] parts = src.split("-");
            if (parts.length > 0) a = parts[0];
            if (parts.length > 1) b = parts[1];
            if (parts.length > 2) c = parts[2];
        }
        return new PhoneParts(a, b, c);
    }

    /**
     * 세 파트의 전화번호를 하나의 문자열로 합침.
     * 전부 비어 있으면 null 반환.
     *
     * @param p1 앞자리
     * @param p2 중간
     * @param p3 끝자리
     * @return 조합된 전화번호 또는 null
     */
    public static String joinPhone(String p1, String p2, String p3) {
        if (isBlank(p1) && isBlank(p2) && isBlank(p3)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (!isBlank(p1)) sb.append(p1);
        if (!isBlank(p2)) {
            if (sb.length() > 0) sb.append("-");
            sb.append(p2);
        }
        if (!isBlank(p3)) {
            if (sb.length() > 0) sb.append("-");
            sb.append(p3);
        }
        return sb.toString();
    }

    /**
     * 이메일 ID + 도메인 합치기.
     * ID가 없으면 null, 도메인이 없으면 ID만 반환.
     * DB mail_addr(64) 길이 안전하게 잘라줌.
     *
     * @param id     이메일 앞부분
     * @param domain 도메인
     * @return 합쳐진 이메일 또는 null
     */
    public static String joinEmail(String id, String domain) {
        if (isBlank(id)) {
            return null;
        }
        if (isBlank(domain)) {
            return id;
        }
        String email = id + "@" + domain;
        return (email.length() > 64) ? email.substring(0, 64) : email;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}