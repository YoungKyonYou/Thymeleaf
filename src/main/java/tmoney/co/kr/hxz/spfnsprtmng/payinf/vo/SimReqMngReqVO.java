package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.*;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * 시뮬레이션 마감확정내역 검색 VO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimReqMngReqVO {

    /** -------------------------
     *  기간 검색
     *  ------------------------- */

    // 단일 신청일자
    @Size(max = 8, message = "신청일자는 8자리 이하로 입력해야 합니다.")
    private String aplDt;

    // 시작일자(YYYY-MM-DD)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "검색 시작일자는 YYYY-MM-DD 형식이어야 합니다.")
    private String sttDt = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .minusMonths(1).toString();

    // 종료일자(YYYY-MM-DD)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "검색 종료일자는 YYYY-MM-DD 형식이어야 합니다.")
    private String endDt = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .toString();


    /** -------------------------
     *  서비스 검색 영역
     *  ------------------------- */

    // 서비스ID
    @Size(max = 7, message = "서비스ID는 7자리 이하로 입력해야 합니다.")
    private String tpwSvcId;

    // 서비스명
    @Size(max = 500, message = "서비스명은 500자 이하로 입력해야 합니다.")
    private String tpwSvcNm;

    // 서비스유형ID
    @Size(max = 10, message = "서비스유형ID는 10자리 이하로 입력해야 합니다.")
    private String tpwSvcTypId;

    // 서비스유형순번
    @PositiveOrZero(message = "서비스유형 일련번호는 음수가 될 수 없습니다.")
    private Integer tpwSvcTypSno;

    // 서비스유형명
    @Size(max = 100, message = "서비스유형명은 100자 이하로 입력해야 합니다.")
    private String tpwSvcTypNm;

    /** -------------------------
     *  회원/카드 검색
     *  ------------------------- */

    @Size(max = 20, message = "회원ID는 20자리 이하 입력해야 합니다.")
    private String mbrsId;

    @Size(max = 100, message = "카드번호는 100자리 이하 입력해야 합니다.")
    private String cardNo;


    /** -------------------------
     *  페이징 및 정렬
     *  ------------------------- */

    @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.")
    private int page = 0;

    @Positive(message = "size 값은 0보다 커야 합니다.")
    private int size = 10;

    @Size(max = 100, message = "정렬값은 100자 이하로 입력해야 합니다.")
    private String sort = "apl_dt";

    private String dir = "desc";

    // offset 계산용
    private int offset;

    /** -------------------------
     *  날짜 기본값 초기화
     *  ------------------------- */
    public void updateDefaultDate(String defaultStart, String defaultEnd) {
        // 기존 값이 없을 때만 들어온 기본값으로 채움
        if (this.sttDt == null || this.sttDt.isEmpty()) {
            this.sttDt = defaultStart;
        }
        if (this.endDt == null || this.endDt.isEmpty()) {
            this.endDt = defaultEnd;
        }
    }


    /** -------------------------
     * Map 기반 생성자
     * ------------------------- */
    public SimReqMngReqVO(Map<String, Object> map) {
        this.aplDt = map.get("aplDt") != null ? map.get("aplDt").toString() : null;
        this.sttDt = map.get("sttDt") != null ? map.get("sttDt").toString() : this.sttDt;
        this.endDt = map.get("endDt") != null ? map.get("endDt").toString() : this.endDt;

        this.tpwSvcId = map.get("tpwSvcId") != null ? map.get("tpwSvcId").toString() : null;
        this.tpwSvcNm = map.get("tpwSvcNm") != null ? map.get("tpwSvcNm").toString() : null;
        this.tpwSvcTypId = map.get("tpwSvcTypId") != null ? map.get("tpwSvcTypId").toString() : null;
        this.tpwSvcTypSno = map.get("tpwSvcTypSno") != null ? Integer.parseInt(map.get("tpwSvcTypSno").toString()) : null;
        this.tpwSvcTypNm = map.get("tpwSvcTypNm") != null ? map.get("tpwSvcTypNm").toString() : null;

        this.mbrsId = map.get("mbrsId") != null ? map.get("mbrsId").toString() : null;
        this.cardNo = map.get("cardNo") != null ? map.get("cardNo").toString() : null;

        this.page = map.get("page") != null ? Integer.parseInt(map.get("page").toString()) : this.page;
        this.size = map.get("size") != null ? Integer.parseInt(map.get("size").toString()) : this.size;
        this.sort = map.get("sort") != null ? map.get("sort").toString() : this.sort;
        this.dir = map.get("dir") != null ? map.get("dir").toString() : this.dir;
        this.offset = map.get("offset") != null ? Integer.parseInt(map.get("offset").toString()) : this.offset;
    }

    /** -------------------------
     * VO -> Map 변환
     * ------------------------- */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("aplDt", this.aplDt);
        map.put("sttDt", this.sttDt);
        map.put("endDt", this.endDt);

        map.put("tpwSvcId", this.tpwSvcId);
        map.put("tpwSvcNm", this.tpwSvcNm);
        map.put("tpwSvcTypId", this.tpwSvcTypId);
        map.put("tpwSvcTypSno", this.tpwSvcTypSno != null ? String.valueOf(this.tpwSvcTypSno) : null);
        map.put("tpwSvcTypNm", this.tpwSvcTypNm);

        map.put("mbrsId", this.mbrsId);
        map.put("cardNo", this.cardNo);

        map.put("page", String.valueOf(this.page));
        map.put("size", String.valueOf(this.size));
        map.put("sort", this.sort);
        map.put("dir", this.dir);
        map.put("offset", String.valueOf(this.offset));
        return map;
    }
}
