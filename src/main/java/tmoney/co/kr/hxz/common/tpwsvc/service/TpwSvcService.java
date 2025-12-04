package tmoney.co.kr.hxz.common.tpwsvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.tpwsvc.mapper.TpwSvcMapper;
import tmoney.co.kr.hxz.common.tpwsvc.service.TpwSvcService;
import tmoney.co.kr.hxz.common.tpwsvc.vo.TpwSvcDropRstVO;
import tmoney.co.kr.hxz.common.tpwsvc.vo.TpwSvcInfVO;
import tmoney.co.kr.hxz.common.tpwsvc.vo.TpwSvcVO;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TpwSvcService {
    private final TpwSvcMapper tpwSvcMapper;


    public TpwSvcDropRstVO processTpwSvcInf(List<TpwSvcInfVO> tpwSvcInfVO) {
        Map<String, List<TpwSvcInfVO>> svcMap = new LinkedHashMap<>();
        List<TpwSvcVO> tpwSvcVOList = new ArrayList<>();
        Set<String> tpwSvcIdSet = new HashSet<>();

        for (TpwSvcInfVO item : tpwSvcInfVO) {
            if (tpwSvcIdSet.add(item.getTpwSvcId())) {
                tpwSvcVOList.add(new TpwSvcVO(
                        item.getOrgCd(),
                        item.getTpwSvcId(),
                        item.getTpwSvcNm()
                ));
            }

            svcMap.computeIfAbsent(item.getTpwSvcId(), k -> new ArrayList<>()).add(
                    new TpwSvcInfVO(
                            item.getOrgCd(),
                            item.getTpwSvcId(),
                            item.getTpwSvcNm(),
                            item.getTpwSvcSttDt(),
                            item.getTpwSvcEndDt(),
                            item.getTpwSvcCtt(),
                            item.getKrnChecYn(),
                            item.getTpwSvcUseYn(),
                            item.getAcngTrdpNo(),
                            item.getBnkTrnCtt(),
                            item.getTpwSvcTypId(),
                            item.getTpwSvcTypSno(),
                            item.getTpwSvcTypNm(),
                            item.getTpwSvcTypSttDt(),
                            item.getTpwSvcTypEndDt(),
                            item.getTpwSvcTypCtt(),
                            item.getTpwMbrsTypCd(),
                            item.getTpwMntnCd(),
                            item.getTpwStlmCycDvsCd(),
                            item.getTpwStlmCtgCd(),
                            item.getStlmCtgAdptVal(),
                            item.getTrnsTrdReqYn(),
                            item.getLdgrTrdReqYn(),
                            item.getTaxiTrdReqYn(),
                            item.getAreaTrdReqYn(),
                            item.getTpwStlmActDvsCd(),
                            item.getTpwRsdcAuthCycCd(),
                            item.getTpwCrovDvsCd(),
                            item.getSprtDplcYn(),
                            item.getTpwTrdOrgCd(),
                            item.getTrnsTrdMlprExYn(),
                            item.getAutAplYn(),
                            item.getEvdnYn(),
                            item.getFrgnSprtYn(),
                            item.getTrdNcntLtnAdptYn(),
                            item.getTpwSvcTypUseYn()
                    )
            );
        }

        return new TpwSvcDropRstVO(tpwSvcVOList, svcMap);
    }

    @Transactional(readOnly = true)
    public List<TpwSvcInfVO> readTpwSvcInfList(String orgCd) {
        return tpwSvcMapper.readTpwSvcInfList(orgCd);
    }
}
