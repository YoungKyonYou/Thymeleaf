package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.*;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.ZoneId;

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
    public void updateDefaultDate(String startDate, String endDate) {
        this.sttDt = startDate;
        this.endDt = endDate;
    }

}
