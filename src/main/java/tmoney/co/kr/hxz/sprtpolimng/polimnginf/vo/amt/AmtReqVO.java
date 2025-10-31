package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class AmtReqVO {
    /** 지원금한도관리번호 */
    @Size(max = 10, message = "지원금한도관리번호의 길이는 10보다 작아야 합니다.")
    private String spfnLmtMngNo;

    /** 한도시작년월 */
    @Size(min = 6, max = 6, message = "한도시작년월의 길이는 6이어야 합니다.")
    private String lmtSttYm;

    /** 한도종료년월 */
    @Size(min = 6, max = 6, message = "한도종료년월의 길이는 6이어야 합니다.")
    private String lmtEndYm;

    /** 대상조건값 */
    @PositiveOrZero(message = "양수만 입력 가능합니다.")
    @Digits(integer = 15, fraction = 0, message = "최대 15자리 숫자만 입력 가능합니다.")
    private int tgtAdptVal;
}
