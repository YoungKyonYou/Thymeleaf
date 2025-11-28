package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemrStlmPtRspVO {

    private String orgCd;        // 기관코드

    //거래일자
    private String rideDmndDt;

    //총사용금액
    private String lstTrdAmt;

    //회원ID
    private String mbrsId;

    //회원명 (조인해야함) tbhxzm101테이블의 mbrs_id 조인
    private String mbrsNm;


    private String tpwSvcId;     // 서비스ID
    private String tpwSvcTypId;  // 서비스유형ID
    private String tpwSvcTypSno; // 서비스유형일련번호

    private String acctNo;       // 이체계좌
    private String payPrcgYn;    // 지급처리여부 (Y/N)
    private String manualPrcgSta; // 수기처리상태 (01:요청, 02:승인, 03:반려)

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



    // 한도금액 (lmt_amt)
    @Size(max = 15, message = "한도금액은 15자리 이하입니다.")
    private String lmtAmt;

    // 요청업무내용 (req_duty_ctt)
    @Size(max = 2000, message = "요청내용은 2000자 이하입니다.")
    private String reqDutyCtt;

    // 거래일자 (trd_dt) - YYYYMMDD
    @Size(max = 8, message = "거래일자는 8자리입니다.")
    private String trdDt;
}