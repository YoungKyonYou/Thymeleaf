package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemrStlmPtRspVO {

    //거래일자
    private String rideDmndDt;

    //총사용금액
    private String lstTrdAmt;

    //회원ID
    private String mbrsId;

    //회원명 (조인해야함) tbhxzm101테이블의 mbrs_id 조인
    private String mbrsNm;

    //회원상태
    private String tpwMbrsSvcStaCd;

    //행정동명
    private String addoNm;

    //요청일자
    private String reqDtm;

    //처리일자
    private String prcgDt;

    //처리상태
    private String tpwMemrPrcgStaCd;

    //요청금액 = 신청금액
    private String aplAmt;

    // 서비스명
    private String tpwSvcNm;

    // 서비스유형명 = 지원유형
    private String tpwSvcTypNm;

    // 서비스유형번호
    private String tpwSvcTypSno;
}