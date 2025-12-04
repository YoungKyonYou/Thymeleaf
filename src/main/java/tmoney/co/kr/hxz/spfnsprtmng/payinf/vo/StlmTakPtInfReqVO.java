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
import java.util.Map;

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

    // [수정] 값이 없을 때만 Default 값을 세팅하도록 변경 (기존 값을 덮어쓰지 않음)
    public void updateDefaultDate(String startDate, String endDate) {
        if (this.sttDt == null || this.sttDt.isEmpty()) {
            this.sttDt = startDate;
        }
        if (this.endDt == null || this.endDt.isEmpty()) {
            this.endDt = endDate;
        }
    }

    public StlmTakPtInfReqVO(Map<String, String> params) {
        if ( params == null ) return;

        this.exeDiv = params.getOrDefault("exeDiv", "PERD");
        this.aplSttDt = params.get("aplSttDt");
        this.aplEndDt = params.get("aplEndDt");
        this.stlmDt = params.get("stlmDt");
        this.fixDt = params.get("fixDt");
        this.orgCd = params.get("orgCd");
        this.sttDt = params.get("sttDt");
        this.endDt = params.get("endDt");
        this.svcNm = params.get("svcNm");
        this.svcTypNm = params.get("svcTypNm");
        this.tpwSvcId = params.get("tpwSvcId");
        this.tpwSvcTypId = params.get("tpwSvcTypId");

        // BigDecimal 타입 변환
        String tpwSvcTypSnoStr = params.get("tpwSvcTypSno");
        if ( tpwSvcTypSnoStr != null && !tpwSvcTypSnoStr.isEmpty() ) {
            try {
                this.tpwSvcTypSno = new BigDecimal(tpwSvcTypSnoStr);
            } catch (NumberFormatException e) {
                // 무시하거나 기본값 null
                this.tpwSvcTypSno = null;
            }
        }

        // int 타입 변환
        String pageStr = params.get("page");
        if ( pageStr != null ) {
            try {
                this.page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                this.page = 0;
            }
        }

        String sizeStr = params.get("size");
        if ( sizeStr != null ) {
            try {
                this.size = Integer.parseInt(sizeStr);
            } catch (NumberFormatException e) {
                this.size = 10;
            }
        }

        this.sort = params.getOrDefault("sort", "stlm_dt");
        this.dir = params.getOrDefault("dir", "asc");
        this.searchType = params.getOrDefault("searchType", "stlmDt");
    }
}