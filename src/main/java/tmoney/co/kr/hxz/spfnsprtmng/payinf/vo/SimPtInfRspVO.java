package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import javax.validation.constraints.Size;

public class SimPtInfRspVO

{


    private String trprActSqno;        // 거래내역실행순번


    private String mbrsId;             // 회원ID


    private String mbrsNm;             // 회원명


    private String cardNo;             // 카드번호


    private String trcrDvsCd;          // 교통카드구분


    private String trcrUserDvsCd;      // 교통카드사용자


    private String rideDtm;            // 승차일시


    private String rideAmt;            // 승차금액


    private String mntnCd;             // 교통수단


    private String rotNm;              // 노선명


    private String rideStnNm;          // 승차역명


    private String alghDtm;            // 하차일시


    private String alghAmt;            // 하차금액


    private String alghStnNm;          // 하차역명


    private String trrdAmt;            // 이용금액


    private String trtrGrpSno;         // 환승그룹일련번호


    private String fctt;               // 환승횟수


    private String stexRsnCd;          // 요금할인제외사유
}
