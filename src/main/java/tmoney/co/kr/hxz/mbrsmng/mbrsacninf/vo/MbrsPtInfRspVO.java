package tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MbrsPtInfRspVO {

    /* 회원 기본 정보 */
    private String mbrsId;             // 회원ID (tbhxzm101)
    private String mbrsNm;             // 회원명 (tbhxzm101)
    private String addoCd;             // 행정동코드 (tbhxzm102) - 화면 표기시 코드명 변환 필요
    private String mbrsStaCd;          // 회원상태 (tbhxzm101) 00:정상,01:휴면, 99:탈퇴

    /* 가입 서비스 정보 */
    private String tpwSvcNm;           // 서비스명 (tbhxzm201)
    private String tpwSvcTypNm;        // 서비스유형명 (tbhxzm202)
    private String mbrsSvcJoinDt;      // 서비스가입일자 (tbhxzm102)
    private String tpwMbrsSvcStaCd;    // 회원서비스상태 (tbhxzm102) 01:신청, 02:재신청, 03:반려, 04:승인, 98:자격상실, 99:해지

    /* 결제/계좌 정보 */
    private String cardNo;             // 카드번호 (tbhxzm102)
    private String bnkCd;              // 은행코드 (tbhxzm102) - 계좌번호와 함께 주로 사용됨
    private String acntNo;             // 계좌번호 (tbhxzm102)

    /* 추가 필요할 수 있는 Hidden 데이터 */
    private String tpwSvcId;           // 서비스ID
    private String tpwSvcTypId;        // 서비스유형ID
    private BigDecimal tpwSvcTypSno;
}