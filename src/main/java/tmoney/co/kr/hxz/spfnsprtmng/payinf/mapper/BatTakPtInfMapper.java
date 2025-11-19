package tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfRspVO;

import java.util.List;

@HxzDb
@Mapper
public interface BatTakPtInfMapper {

    // 배치작업 포인트 리스트 조회
    List<BatTakPtInfRspVO> readBatTakPtInfList(
            @Param("req") BatTakPtInfReqVO req,
            @Param("orgCd") String orgCd
    );

    // 배치작업 포인트 총 건수 조회
    long readBatTakPtInfListCnt(
            @Param("req") BatTakPtInfReqVO req,
            @Param("orgCd") String orgCd
    );
}
