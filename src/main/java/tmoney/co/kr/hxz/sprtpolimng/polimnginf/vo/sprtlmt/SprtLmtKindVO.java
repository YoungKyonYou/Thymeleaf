package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SprtLmtKindVO {
    /** 한도구분(01=금액, 02=건수) */
    private String dvsCd;
    /** 한도유형(01=월, 02=분기/건수) */
    private String lmtTypCd;
}
