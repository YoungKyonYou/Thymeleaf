package tmoney.co.kr.hxz.common.cmncd.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.common.cmncd.vo.CmnCdVO;

@HxzDb
@Mapper
public interface CmnCdMapper {
    List<CmnCdVO> readCmnCdInf(@Param("cmnGrpCdId") String cmnGrpCdId);
}
