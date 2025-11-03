package tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfRspVO;

import java.util.List;

@HxzDb
@Mapper
public interface SimPtInfMapper {

    // 시뮬레이션 내역 리스트 조회
    List<SimPtInfRspVO> readSimPtList(
            @Param("req") SimPtInfReqVO req,
            @Param("orgCd") String orgCd
    );

    // 시뮬레이션 내역 총 건수 조회
    long readSimPtListCnt(
            @Param("req") SimPtInfReqVO req,
            @Param("orgCd") String orgCd
    );
}
