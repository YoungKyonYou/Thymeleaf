package tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper;

import org.apache.ibatis.annotations.Mapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfRspVO;

import java.util.List;

@Mapper
public interface StlmTakPtInfMapper {

    /** ------------------------------
     * 검색용 리스트 조회
     * ------------------------------ */
    List<StlmTakPtInfRspVO> readStlmTakPtInfList(StlmTakPtInfReqVO req);

    /** 리스트 카운트 */
    int StlmTakPtInfListCnt(StlmTakPtInfReqVO req);

    /** ------------------------------
     * 상세조회 (PERD/SIM 테이블)
     * ------------------------------ */
    StlmTakPtInfRspVO findPerdStlmTakPtByService(String tpwSvcTypId, String tpwSvcTypSno);

    StlmTakPtInfRspVO findSimStlmTakPtByService(String tpwSvcTypId, String tpwSvcTypSno);

    /** ------------------------------
     * 등록 (PERD/SIM)
     * ------------------------------ */
    void savePerdStlmTakPt(StlmTakPtInfRspVO vo);

    void saveSimTakPt(StlmTakPtInfRspVO vo);

    /** ------------------------------
     * 수정 (PERD/SIM, 서비스ID+번호 기준)
     * ------------------------------ */
    void updatePerdStlmTakPtByService(StlmTakPtInfRspVO vo);

    void updateSimStlmTakPtByService(StlmTakPtInfRspVO vo);
}
