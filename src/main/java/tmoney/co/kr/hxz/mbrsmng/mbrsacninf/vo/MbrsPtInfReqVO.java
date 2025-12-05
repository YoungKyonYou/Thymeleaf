package tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo;

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
 * 회원정보내역조회 검색 요청 VO
 * 테이블: tbhxzm101, tbhxzm102, tbhxzm201, tbhxzm202
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MbrsPtInfReqVO {

    private String orgCd;

    /** -------------------------
     * 서비스 검색 조건
     * ------------------------- */

    // 서비스ID (tbhxzm201.tpw_svc_id)
    @Size(max = 7, message = "서비스ID는 7자리 이하로 입력해야 합니다.")
    private String tpwSvcId;

    // 서비스유형ID (tbhxzm202.tpw_svc_typ_id)
    @Size(max = 10, message = "서비스유형ID는 10자리 이하로 입력해야 합니다.")
    private String tpwSvcTypId;

    // 서비스유형순번 (tbhxzm202.tpw_svc_typ_sno)
    @PositiveOrZero(message = "서비스유형 일련번호는 음수가 될 수 없습니다.")
    private Integer tpwSvcTypSno;

    /** -------------------------
     * 가입 기간 검색 (mbrs_svc_join_dt 기준)
     * ------------------------- */

    // 검색 시작일자(YYYY-MM-DD)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "가입 시작일자는 YYYY-MM-DD 형식이어야 합니다.")
    private String sttDt = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .minusMonths(1).toString();

    // 검색 종료일자(YYYY-MM-DD)
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "가입 종료일자는 YYYY-MM-DD 형식이어야 합니다.")
    private String endDt = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .toString();

    /** -------------------------
     * 회원 기본 정보 검색
     * ------------------------- */

    // 회원ID (tbhxzm101.mbrs_id)
    @Size(max = 20, message = "회원ID는 20자리 이하 입력해야 합니다.")
    private String mbrsId;

    // 회원명 (tbhxzm101.mbrs_nm)
    @Size(max = 100, message = "회원명은 100자리 이하 입력해야 합니다.")
    private String mbrsNm;

    // 회원상태코드 (tbhxzm101.mbrs_sta_cd) 00:정상,01:휴면, 99:탈퇴
    @Size(max = 2, message = "회원상태코드는 2자리 이하 입력해야 합니다.")
    private String mbrsStaCd;

    // 가입유형코드 (tbhxzm101.tpw_join_typ_cd) 01:직접, 02: 대리, 99:시스템
    @Size(max = 2, message = "가입유형코드는 2자리 이하 입력해야 합니다.")
    private String tpwJoinTypCd;

    // 행정동코드 (tbhxzm102.addo_cd)
    // 조인 필요 (tbhxzm008.addo_cd)
    @Size(max = 10, message = "행정동코드는 10자리 이하 입력해야 합니다.")
    private String addoCd;


    @Size(max = 10, message = "행정동명은 100자리 이하 입력해야 합니다.")
    private String addoNm;


    // 카드번호 (tbhxzm102.card_no)
    @Size(max = 100, message = "카드번호는 100자리 이하 입력해야 합니다.")
    private String cardNo;

    /** -------------------------
     * 페이징 및 정렬
     * ------------------------- */

    @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.")
    private int page = 0;

    @Positive(message = "size 값은 0보다 커야 합니다.")
    private int size = 10;

    // 기본 정렬: 가입일자 최신순
    @Size(max = 100, message = "정렬값은 100자 이하로 입력해야 합니다.")
    private String sort = "mbrs_svc_join_dt";

    private String dir = "desc";

    // offset 계산용
    private int offset;

    /** -------------------------
     * 날짜 기본값 초기화 로직
     * ------------------------- */
    public void updateDefaultDate(String defaultStart, String defaultEnd) {
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
    public MbrsPtInfReqVO(Map<String, Object> map) {
        this.orgCd = map.get("orgCd") != null ? map.get("orgCd").toString() : null;

        this.tpwSvcId = map.get("tpwSvcId") != null ? map.get("tpwSvcId").toString() : null;
        this.tpwSvcTypId = map.get("tpwSvcTypId") != null ? map.get("tpwSvcTypId").toString() : null;
        this.tpwSvcTypSno = map.get("tpwSvcTypSno") != null ? Integer.parseInt(map.get("tpwSvcTypSno").toString()) : null;

        this.sttDt = map.get("sttDt") != null ? map.get("sttDt").toString() : this.sttDt;
        this.endDt = map.get("endDt") != null ? map.get("endDt").toString() : this.endDt;

        this.mbrsId = map.get("mbrsId") != null ? map.get("mbrsId").toString() : null;
        this.mbrsNm = map.get("mbrsNm") != null ? map.get("mbrsNm").toString() : null;
        this.mbrsStaCd = map.get("mbrsStaCd") != null ? map.get("mbrsStaCd").toString() : null;
        this.tpwJoinTypCd = map.get("tpwJoinTypCd") != null ? map.get("tpwJoinTypCd").toString() : null;
        this.addoCd = map.get("addoCd") != null ? map.get("addoCd").toString() : null;
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
        map.put("orgCd", this.orgCd);

        map.put("tpwSvcId", this.tpwSvcId);
        map.put("tpwSvcTypId", this.tpwSvcTypId);
        map.put("tpwSvcTypSno", this.tpwSvcTypSno != null ? String.valueOf(this.tpwSvcTypSno) : null);

        map.put("sttDt", this.sttDt);
        map.put("endDt", this.endDt);

        map.put("mbrsId", this.mbrsId);
        map.put("mbrsNm", this.mbrsNm);
        map.put("mbrsStaCd", this.mbrsStaCd);
        map.put("tpwJoinTypCd", this.tpwJoinTypCd);
        map.put("addoCd", this.addoCd);
        map.put("cardNo", this.cardNo);

        map.put("page", String.valueOf(this.page));
        map.put("size", String.valueOf(this.size));
        map.put("sort", this.sort);
        map.put("dir", this.dir);
        map.put("offset", String.valueOf(this.offset));
        return map;
    }
}