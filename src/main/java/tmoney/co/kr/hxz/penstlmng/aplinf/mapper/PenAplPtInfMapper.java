package tmoney.co.kr.hxz.penstlmng.aplinf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfReqVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfRspVO;

import java.util.List;

@HxzDb
@Mapper
public interface PenAplPtInfMapper {

    List<PenAplPtInfRspVO> readPenAplPtInfList(
            @Param("req") PenAplPtInfReqVO req,
            @Param("orgCd") String orgCd
    );

    long readPenAplPtInfListCnt(@Param("req") PenAplPtInfReqVO req, @Param("orgCd") String orgCd);

    List<PenAplPtInfRspVO> readPenAplCntByMonth(@Param("req") PenAplPtInfReqVO req);

    List<PenAplPtInfRspVO> readPenAplCntByDay(@Param("req") PenAplPtInfReqVO req);

    void updateApprove(PenAplPtInfRspVO form);
}