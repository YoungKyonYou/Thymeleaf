package tmoney.co.kr.hxz.mypage.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tmoney.co.kr.hxz.common.util.ParamUtil;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MyPageVO {
    @NotBlank
    @Size(max = 20)
    private String userId;        // tbhxzm106.mngr_id

    @NotBlank
    @Size(max = 40)
    private String userName;      // tbhxzm106.mngr_name

    // 화면 표시에만 사용 (org_cd == '0000000' -> ADMIN, else USER)
    @Size(max = 10)
    private String userType;

    @Size(max = 32)
    private String emailId;

    @Size(max = 32)
    private String emailDomain;

    // 전화번호: 정규식으로 숫자만 허용
    @Pattern(regexp = "^[0-9]{0,3}$", message = "전화번호 앞자리는 숫자 3자리까지입니다.")
    private String tel1;

    @Pattern(regexp = "^[0-9]{0,4}$", message = "전화번호 가운데는 숫자 4자리까지입니다.")
    private String tel2;

    @Pattern(regexp = "^[0-9]{0,4}$", message = "전화번호 끝자리는 숫자 4자리까지입니다.")
    private String tel3;

    @Pattern(regexp = "^[0-9]{0,3}$", message = "휴대전화 앞자리는 숫자 3자리까지입니다.")
    private String mobile1;

    @Pattern(regexp = "^[0-9]{0,4}$", message = "휴대전화 가운데는 숫자 4자리까지입니다.")
    private String mobile2;

    @Pattern(regexp = "^[0-9]{0,4}$", message = "휴대전화 끝자리는 숫자 4자리까지입니다.")
    private String mobile3;
    // getter/setter

    public static MyPageVO from(MngrVO src) {
        if (src == null) {
            return null;
        }

        MyPageVO vo = new MyPageVO();

        // ID / 이름
        vo.setUserId(src.getMngrId());
        vo.setUserName(src.getMngrName());

        // org_cd == '0000000' 이면 시스템 관리자
        String orgCd = src.getOrgCd();
        vo.setUserType("0000000".equals(orgCd) ? "시스템 운영자" : "지자체 운영자");

        // 이메일 분리(mail_addr)
        String mail = src.getMailAddr();
        if (mail != null) {
            int idx = mail.indexOf('@');
            if (idx > 0) {
                vo.setEmailId(mail.substring(0, idx));
                if (idx + 1 < mail.length()) {
                    vo.setEmailDomain(mail.substring(idx + 1));
                }
            } else {
                // @ 없는 경우 통째로 앞부분으로
                vo.setEmailId(mail);
            }
        }

        // 전화번호 분리 (mngr_tel_no)
        ParamUtil.PhoneParts tel = ParamUtil.splitPhone(src.getMngrTelNo());
        if (tel != null) {
            vo.setTel1(tel.getP1());
            vo.setTel2(tel.getP2());
            vo.setTel3(tel.getP3());
        }

        // 휴대전화 분리 (mngr_mbph_no)
        ParamUtil.PhoneParts mb = ParamUtil.splitPhone(src.getMngrMbphNo());
        if (mb != null) {
            vo.setMobile1(mb.getP1());
            vo.setMobile2(mb.getP2());
            vo.setMobile3(mb.getP3());
        }

        return vo;
    }



}
