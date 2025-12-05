package tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
//import tmoney.co.kr.hxz.config.HxzDb;
import tmoney.co.kr.config.HxzDb;

import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtRspVO;

import java.util.List;

@HxzDb
@Mapper
public interface MemrStlmPtMapper {

    List<MemrStlmPtRspVO> readMemrStlmPtList(
            @Param("req") MemrStlmPtReqVO req,
            @Param("orgCd") String orgCd
    );

    long readMemrStlmPtListCnt(@Param("req") MemrStlmPtReqVO req, @Param("orgCd") String orgCd);

    void saveMemrStlmPt(MemrStlmPtRspVO form);

    int updateMemrStlmPt(MemrStlmPtRspVO form);

    void deleteMemrStlmPt(MemrStlmPtRspVO form);
}