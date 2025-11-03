package tmoney.co.kr.hxz.spfnsprtmng.payinf.service;

import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfRspVO;

import javax.validation.Valid;
import java.util.List;

public interface SimPtInfService {

    // 페이징 조회
    PageDataVO<SimPtInfRspVO> readSimPtPaging(@Valid SimPtInfReqVO req, String orgCd);

    // 리스트 조회
    List<SimPtInfRspVO> readSimPtList(SimPtInfReqVO reqVO , String orgCd);

    // 총 건수 조회
    //    @Transactional(readOnly = true)
    long readSimPtListCnt(SimPtInfReqVO reqVO, String orgCd);
}
