package tmoney.co.kr.hxz.mypage.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tmoney.co.kr.hxz.common.util.ParamUtil;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateMngrVO {
    private String mngrId;
    private String mngrName;
    private String mailAddr;
    private String mngrTelNo;
    private String mngrMbphNo;
    private String updrId;

    public static UpdateMngrVO from(String mngrId, MyPageVO src) {
        UpdateMngrVO vo = new UpdateMngrVO();

        vo.setMngrId(mngrId);
        vo.setMngrName(src.getUserName());

        // 이메일 결합
        vo.setMailAddr(
                ParamUtil.joinEmail(src.getEmailId(), src.getEmailDomain())
        );

        // 전화번호 결합
        vo.setMngrTelNo(
                ParamUtil.joinPhone(src.getTel1(), src.getTel2(), src.getTel3())
        );

        // 휴대전화번호 결합
        vo.setMngrMbphNo(
                ParamUtil.joinPhone(src.getMobile1(), src.getMobile2(), src.getMobile3())
        );

        // 수정자: 보통 현재 로그인 관리자 ID
        vo.setUpdrId(mngrId);

        return vo;
    }
}
