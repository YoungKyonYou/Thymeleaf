package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 마감확정내역 조회 요청 VO
 * 검색조건용
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StlmTakPtInfReqVO {
    

    // 실행구분
    private String exeDiv; // 'PERD' or 'SIM' 구분 정기, 시뮬레이션
    
    /** 신청시작일 (YYYYMMDD) */
    @Size(max = 8, message = "신청일자는 8자리 이하이어야 합니다.")
    private String aplSttDt;
    
    /** 신청종료일 (YYYYMMDD) */
    @Size(max = 8, message = "신청일자는 8자리 이하이어야 합니다.")
    private String aplEndDt;
    
    


    /** 정산일자 (YYYYMMDD) */
    @Size(max = 8, message = "정산일자는 8자리 이하이어야 합니다.")
    private String stlmDt;

    /** 확정일자 (YYYYMMDD) */
    @Size(max = 8, message = "확정일자는 8자리 이하이어야 합니다.")
    private String fixDt;

    @Size(max = 7, message = "기관코드는 7자리 이하의 길이어야 합니다.")
    private String orgCd; // 기관코드

    @Size(max = 8, message = "검색 시작일자는 8자리 이하의 길이어야 합니다.")
    private String sttDt; // 검색 시작일자 (YYYYMMDD)

    @Size(max = 8, message = "검색 종료일자는 8자리 이하의 길이어야 합니다.")
    private String endDt; // 검색 종료일자 (YYYYMMDD)

    @Size(max = 500, message = "서비스명은 500자리 이하의 길이어야 합니다.")
    private String svcNm; // 서비스명

    @Size(max = 100, message = "서비스유형명은 100자리 이하의 길이어야 합니다.")
    private String svcTypNm; // 서비스유형명

    @Size(max = 7, message = "서비스ID는 7자리 이하의 길이어야 합니다.")
    private String tpwSvcId; // 서비스ID

    @Size(max = 10, message = "서비스유형ID는 10자리 이하의 길이어야 합니다.")
    private String tpwSvcTypId; // 서비스유형ID

    private BigDecimal tpwSvcTypSno; // 서비스유형일련번호

    @Size(max = 10, message = "검색유형은 10자리 이하의 길이어야 합니다.")
    private String searchType = "stlmDt"; // 검색유형 (정산일자, 확정일자, 신청일자, 거래기간 등)

    @PositiveOrZero(message = "페이지는 음수가 될 수 없습니다.")
    private int page = 0;

    @Positive(message = "페이지 크기는 0보다 커야 합니다.")
    private int size = 10;

    @Size(max = 100, message = "정렬 컬럼명은 500자리 이하이어야 합니다.")
    private String sort = "stlm_dt";

    @Size(max = 10, message = "정렬 방향은 10자리 이하이어야 합니다.")
    private String dir = "asc";

    public void updateDefaultDate(String startDate, String endDate) {
        this.sttDt = startDate;
        this.endDt = endDate;
    }
}
