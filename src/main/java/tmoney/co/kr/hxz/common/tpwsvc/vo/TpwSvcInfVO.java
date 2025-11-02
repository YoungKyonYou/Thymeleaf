package tmoney.co.kr.hxz.common.tpwsvc.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TpwSvcInfVO {
    private String orgCd;
    private String tpwSvcId;
    private String tpwSvcNm;
    private String tpwSvcSttDt;
    private String tpwSvcEndDt;
    private String tpwSvcCtt;
    private String krnChecYn;
    private String tpwSvcUseYn;
    private String acngTrdpNo;
    private String bnkTrnCtt;

    private String tpwSvcTypId;
    private BigDecimal tpwSvcTypSno;
    private String tpwSvcTypNm;
    private String tpwSvcTypSttDt;
    private String tpwSvcTypEndDt;
    private String tpwSvcTypCtt;
    private String tpwMbrsTypCd;
    private String tpwMntnCd;
    private String tpwStlmCycDvsCd;
    private String tpwStlmCtgCd;
    private BigDecimal stlmCtgAdptVal;
    private String trnsTrdReqYn;
    private String ldgrTrdReqYn;
    private String taxiTrdReqYn;
    private String areaTrdReqYn;
    private String tpwStlmActDvsCd;
    private String tpwRsdcAuthCycCd;
    private String tpwCrovDvsCd;
    private String sprtDplcYn;
    private String tpwTrdOrgCd;
    private String trnsTrdMlprExYn;
    private String autAplYn;
    private String evdnYn;
    private String frgnSprtYn;
    private String trdNcntLtnAdptYn;
    private String tpwSvcTypUseYn;
}
