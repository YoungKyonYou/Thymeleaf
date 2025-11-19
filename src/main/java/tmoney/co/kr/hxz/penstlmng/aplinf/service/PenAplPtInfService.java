package tmoney.co.kr.hxz.penstlmng.aplinf.service;

import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfRspVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfReqVO;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 지급금신청 정보 서비스
 */
public interface PenAplPtInfService {

    /**
     * 지급금신청 정보 페이징 조회
     */
    @Transactional(readOnly = true)
    PageDataVO<PenAplPtInfRspVO> readPenAplPtInfPaging(@Valid PenAplPtInfReqVO req, String orgCd);

    /**
     * 지급금신청 정보 리스트 조회
     */
    @Transactional(readOnly = true)
    List<PenAplPtInfRspVO> readPenAplPtInfList(PenAplPtInfReqVO req, String orgCd);


    /**
     * 지급금신청 정보 총 건수 조회
     */
    @Transactional(readOnly = true)
    long readPenAplPtInfListCnt(PenAplPtInfReqVO req, String orgCd);


    @Transactional(readOnly = true)
    List<PenAplPtInfRspVO> readPenAplCntByMonth(PenAplPtInfReqVO req);

    @Transactional(readOnly = true)
    List<PenAplPtInfRspVO> readPenAplCntByDay(PenAplPtInfReqVO req);

    void updateApprove(PenAplPtInfRspVO form);
}
