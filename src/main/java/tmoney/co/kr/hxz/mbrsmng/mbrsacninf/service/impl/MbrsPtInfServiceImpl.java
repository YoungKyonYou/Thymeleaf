package tmoney.co.kr.hxz.mbrsmng.mbrsacninf.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.mapper.MbrsPtInfMapper;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.service.MbrsPtInfService;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfReqVO;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfRspVO;

import java.util.List;

/**
 * 배치작업 포인트 정보 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class MbrsPtInfServiceImpl implements MbrsPtInfService {

    private final MbrsPtInfMapper mbrsPtInfMapper;


    /**
     * 배치작업 포인트 정보 페이징 조회
     */
    @Transactional(readOnly = true)
    @Override
    public PageDataVO<MbrsPtInfRspVO> readMbrsPtInfPaging(MbrsPtInfReqVO req, String orgCd) {

        int offset = req.getPage() * req.getSize();
        if (offset < 0) offset = 0;

        req.setOffset(offset);

        long total = mbrsPtInfMapper.readMbrsPtInfListCnt(req, orgCd);
        List<MbrsPtInfRspVO> content = mbrsPtInfMapper.readMbrsPtInfList(req, orgCd);

        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MbrsPtInfRspVO> readMbrsPtInfList(MbrsPtInfReqVO req, String orgCd) {
        return mbrsPtInfMapper.readMbrsPtInfList(req, orgCd);
    }

    @Override
    @Transactional(readOnly = true)
    public long readMbrsPtInfListCnt(MbrsPtInfReqVO req, String orgCd) {
        return mbrsPtInfMapper.readMbrsPtInfListCnt(req, orgCd);
    }



}
