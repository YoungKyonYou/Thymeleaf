package tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper;

import org.springframework.data.repository.query.Param;
import tmoney.co.kr.config.HxzDb;
import org.apache.ibatis.annotations.Mapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfRspVO;

import java.util.List;
import java.math.BigDecimal;

@HxzDb
@Mapper
public interface StlmTakPtInfMapper {

    /** ------------------------------
     * 검색용 리스트 조회
     * ------------------------------ */
    List<StlmTakPtInfRspVO> readPerdStlmList(StlmTakPtInfReqVO req, String orgCd);

    List<StlmTakPtInfRspVO> readSimStlmList(StlmTakPtInfReqVO req, String orgCd);

    /** 리스트 카운트 */
    Long readSimStlmListCnt(StlmTakPtInfReqVO req, String orgCd);

    long readPerdStlmListCnt(StlmTakPtInfReqVO req, String orgCd);


    /**
     * ------------------------------
     * 상세조회 (PERD/SIM 테이블)
     * ------------------------------
     */
    StlmTakPtInfRspVO findPerdTakPtInf(@Param("tpwSvcTypId") String tpwSvcTypId,
                                       @Param("tpwSvcTypSno") BigDecimal tpwSvcTypSno,
                                       @Param("exeDiv") String exeDiv,
                                       @Param("tpwSvcId") String tpwSvcId);

    StlmTakPtInfRspVO findSimTakPtInf(@Param("tpwSvcTypId") String tpwSvcTypId,
                                      @Param("tpwSvcTypSno") BigDecimal tpwSvcTypSno,
                                      @Param("exeDiv") String exeDiv,
                                      @Param("tpwSvcId") String tpwSvcId);

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
