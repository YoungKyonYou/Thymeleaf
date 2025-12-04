package tmoney.co.kr.hxz.spfnsprtmng.payinf.service;

import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfRspVO;

import javax.validation.Valid;
import java.util.List;

/**
 * 배치작업 포인트 정보 서비스
 */
public interface BatTakPtInfService {

    /**
     * 배치작업 포인트 정보 페이징 조회
     */
    @Transactional(readOnly = true)
    PageDataVO<BatTakPtInfRspVO> readBatTakPtPaging(@Valid BatTakPtInfReqVO req, String orgCd);

    /**
     * 배치작업 포인트 정보 리스트 조회
     */
    @Transactional(readOnly = true)
    List<BatTakPtInfRspVO> readBatTakPtInfList(BatTakPtInfReqVO req, String orgCd);

    /**
     * 배치작업 포인트 정보 총 건수 조회
     */
    @Transactional(readOnly = true)
    long readBatTakPtInfListCnt(BatTakPtInfReqVO req, String orgCd);
}
