package tmoney.co.kr.hxz.sprtpolimng.polimnginf.mapper;

import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtRspVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtSrchReqVO;

import java.util.List;


@HxzDb
@Mapper
public interface SprtLmtMapper {
    List<SprtLmtRspVO> readSprtLmtPtList(
            @Param("req") SprtLmtSrchReqVO req
    );

    long readSprtLmtPtListCnt(@Param("req") SprtLmtSrchReqVO req);

    List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(@Param("tpwSvcId") String tpwSvcId, @Param("tpwSvcTypId") String tpwSvcTypId, @Param("useYn") String useYn);

    void insertSprtLmt(List<SprtLmtReqVO> req);

    void updatePrevSprtLmt(List<String> req, @Param("tpwSvcTypId") String tpwSvcTypId);

    void insertNextSprtLmt(List<SprtLmtReqVO> req, @Param("tpwSvcTypId") String tpwSvcTypId);

    String readSpfnLmtMngNoNextVal();

    String readSpfnLmtSnoNextVal(@Param("spfnLmtSno") String spfnLmtSno);
    List<String> readNextMngNo(@Param("count") int count);

    void updateTrdNcntLtnAdptYn(@Param("tpwSvcTypId") String tpwSvcTypId, @Param("adptYn") String adptYn);

    void updateSprtLmtUseYn(@Param("tpwSvcTypId") String tpwSvcTypId);
    Integer readSprtLmtCntBySvcTyp(@Param("tpwSvcId") String tpwSvcId,
                                   @Param("tpwSvcTypId") String tpwSvcTypId);
}
