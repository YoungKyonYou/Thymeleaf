package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class AmtLmtModReqVO {
    List<AmtReqVO> list = new ArrayList<>();

    /** 교통복지한도유형코드 */
    @Size(max = 2, message = "교통복지한도유형코드의 길이는 2보다 작아야 합니다.")
    private String tpwLmtTypCd;
    /** 교통복지한도구분코드 */
    @Size(max = 2, message = "교통복지한도구분코드의 길이는 2보다 작아야 합니다.")
    private String tpwLmtDvsCd;
}
