package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper.SimPtInfMapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfRspVO;

import java.util.List;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SimPtInfServiceImpl implements SimPtInfService {

    private final SimPtInfMapper simPtInfMapper;

    /**
     * 시뮬레이션 포인트 정보 페이징 조회
     */
    @Override
    @Transactional(readOnly = true)
    public PageDataVO<SimPtInfRspVO> readSimPtPaging(SimPtInfReqVO req, String orgCd) {

        // 페이지네이션 offset 계산
        final int offset = req.getPage() * req.getSize();

        // 🔹 임시 orgCd (로컬용)
//        final String orgCd = "0000000"; // TODO: 추후 로그인 정보(orgCd) 연동 예정

        // 총 건수 조회
        long total = simPtInfMapper.readSimPtListCnt(req, orgCd);

        // 요청 파라미터를 생성자 방식으로 복사
        SimPtInfReqVO reqVO = new SimPtInfReqVO(
                orgCd,
                req.getSttDt(),
                req.getEndDt(),
                req.getCardNo(),
                req.getSvcNm(),
                req.getSvcTypNm(),
                req.getTpwSvcId(),
                req.getTpwSvcTypId(),
                req.getTpwSvcTypSno(),
                offset,
                req.getPage(),
                req.getSize(),
                req.getSort(),
                req.getDir()
        );

        // 리스트 조회
        List<SimPtInfRspVO> content = simPtInfMapper.readSimPtList(reqVO, orgCd);

        // PageDataVO 리턴
        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SimPtInfRspVO> readSimPtList(SimPtInfReqVO req, String orgCd) {
        return simPtInfMapper.readSimPtList(req, orgCd);
    }


    @Transactional(readOnly = true)
    @Override
    public long readSimPtListCnt(SimPtInfReqVO req, String orgCd) {
//        final String orgCd = "0000000"; // TODO: 추후 로그인 정보(orgCd) 연동 예정
        return simPtInfMapper.readSimPtListCnt(req, orgCd);
    }
}
