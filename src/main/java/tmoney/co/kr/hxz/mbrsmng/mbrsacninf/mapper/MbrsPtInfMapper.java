package tmoney.co.kr.hxz.mbrsmng.mbrsacninf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfReqVO;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfRspVO;

import java.util.List;

@HxzDb
@Mapper
public interface MbrsPtInfMapper {

    // 시뮬레이션 내역 리스트 조회
    List<MbrsPtInfRspVO> readMbrsPtInfList(
            @Param("req") MbrsPtInfReqVO req,
            @Param("orgCd") String orgCd
    );

    // 시뮬레이션 내역 총 건수 조회
    long readMbrsPtInfListCnt(
            @Param("req") MbrsPtInfReqVO req,
            @Param("orgCd") String orgCd
    );
}
