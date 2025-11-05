package tmoney.co.kr.hxz.spfnsprtmng.payinf.service;

import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfRspVO;

public interface StlmTakPtInfService {

    /** 1. 검색용 리스트 페이징 */
    PageDataVO<StlmTakPtInfRspVO> readStlmTakPtPaging(StlmTakPtInfReqVO req);

    /** 2. 서비스ID + 서비스번호 기준 단건 조회 (상세보기) */
    StlmTakPtInfRspVO findStlmTakPtInfByService(String tpwSvcTypId, String tpwSvcTypSno, String exeDiv);

    /** 3. 등록 */
    void saveStlmTakPtInf(StlmTakPtInfRspVO form);

    /** 4. 수정 (서비스ID + 서비스번호 기준) */
    void updateStlmTakPtInfByService(StlmTakPtInfRspVO form);
}
