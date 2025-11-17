package tmoney.co.kr.hxz.penstlmng.aplinf.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.mapper.PenAplPtInfMapper;
import tmoney.co.kr.hxz.penstlmng.aplinf.service.PenAplPtInfService;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfReqVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfRspVO;

import java.util.List;

/**
 * 지급금신청 정보 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class PenAplPtInfServiceImpl implements PenAplPtInfService {

    private final PenAplPtInfMapper penAplPtInfMapper;


    /**
     * 지급금신청 정보 페이징 조회
     */
    @Transactional(readOnly = true)
    @Override
    public PageDataVO<PenAplPtInfRspVO> readPenAplPtInfPaging(PenAplPtInfReqVO req, String orgCd) {

        // 페이지네이션 offset 계산
        final int offset = req.getPage() * req.getSize();

        // 총 건수 조회
        long total = penAplPtInfMapper.readPenAplPtInfListCnt(req, orgCd);

        // 요청 파라미터 복사
        PenAplPtInfReqVO reqVO = new PenAplPtInfReqVO(
//                req.getBatTakDt(),
//                req.getSttDt(),
//                req.getEndDt(),
//                offset,
//                req.getSize(),
//                req.getSort(),
//                req.getDir()
        );

        // 리스트 조회
        List<PenAplPtInfRspVO> content = penAplPtInfMapper.readPenAplPtInfList(reqVO, orgCd);

        // PageDataVO 리턴
        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }



    @Override
    @Transactional(readOnly = true)
    public List<PenAplPtInfRspVO> readPenAplPtInfList(PenAplPtInfReqVO req, String orgCd) {
        return penAplPtInfMapper.readPenAplPtInfList(req, orgCd);
    }

    @Override
    @Transactional(readOnly = true)
    public long readPenAplPtInfListCnt(PenAplPtInfReqVO req, String orgCd) {
        return penAplPtInfMapper.readPenAplPtInfListCnt(req, orgCd);
    }

    @Override
    public Object readPenAplCntByMonth(String orgCd, String tpwSvcId, String tpwSvcId1) {
        return null;
    }

    @Override
    public Object readPenAplCntByDay(String sttDt, String orgCd, String tpwSvcId, String tpwSvcId1) {
        return null;
    }


}
