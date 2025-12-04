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

    // 리스트 조회
    List<SimReqMngRspVO> readSimReqMngList(
            @Param("req") SimReqMngReqVO req,
            @Param("orgCd") String orgCd
    );

    // 총 건수 조회
    long readSimReqMngListCnt(
            @Param("req") SimReqMngReqVO req,
            @Param("orgCd") String orgCd
    );

    // [등록]
    void saveSimReqSfpn(SimReqMngRspVO form); // 208
    void saveSimReqStlm(SimReqMngRspVO form); // 207

    // [수정] (int 반환: 수정된 건수)
    int updateSimReqSfpn(SimReqMngRspVO form); // 208
    int updateSimReqStlm(SimReqMngRspVO form); // 207

    // [삭제] (int 반환: 삭제된 건수)
    int deleteSimReqStlm(SimReqMngRspVO form); // 207
    int deleteSimReqSfpn(SimReqMngRspVO form); // 208
}