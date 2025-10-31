package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class SprtLmtSrchReqVO {
    /** 교통복지서비스명 */
    @Size(min = 0, max = 500, message = "교통복지서비스명은 0에서 500 이하의 길이어야 합니다.")
    private String tpwSvcNm;
    /** 사용여부 */
    @Size(min = 0, max = 1, message = "사용여부는 0에서 1 이하의 길이어야 합니다.")
    private String useYn = "Y";
    /** 교통복지한도구분코드 */
    @Size(min = 0, max = 2, message = "교통복지한도구분코드는 0에서 2 이하의 길이어야 합니다.")
    private String tpwLmtDvsCd = "01";
    @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.")
    private int page = 0;
    @Positive(message = "페이지 값은 0보다 커야 합니다.")
    private int size = 10;
    @Size(max = 15, message = "정렬값의 문자열 길이는 8보다 작아야 합니다.")
    private String sort = "tpw_svc_typ_id";

    private String dir = "asc";
}
