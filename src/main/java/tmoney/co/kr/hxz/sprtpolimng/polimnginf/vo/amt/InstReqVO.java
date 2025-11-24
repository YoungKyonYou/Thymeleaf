package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class InstReqVO {
    private List<AmtReqVO> amtList = new ArrayList<>();
    private List<NcntReqVO>  ncntList = new ArrayList<>();
    private String mode;
    private String tpwSvcId;

    private String tpwSvcTypId;
    private String tpwLmtDvsCd;
    private String tpwLmtTypCd;

}
