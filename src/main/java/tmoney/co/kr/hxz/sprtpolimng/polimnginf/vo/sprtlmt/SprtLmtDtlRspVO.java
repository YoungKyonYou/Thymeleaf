package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SprtLmtDtlRspVO {
    private List<SprtLmtRspVO> sprtLmtDtlList;
    private List<SprtLmtReqVO> sprtLmtReqList;
    /** 교통복지한도구분코드 */
    private String tpwLmtDvsCd;
    /** 교통복지한도유형코드 */
    private String tpwLmtTypCd;
    /** 사용여부 */
    private String useYn;
}
