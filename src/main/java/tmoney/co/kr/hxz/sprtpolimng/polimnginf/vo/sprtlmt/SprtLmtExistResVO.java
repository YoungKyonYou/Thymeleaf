package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SprtLmtExistResVO {
    /** 선택한 서비스+서비스유형에 기존 한도가 있는지 여부 */
    private boolean exists;

    /** 선택한 서비스+서비스유형의 기존 분기 목록 */
    private List<QuarterRangeVO> qtRanges;

    /** 서비스 단위(모든 서비스유형 통틀어)의 한도구분코드 (01=금액, 02=건수) */
    private String svcLmtDvsCd;

    /** 서비스 단위 한도유형코드 (01=월, 02=분기/건수) */
    private String svcLmtTypCd;

    /** 한 서비스 안에서 서로 다른 조합이 2개 이상 존재하는지 (데이터가 이미 꼬인 상태) */
    private boolean multiKinds;
}
