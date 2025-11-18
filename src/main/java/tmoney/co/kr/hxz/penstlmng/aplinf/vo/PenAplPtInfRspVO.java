package tmoney.co.kr.hxz.penstlmng.aplinf.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

/**
 * ============================================
 * PenAplPtInfRspVO
 * - 지원금신청 목록 조회 Response VO
 * ============================================
 */

public class PenAplPtInfRspVO {

    /** 정산일자 */
    private String stlmDt;

    /** 회원ID */
    private String mbrsId;

    /** 신청일자 */
    private String aplDt;

    /** 카드번호 */
    private String cardNo;

    /** 은행코드 */
    private String bnkCd;

    /** 계좌번호 */
    private String acntNo;

    /** 예금주명 */
    private String ooaNm;

    /** 승인자ID */
    private String aproId;

    /** 승인일시 */
    private String aprvDtm;

    /** 승인상태코드 */
    private String aprvStaCd;

    /** 지원대상유형 */
    private String tpwMbrsTypCd;

    /** 첨부파일관리번호 */
    private Long atflMngNo;

    /** 신청진행상태 */
    private String tpwAplPrgsStaCd;


    //서비스id
    private String tpwSvcId;

    // 서비스명
    private String tpwSvcNm;

    // 서비스유형id
    private String tpwSvcTypId;

    // 서비스유형명 = 지원유형
    private String tpwSvcTypNm;

    // 서비스유형번호
    private BigDecimal tpwSvcTypSno;

    /* ============================
     * 월별 통계용 필드
     * ============================ */

    /** 신청년월(YYYY-MM) */
    private String aplYm;

    /** 월별 신청건수 */
    private Integer aplCnt;


    /* ============================
     * 일별 통계용 필드
     * ============================ */

    /** 신청일자(DD) */
    private String aplDay;

    /** 일별 신청건수 */
    private Integer aplCntDay;


}