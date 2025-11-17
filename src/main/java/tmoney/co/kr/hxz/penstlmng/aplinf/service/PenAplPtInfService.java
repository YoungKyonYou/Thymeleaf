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

    Object readPenAplCntByMonth(String orgCd, String tpwSvcId, String tpwSvcId1);

    Object readPenAplCntByDay(@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 YYYY-MM-DD 형식이어야 합니다.") String sttDt, String orgCd, String tpwSvcId, String tpwSvcId1);
}
