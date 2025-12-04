package tmoney.co.kr.hxz.common.file.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.common.file.vo.AttachmentVO;

@HxzDb
@Mapper
public interface FileMapper {
    AttachmentVO readAtflPath(@Param("orgCd") String orgCd, @Param("atflMngNo") Long atflMngNo);
}
