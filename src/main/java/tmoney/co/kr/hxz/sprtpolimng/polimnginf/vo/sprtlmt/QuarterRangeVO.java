package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class QuarterRangeVO {
    private String lmtSttYm; // "2025-01" 또는 "202501" 중 하나로 통일
    private String lmtEndYm;
}
