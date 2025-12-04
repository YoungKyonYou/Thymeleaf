package tmoney.co.kr.hxz.penstlmng.aplinf.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
//@RequiredArgsConstructor // final 필드가 없으므로 @NoArgsConstructor, @AllArgsConstructor와 병행 사용이 필요
@NoArgsConstructor // Lombok의 NoArgsConstructor를 가정
@AllArgsConstructor

/**
 * ============================================
 * PenAplPtInfReqVO
 * - 지급금신청 목록 조회 Request VO
 * ============================================
 */
public class PenAplPtInfReqVO {


    /** 기관코드 */
    @Size(max = 7, message = "기관코드는 7 이하의 길이여야 합니다.")
    private String orgCd;

    /** 정산일자  (startDate, endDate) **/
    // 정산일자
    @Size(max = 8, message = "정산일자  8 이하의 길이여야 합니다.")
    private String stlmDt;

    /** 승인상태코드 */
    @Size(max = 2, message = "승인상태코드 2 이하의 길이여야 합니다.")
    private String aprvStaCd;

    /** 회원ID */
    @Size(max = 20, message = "회원id는 20 이하의 길이여야 합니다.")
    private String mbrsId;


    /** 서비스명 */
    private String tpwSvcNm;

    /** 서비스id */
    private String tpwSvcId;

    /** 서비스유형명 */
    private String tpwSvcTypNm;

    // 서비스유형코드 서비스유형관리 서비스 카테고리
    @Size(max = 10, message = "교통복지서비스유형코드는 10 이하의 길이여야 합니다.")
    private String tpwSvcTypId;

    // 서비스유형번호
    private int tpwSvcTypSno;

    // 시작기간
    // * 기본값 빼야함
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD 형식이어야 합니다.") // 검색시작일
    private String sttDt;
    // private String sttDt = LocalDate.now(ZoneId.of("Asia/Seoul")).minusMonths(1).toString();

    // * 기본값 빼야함
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD 형식이어야 합니다.") // 검색종료일
    private String endDt;
    // private String endDt = LocalDate.now(ZoneId.of("Asia/Seoul")).toString();

    @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.")
    private int page = 0;

    @Positive(message = "페이지 값은 0보다 커야 합니다.")
    private int size = 10;

    @Size(max = 100, message = "정렬값은 mbrs_id, 100이하 입니다.")
    private String sort = "mbrs_id"; // 기본값
    private String dir = "asc";
    
    // 페이징에 필요한 필드
    private int offset;

    public PenAplPtInfReqVO(@Size(max = 7, message = "기관코드는 7 이하의 길이여야 합니다.") String orgCd, @Size(max = 8, message = "정산일자  8 이하의 길이여야 합니다.") String stlmDt, @Size(max = 2, message = "승인상태코드 2 이하의 길이여야 합니다.") String aprvStaCd, @Size(max = 20, message = "회원id는 20 이하의 길이여야 합니다.") String mbrsId, String tpwSvcNm, String tpwSvcId, String tpwSvcTypNm, @Size(max = 10, message = "교통복지서비스유형코드는 10 이하의 길이여야 합니다.") String tpwSvcTypId, int tpwSvcTypSno, @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD 형식이어야 합니다.") String sttDt, @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD 형식이어야 합니다.") String endDt, @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.") int page, @Positive(message = "페이지 값은 0보다 커야 합니다.") int size, @Size(max = 100, message = "정렬값은 mbrs_id, 100이하 입니다.") String sort, String dir) {
    }


    // 값이 없을때만 채우게
    public void updateDefaultDate(String startDate, String endDate) {
        if (this.sttDt == null || this.sttDt.trim().isEmpty())
            this.sttDt = startDate;
        if (this.endDt == null || this.endDt.trim().isEmpty())
            this.endDt = endDate;
    }


    public PenAplPtInfReqVO(Map<String, String> map)
    {
        if (map == null)
        {
            return;
        }

        this.orgCd = map.get("orgCd");
        this.stlmDt = map.get("stlmDt");
        this.aprvStaCd = map.get("aprvStaCd");
        this.mbrsId = map.get("mbrsId");

        this.tpwSvcNm = map.get("tpwSvcNm");
        this.tpwSvcId = map.get("tpwSvcId");
        this.tpwSvcTypNm = map.get("tpwSvcTypNm");
        this.tpwSvcTypId = map.get("tpwSvcTypId");

        try
        {
            if (map.get("tpwSvcTypSno") != null)
            {
                this.tpwSvcTypSno = Integer.parseInt(map.get("tpwSvcTypSno"));
            }
        }
        catch (NumberFormatException ignored)
        {
        }

        this.sttDt = map.getOrDefault("sttDt", this.sttDt);
        this.endDt = map.getOrDefault("endDt", this.endDt);

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
        }

        // 정렬
        this.sort = map.getOrDefault("sort", this.sort);
        this.dir = map.getOrDefault("dir", this.dir);
    }



    public Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>();

        if (this.orgCd != null)
        {
            map.put("orgCd", this.orgCd);
        }

        if (this.stlmDt != null)
        {
            map.put("stlmDt", this.stlmDt);
        }

        if (this.aprvStaCd != null)
        {
            map.put("aprvStaCd", this.aprvStaCd);
        }

        if (this.mbrsId != null)
        {
            map.put("mbrsId", this.mbrsId);
        }

        if (this.tpwSvcNm != null)
        {
            map.put("tpwSvcNm", this.tpwSvcNm);
        }

        if (this.tpwSvcId != null)
        {
            map.put("tpwSvcId", this.tpwSvcId);
        }

        if (this.tpwSvcTypNm != null)
        {
            map.put("tpwSvcTypNm", this.tpwSvcTypNm);
        }

        if (this.tpwSvcTypId != null)
        {
            map.put("tpwSvcTypId", this.tpwSvcTypId);
        }

        map.put("tpwSvcTypSno", String.valueOf(this.tpwSvcTypSno));

        if (this.sttDt != null)
        {
            map.put("sttDt", this.sttDt);
        }

        if (this.endDt != null)
        {
            map.put("endDt", this.endDt);
        }

        map.put("page", String.valueOf(this.page));
        map.put("size", String.valueOf(this.size));
        map.put("sort", this.sort);
        map.put("dir", this.dir);

        return map;
    }



}