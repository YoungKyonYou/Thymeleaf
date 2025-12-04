package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SprtLmtModalDtlVO {
    private List<AmtReqVO> qt;
    private List<AmtReqVO> mon;
    private List<NcntReqVO> arr;
    private String dvsCd;
    private String typCd;
}
