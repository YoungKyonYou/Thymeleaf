package tmoney.co.kr.hxz.spfnsprtmng.payinf.service;

import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngRspVO;

import javax.validation.Valid;
import java.util.List;

public interface SimReqMngService {

    // 페이징 조회
    PageDataVO<SimReqMngRspVO> readSimReqMngPaging(@Valid SimReqMngReqVO req, String orgCd);

    // 리스트 조회
    List<SimReqMngRspVO> readSimReqMngList(SimReqMngReqVO reqVO , String orgCd);

    // 총 건수 조회
    //    @Transactional(readOnly = true)
    long readSimReqMngListCnt(SimReqMngReqVO req, String orgCd);
    
    // 시뮬레이션요청등록
    void saveSimReqMng(SimReqMngRspVO form);
}
