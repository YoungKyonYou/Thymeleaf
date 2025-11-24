package tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngRspVO;

import java.util.List;

@HxzDb
@Mapper
public interface SimReqMngMapper {

    // 시뮬레이션 내역 리스트 조회
    List<SimReqMngRspVO> readSimReqMngList(
            @Param("req") SimReqMngReqVO req,
            @Param("orgCd") String orgCd
    );

    // 시뮬레이션 내역 총 건수 조회
    long readSimReqMngListCnt(
            @Param("req") SimReqMngReqVO req,
            @Param("orgCd") String orgCd
    );
    
    // 시뮬레이션요청 등록
    void saveSimReqMng(SimReqMngRspVO form);
}
