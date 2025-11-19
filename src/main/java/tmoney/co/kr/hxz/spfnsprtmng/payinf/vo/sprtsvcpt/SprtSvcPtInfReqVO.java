package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * ============================================
 * SprtSvcInfReqVO
 * - 서비스 조회 요청 VO
 * - 검색 조건 + 페이징
 * ============================================
 */
@Getter
@Setter
public class SprtSvcPtInfReqVO {

    // 기관명
    private String orgNm;
    private String orgCd;

    // 지원대상
    private String tpwMbrsTypCdNm;

    // 서비스명
    private String tpwSvcNm;

    // 서비스 아이디
    private String tpwSvcId;
    private String tpwSvcTypId;

    // 서비스기간
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "서비스기간은 yyyy-MM-dd 형식이어야 합니다.")
    private String svcSttDt  =  java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).minusMonths(1).toString(); // 기본값: 오늘날짜;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "서비스기간은 yyyy-MM-dd 형식이어야 합니다.")
    private String svcEndDt = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).toString(); // 기본값: 오늘날

    @Size(max = 8, message = "신청기간 시작일자는 8자리 이하의 길이어야 합니다.")
    private String sttDt;  // 신청기간 시작일자 (YYYYMMDD)

    @Size(max = 8, message = "신청기간 종료일자는 8자리 이하의 길이어야 합니다.")
    private String endDt;  // 신청기간 종료일자 (YYYYMMDD)

    @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.")
    private int page = 0;

    @Positive(message =  "페이지 값은 0보다 커야 합니다.")
    private int size = 10;

    @Size(max = 10, message = "정랼값의 문자열 길이는  500자리 이하의 길이어야 합니다.")
    private String sort = "tpw_svc_id";
    private String dir = "asc";

    public SprtSvcPtInfReqVO(Map<String, String> map)
    {
        if (map == null)
        {
            return;
        }

        // 기관명
        this.orgNm = map.get("orgNm");
        this.orgCd = map.get("orgCd");

        // 지원대상
        this.tpwMbrsTypCdNm = map.get("tpwMbrsTypCdNm");

        // 서비스명
        this.tpwSvcNm = map.get("tpwSvcNm");

        // 서비스 아이디
        this.tpwSvcId = map.get("tpwSvcId");
        this.tpwSvcTypId = map.get("tpwSvcTypId");

        // 서비스 기간
        this.svcSttDt = map.getOrDefault("svcSttDt", this.svcSttDt);
        this.svcEndDt = map.getOrDefault("svcEndDt", this.svcEndDt);

        // 신청기간
        this.sttDt = map.get("sttDt");
        this.endDt = map.get("endDt");

        // 페이징
        try
        {
            if (map.get("page") != null)
            {
                this.page = Integer.parseInt(map.get("page"));
            }
        }
        catch (NumberFormatException ignored)
        {
            // 무시하고 기본값 사용
        }

        try
        {
            if (map.get("size") != null)
            {
                this.size = Integer.parseInt(map.get("size"));
            }
        }
        catch (NumberFormatException ignored)
        {
            // 기본값 사용
        }

        // 정렬
        this.sort = map.getOrDefault("sort", this.sort);
        this.dir = map.getOrDefault("dir", this.dir);
    }



    public Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>();

        // 기관명
        if (this.orgNm != null)
        {
            map.put("orgNm", this.orgNm);
        }

        if (this.orgCd != null)
        {
            map.put("orgCd", this.orgCd);
        }

        // 지원대상
        if (this.tpwMbrsTypCdNm != null)
        {
            map.put("tpwMbrsTypCdNm", this.tpwMbrsTypCdNm);
        }

        // 서비스명
        if (this.tpwSvcNm != null)
        {
            map.put("tpwSvcNm", this.tpwSvcNm);
        }

        // 서비스 아이디
        if (this.tpwSvcId != null)
        {
            map.put("tpwSvcId", this.tpwSvcId);
        }

        if (this.tpwSvcTypId != null)
        {
            map.put("tpwSvcTypId", this.tpwSvcTypId);
        }

        // 서비스 기간
        if (this.svcSttDt != null)
        {
            map.put("svcSttDt", this.svcSttDt);
        }

        if (this.svcEndDt != null)
        {
            map.put("svcEndDt", this.svcEndDt);
        }

        // 신청기간
        if (this.sttDt != null)
        {
            map.put("sttDt", this.sttDt);
        }

        if (this.endDt != null)
        {
            map.put("endDt", this.endDt);
        }

        // 페이징
        map.put("page", String.valueOf(this.page));
        map.put("size", String.valueOf(this.size));

        // 정렬
        if (this.sort != null)
        {
            map.put("sort", this.sort);
        }

        if (this.dir != null)
        {
            map.put("dir", this.dir);
        }

        return map;
    }



    public void updateDefaultDate(String startDate, String endDate) {
        this.sttDt = startDate;
        this.endDt = endDate;
    }
}