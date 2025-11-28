package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper.SimReqMngMapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimReqMngService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngReqVO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SimReqMngServiceImpl implements SimReqMngService {

    private final SimReqMngMapper simReqMngMapper;

    // ============================================================
    // 조회 로직
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public PageDataVO<SimReqMngRspVO> readSimReqMngPaging(SimReqMngReqVO req, String orgCd) {
        final int offset = req.getPage() * req.getSize();
        long total = simReqMngMapper.readSimReqMngListCnt(req, orgCd);

        SimReqMngReqVO reqVO = new SimReqMngReqVO(
                req.getAplDt(), req.getSttDt(), req.getEndDt(),
                req.getTpwSvcId(), req.getTpwSvcNm(),
                req.getTpwSvcTypId(), req.getTpwSvcTypSno(), req.getTpwSvcTypNm(),
                req.getMbrsId(), req.getCardNo(),
                req.getPage(), req.getSize(),
                req.getSort(), req.getDir(), offset
        );

        List<SimReqMngRspVO> content = simReqMngMapper.readSimReqMngList(reqVO, orgCd);
        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimReqMngRspVO> readSimReqMngList(SimReqMngReqVO req, String orgCd) {
        return simReqMngMapper.readSimReqMngList(req, orgCd);
    }

    @Override
    @Transactional(readOnly = true)
    public long readSimReqMngListCnt(SimReqMngReqVO req, String orgCd) {
        return simReqMngMapper.readSimReqMngListCnt(req, orgCd);
    }

    // ============================================================
    // 등록 로직
    // ============================================================
    @Override
    @Transactional
    public void saveSimReqMng(SimReqMngRspVO form) {
        simReqMngMapper.saveSimReqSfpn(form); // 208
        simReqMngMapper.saveSimReqStlm(form); // 207
    }

    // ============================================================
    // 수정 로직 (통합)
    // ============================================================
    @Override
    @Transactional
    public void updateSimReqMng(SimReqMngRspVO form)  {
        // 1. 지원금 신청 카드 수정 (208)
        int cntSfpn = simReqMngMapper.updateSimReqSfpn(form);

        // 2. 정산 대상 카드 수정 (207)
        int cntStlm = simReqMngMapper.updateSimReqStlm(form);

        // [검증] 둘 다 수정되지 않았다면 -> 실패 처리
        if (cntSfpn == 0 && cntStlm == 0) {
            throw new RuntimeException("수정 실패: 조건에 맞는 데이터가 없거나, 이미 마감된 건입니다.");
        }
    }

    // ============================================================
    // 삭제 로직 (통합)
    // ============================================================
    @Override
    @Transactional
    public void deleteSimReqMng(SimReqMngRspVO form){
        // 1. 정산 대상 카드 삭제 (207) - 자식
        simReqMngMapper.deleteSimReqStlm(form);

        // 2. 지원금 신청 카드 삭제 (208) - 부모
        int deletedCnt = simReqMngMapper.deleteSimReqSfpn(form);

        // [검증] 삭제된 건수가 없다면 -> 실패 처리
        if (deletedCnt == 0) {
            throw new RuntimeException("삭제 실패: 이미 삭제되었거나, 마감된 건입니다.");
        }
    }
}