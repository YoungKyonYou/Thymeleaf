package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.*;
//import org.hibernate.validator.constraints.time.Pattern; // javax.validation.constraints.Pattern으로 대체될 수 있음
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
public class MemrStlmPtReqVO {



    /** 요청기간 (startDate, endDate) **/
    // 요청일자
    @Size(max = 8, message = "요청일자는 8 이하의 길이여야 합니다.")
    private String reqDtm;

    // 처리일자
    @Size(max = 8, message = "처리일자는 8 이하의 길이여야 합니다.")
    private String prcgDt;

    private String searchType; // 검색정렬조건
    private String keyword; // 검색어

    // 행정동
    @Size(max = 100, message = "행정동명은 100 이하의 길이여야 합니다.")
    private String addoNm;

    // 처리상태 코드
    @Size(max = 2, message = "처리상태코드는 2 이하의 길이여야 합니다.")
    private String tpwMemrPrcgStaCd;

    // 회원id
    @Size(max = 20, message = "회원id는 20 이하의 길이여야 합니다.")
    private String mbrsId;

    // 기관코드
    @Size(max = 7, message = "기관코드는 7 이하의 길이여야 합니다.")
    private String orgCd;

    // 회원명
    @Size(max = 100, message = "회원명은 100 이하의 길이여야 합니다.")
    private String mbrsNm;

    /** 서비스명 */
    private String tpwSvcNm;

    // 서비스버전
    private String tpwSvcId;

    /** 서비스유형명 */
    private String tpwSvcTypNm;

    // 서비스유형코드 서비스유형관리 서비스 카테고리
    @Size(max = 10, message = "교통복지서비스유형코드는 10 이하의 길이여야 합니다.")
    private String tpwSvcTypId;

    // 서비스유형번호
    private int tpwSvcTypSno;

    // 시작기간
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD 형식이어야 합니다.") // 검색시작일
    private String sttDt = LocalDate.now(ZoneId.of("Asia/Seoul")).minusMonths(1).toString();

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD 형식이어야 합니다.") // 검색종료일
    private String endDt = LocalDate.now(ZoneId.of("Asia/Seoul")).toString();

    @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.")
    private int page = 0;

    @Positive(message = "페이지 값은 0보다 커야 합니다.")
    private int size = 10;

    @Size(max = 100, message = "정렬값은 mbrs_id, 100이하 입니다.")
    private String sort = "mbrs_id"; // 기본값
    private String dir = "asc";
    
    // 페이징에 필요한 필드
    private int offset;

    public MemrStlmPtReqVO(@Size(max = 8, message = "요청일자는 8 이하의 길이여야 합니다.") String reqDtm, @Size(max = 8, message = "처리일자는 8 이하의 길이여야 합니다.") String prcgDt, String searchType, String keyword, @Size(max = 7, message = "기관코드는 7 이하의 길이여야 합니다.") String orgCd) {
    }

    public void updateDefaultDate(String startDate, String endDate) {
        this.sttDt = startDate;
        this.endDt = endDate;
    }

      /** Map으로 생성자 */
    public MemrStlmPtReqVO(Map<String, Object> map) {
        if ( map == null ) return;

        this.reqDtm = (String) map.get("reqDtm");
        this.prcgDt = (String) map.get("prcgDt");
        this.searchType = (String) map.get("searchType");
        this.keyword = (String) map.get("keyword");
        this.addoNm = (String) map.get("addoNm");
        this.tpwMemrPrcgStaCd = (String) map.get("tpwMemrPrcgStaCd");
        this.mbrsId = (String) map.get("mbrsId");
        this.orgCd = (String) map.get("orgCd");
        this.mbrsNm = (String) map.get("mbrsNm");
        this.tpwSvcNm = (String) map.get("tpwSvcNm");
        this.tpwSvcId = (String) map.get("tpwSvcId");
        this.tpwSvcTypNm = (String) map.get("tpwSvcTypNm");
        this.tpwSvcTypId = (String) map.get("tpwSvcTypId");

        Object sno = map.get("tpwSvcTypSno");
        if ( sno instanceof Number ) {
            this.tpwSvcTypSno = ((Number) sno).intValue();
        } else if ( sno instanceof String ) {
            this.tpwSvcTypSno = Integer.parseInt((String) sno);
        }

        this.sttDt = (String) map.getOrDefault("sttDt", this.sttDt);
        this.endDt = (String) map.getOrDefault("endDt", this.endDt);

        Object pageObj = map.get("page");
        if ( pageObj instanceof Number ) {
            this.page = ((Number) pageObj).intValue();
        }

        Object sizeObj = map.get("size");
        if ( sizeObj instanceof Number ) {
            this.size = ((Number) sizeObj).intValue();
        }

        this.sort = (String) map.getOrDefault("sort", this.sort);
        this.dir = (String) map.getOrDefault("dir", this.dir);
        Object offsetObj = map.get("offset");
        if ( offsetObj instanceof Number ) {
            this.offset = ((Number) offsetObj).intValue();
        }
    }


    /** VO를 Map<String, String>으로 변환 */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("reqDtm", this.reqDtm);
        map.put("prcgDt", this.prcgDt);
        map.put("searchType", this.searchType);
        map.put("keyword", this.keyword);
        map.put("addoNm", this.addoNm);
        map.put("tpwMemrPrcgStaCd", this.tpwMemrPrcgStaCd);
        map.put("mbrsId", this.mbrsId);
        map.put("orgCd", this.orgCd);
        map.put("mbrsNm", this.mbrsNm);
        map.put("tpwSvcNm", this.tpwSvcNm);
        map.put("tpwSvcId", this.tpwSvcId);
        map.put("tpwSvcTypNm", this.tpwSvcTypNm);
        map.put("tpwSvcTypId", this.tpwSvcTypId);
        map.put("tpwSvcTypSno", String.valueOf(this.tpwSvcTypSno));
        map.put("sttDt", this.sttDt);
        map.put("endDt", this.endDt);
        map.put("page", String.valueOf(this.page));
        map.put("size", String.valueOf(this.size));
        map.put("sort", this.sort);
        map.put("dir", this.dir);
        map.put("offset", String.valueOf(this.offset));

        return map;
    }

}