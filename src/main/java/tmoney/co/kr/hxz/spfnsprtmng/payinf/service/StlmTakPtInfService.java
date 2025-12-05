package tmoney.co.kr.hxz.spfnsprtmng.payinf.service;

import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfRspVO;

import java.math.BigDecimal;

public interface StlmTakPtInfService {

    /** 1. 검색용 리스트 페이징 */


    @Transactional(readOnly = true)
    PageDataVO<StlmTakPtInfRspVO> readStlmTakPtPaging(StlmTakPtInfReqVO req, String orgCd, String exeDiv);

    /** 2. 서비스ID + 서비스번호 기준 단건 조회 (상세보기) */
//    StlmTakPtInfRspVO readTakPtInf(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String exeDiv, String tpwSvcId);

    /** 3. 등록 */
    void saveStlmTakPtInf(StlmTakPtInfRspVO form);

    /** 4. 수정 (서비스ID + 서비스번호 기준) */
    void updateStlmTakPtInfByService(StlmTakPtInfRspVO form);


    StlmTakPtInfRspVO readSimTakPtInf(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String exeDiv, String tpwSvcId, String aplDt);

    StlmTakPtInfRspVO readPerdTakPtInf(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String exeDiv, String tpwSvcId, String stlmDt);
}
