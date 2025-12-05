package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper.BatTakPtInfMapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.BatTakPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfRspVO;

import java.util.List;

/**
 * 배치작업 포인트 정보 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class BatTakPtInfServiceImpl implements BatTakPtInfService {

    private final BatTakPtInfMapper batTakPtInfMapper;


    /**
     * 배치작업 포인트 정보 페이징 조회
     */
    @Transactional(readOnly = true)
    @Override
    public PageDataVO<BatTakPtInfRspVO> readBatTakPtPaging(BatTakPtInfReqVO req, String orgCd) {

        // 페이지네이션 offset 계산
        final int offset = req.getPage() * req.getSize();

        // 총 건수 조회
        long total = batTakPtInfMapper.readBatTakPtInfListCnt(req, orgCd);

        // 요청 파라미터 복사
        BatTakPtInfReqVO reqVO = new BatTakPtInfReqVO(
                req.getBatTakDt(),
                req.getSttDt(),
                req.getEndDt(),
                req.getPage(),
                req.getSize(),
                req.getSort(),
                req.getDir()
        );

        // 리스트 조회
        List<BatTakPtInfRspVO> content = batTakPtInfMapper.readBatTakPtInfList(reqVO, orgCd);

        // PageDataVO 리턴
        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatTakPtInfRspVO> readBatTakPtInfList(BatTakPtInfReqVO req, String orgCd) {
        return batTakPtInfMapper.readBatTakPtInfList(req, orgCd);
    }

    @Override
    @Transactional(readOnly = true)
    public long readBatTakPtInfListCnt(BatTakPtInfReqVO req, String orgCd) {
        return batTakPtInfMapper.readBatTakPtInfListCnt(req, orgCd);
    }


}
