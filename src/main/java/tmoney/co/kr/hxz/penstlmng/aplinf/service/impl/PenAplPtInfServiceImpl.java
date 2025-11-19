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
                req.getOrgCd(),       // 기관코드
                req.getStlmDt(),      // 정산일자
                req.getAprvStaCd(),   // 승인상태코드
                req.getMbrsId(),      // 회원ID
                req.getTpwSvcNm(),    // 서비스명
                req.getTpwSvcId(),    // 서비스ID
                req.getTpwSvcTypNm(), // 서비스유형명
                req.getTpwSvcTypId(), // 서비스유형코드
                req.getTpwSvcTypSno(),// 서비스유형번호
                req.getSttDt(),       // 시작기간
                req.getEndDt(),       // 종료기간
                req.getPage(),        // 페이지
                req.getSize(),        // 페이지 사이즈
                req.getSort(),        // 정렬
                req.getDir()          // 정렬 방향
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




    @Transactional(readOnly = true)
    @Override
    public List<PenAplPtInfRspVO> readPenAplCntByMonth(PenAplPtInfReqVO req) {
        return penAplPtInfMapper.readPenAplCntByMonth(req);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PenAplPtInfRspVO> readPenAplCntByDay(PenAplPtInfReqVO req) {
        return penAplPtInfMapper.readPenAplCntByDay(req);
    }

    @Override
    public void updateApprove(PenAplPtInfRspVO form) {
        penAplPtInfMapper.updateApprove(form);
    }


}
