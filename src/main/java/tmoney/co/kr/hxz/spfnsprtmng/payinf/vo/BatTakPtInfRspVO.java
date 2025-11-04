package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시뮬레이션 이력 응답 VO
 * 테이블 : tbhxzd200
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BatTakPtInfRspVO {

    /** 작업일자 (yyyy-MM-dd) */
    private String batTakDt;

    /** 작업ID */
    private String batTakId;

    /** 배치유형코드 */
    private String tpwBatTypCd;

    /** 배치시작일시 (yyyyMMddHHmmss) */
    private String batTakSttDtm;

    /** 배치명 */
    private String batTakNm;

    /** 배치종료일시 (yyyyMMddHHmmss) */
    private String batTakEndDtm;

    /** 처리건수 */
    private Integer prcgNcnt;

    /** 배치처리상태코드 */
    private String batPrcgStaCd;

    /** 등록자ID */
    private String rgsrId;

    /** 등록일시 (yyyyMMddHHmmss) */
    private String rgtDtm;

    /** 수정자ID */
    private String updrId;

    /** 수정일시 (yyyyMMddHHmmss) */
    private String updDtm;

}
