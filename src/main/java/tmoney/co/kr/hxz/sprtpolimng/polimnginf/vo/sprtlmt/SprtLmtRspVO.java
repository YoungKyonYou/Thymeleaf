package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SprtLmtRspVO {
    /** 서비스ID */
    private String tpwSvcId;
    /** 서비스명 */
    private String tpwSvcNm;
    /** 서비스유형ID */
    private String tpwSvcTypId;
    /** 서비스유형명 */
    private String tpwSvcTypNm;
    /** 지원금한도관리번호 */
    private String spfnLmtMngNo;
    /** 지원금한도일련번호 */
    private String spfnLmtSno;
    /** 교통복지한도구분코드 */
    private String tpwLmtDvsCd;
    /** 교통복지한도유형코드 */
    private String tpwLmtTypCd;
    /** 한도시작년월 */
    private String lmtSttYm;
    /** 한도종료년월 */
    private String lmtEndYm;
    /** 최소조건값 */
    private int minCndtVal;
    /** 최대조건값 */
    private int maxCndtVal;
    /** 대상조건값 */
    private int tgtAdptVal;
    /** 사용여부 */
    private String useYn;
}
