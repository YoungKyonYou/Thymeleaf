package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class SprtLmtPkVO {
    /** 서비스ID */
    private String tpwSvcId;
    /** 서비스유형ID */
    private String tpwSvcTypId;
    /** 지원금한도관리번호 */
    private String spfnLmtMngNo;
    /** 지원금한도일련번호 */
    private String spfnLmtSno;
    /** 교통복지한도구분코드 */
    private String tpwLmtDvsCd;
}
