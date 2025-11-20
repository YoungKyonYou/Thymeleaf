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
    List<AmtReqVO> amtList = new ArrayList<>();
    List<NcntReqVO>  ncntList = new ArrayList<>();

    private String tpwSvcId;

    private String tpwSvcTypId;
    private String tpwLmtDvsCd;
    private String tpwLmtTypCd;

}
