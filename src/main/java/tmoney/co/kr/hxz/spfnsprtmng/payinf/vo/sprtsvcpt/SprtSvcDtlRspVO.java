package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================
 * SprtSvcDtlRspVO
 * - 201 서비스 단건 상세보기 VO
 * - 하위 202 리스트 포함 (svcTypList)
 * ============================================
 */
@Getter
@Setter
public class SprtSvcDtlRspVO  {

    // 지원서비스내역 상세관리 목록 조회

    // 서비스 기본정보
    private String orgCd;
    private String tpwSvcId;
    private String tpwSvcNm;
    private String tpwSvcCtt;  /** 서비스 내용 */
    private String tpwSvcSttDt;
    private String tpwSvcEndDt;
    private String krnChecYn;       // KRN 체크 여부
    private String useYn;   /** 사용 여부 */

    // 기관명 등 부가정보
    private String tpwOrgNm;

    private String acngTrdpNo;      // 회계 거래처번호
    private String bnkTrnCtt;       // 은행 계좌/거래 정보


    /** 등록자 ID */
    private String rgsrId;

    /** 등록 일시 */
    private String rgtDtm;

    /** 수정자 ID */
    private String updrId;

    /** 수정 일시 */
    private String updDtm;


    // 하위유형목록 (tbhxzm202)
    private List<SprtSvcTypRspVO> svcTypList = new ArrayList<>();


    public void setTpwSvcId(String tpwSvcId) {
        this.tpwSvcId = tpwSvcId;
    }
}
