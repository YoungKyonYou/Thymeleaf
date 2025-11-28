package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * 배치작업 포인트 정보 요청 VO
 */
@Getter
@Setter
public class BatTakPtInfReqVO {

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "작업일자는 yyyy-MM-dd 형식이어야 합니다.")
    private String batTakDt; // 작업일자

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "시작일자는 yyyy-MM-dd 형식이어야 합니다.")
    private String sttDt = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).toString(); // 기본값: 오늘날짜

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "종료일자는 yyyy-MM-dd 형식이어야 합니다.")
    private String endDt = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).toString(); // 기본값: 오늘날짜

    @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.")
    private int page = 0;

    @Positive(message = "페이지 크기는 0보다 커야 합니다.")
    private int size = 10;

    @Size(max = 30, message = "정렬값의 문자열 길이는 30보다 작아야 합니다.")
    private String sort = "bat_tak_dt";

    private String dir = "desc";

    /**
     * 기본값 날짜 세팅 메서드
     */
    public void updateDefaultDate(String startDate, String endDate) {
        this.sttDt = startDate;
        this.endDt = endDate;
    }

    /**
     * 생성자 - ServiceImpl에서 req.get() 값으로 초기화할 때 사용
     */
    public BatTakPtInfReqVO(String batTakDt, String sttDt, String endDt,
                            int page, int size, String sort, String dir) {
        this.batTakDt = batTakDt;
        this.sttDt = sttDt;
        this.endDt = endDt;
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.dir = dir;
    }

    /**
     * 기본 생성자 (MyBatis 등에서 필수)
     */
    public BatTakPtInfReqVO() {}

    // 맵 받고 생성
    public BatTakPtInfReqVO(Map<String, String> map)
    {
        if (map == null)
        {
            return;
        }

        this.batTakDt = map.get("batTakDt");
        this.sttDt    = map.getOrDefault("sttDt", this.sttDt);
        this.endDt    = map.getOrDefault("endDt", this.endDt);

        // 숫자 파싱 안전 처리
        try
        {
            this.page = map.containsKey("page") ? Integer.parseInt(map.get("page")) : this.page;
        }
        catch (NumberFormatException e)
        {
            this.page = 0;
        }

        try
        {
            this.size = map.containsKey("size") ? Integer.parseInt(map.get("size")) : this.size;
        }
        catch (NumberFormatException e)
        {
            this.size = 10;
        }

        this.sort = map.getOrDefault("sort", this.sort);
        this.dir  = map.getOrDefault("dir", this.dir);
    }

    // 맵 반환
    public Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>();

        if (this.batTakDt != null) map.put("batTakDt", this.batTakDt);
        if (this.sttDt    != null) map.put("sttDt", this.sttDt);
        if (this.endDt    != null) map.put("endDt", this.endDt);

        map.put("page", String.valueOf(this.page));
        map.put("size", String.valueOf(this.size));

        if (this.sort != null) map.put("sort", this.sort);
        if (this.dir  != null) map.put("dir", this.dir);

        return map;
    }

}
