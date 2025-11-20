package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper.SimReqMngMapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimReqMngService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngRspVO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SimReqMngServiceImpl implements SimReqMngService {

    private final SimReqMngMapper simReqMngMapper;

    /**
     * 시뮬레이션 포인트 정보 페이징 조회
     */
    @Transactional(readOnly = true)
    public PageDataVO<SimReqMngRspVO> readSimReqMngPaging(SimReqMngReqVO req, String orgCd) {

        // 페이지네이션 offset 계산
        final int offset = req.getPage() * req.getSize();

        // 🔹 임시 orgCd (로컬용)
//        final String orgCd = "0000000"; // TODO: 추후 로그인 정보(orgCd) 연동 예정

        // 총 건수 조회
        long total = simReqMngMapper.readSimReqMngListCnt(req, orgCd);

        // 요청 파라미터를 생성자 방식으로 복사
        SimReqMngReqVO reqVO = new SimReqMngReqVO(
                req.getAplDt(),
                req.getSttDt(),
                req.getEndDt(),
                req.getTpwSvcId(),
                req.getTpwSvcNm(),
                req.getTpwSvcTypId(),
                req.getTpwSvcTypSno(),
                req.getTpwSvcTypNm(),
                req.getMbrsId(),
                req.getCardNo(),
                req.getPage(),
                req.getSize(),
                req.getSort(),
                req.getDir(),
                offset
        );

        // 리스트 조회
        List<SimReqMngRspVO> content = simReqMngMapper.readSimReqMngList(reqVO, orgCd);

        // PageDataVO 리턴
        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }


    @Transactional(readOnly = true)
    @Override
    public List<SimReqMngRspVO> readSimReqMngList(SimReqMngReqVO req, String orgCd) {
        return simReqMngMapper.readSimReqMngList(req, orgCd);
    }


    @Transactional(readOnly = true)
    @Override
    public long readSimReqMngListCnt(SimReqMngReqVO req, String orgCd) {
//        final String orgCd = "0000000"; // TODO: 추후 로그인 정보(orgCd) 연동 예정
        return simReqMngMapper.readSimReqMngListCnt(req, orgCd);
    }

    @Override
    @Transactional
    public void saveSimReqMng(SimReqMngRspVO form) {
        simReqMngMapper.saveSimReqMng(form);
    }
}
