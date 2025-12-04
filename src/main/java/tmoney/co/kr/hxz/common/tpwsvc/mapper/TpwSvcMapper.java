package tmoney.co.kr.hxz.common.tpwsvc.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.common.tpwsvc.vo.TpwSvcInfVO;

import java.util.List;

@Mapper
@HxzDb
public interface TpwSvcMapper {
    List<TpwSvcInfVO> readTpwSvcInfList(@Param("orgCd") String orgCd);
}
