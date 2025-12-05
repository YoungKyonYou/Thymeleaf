package tmoney.co.kr.hxz.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.mypage.vo.MngrVO;
import tmoney.co.kr.hxz.mypage.vo.UpdateMngrVO;

@HxzDb
@Mapper
public interface MyPageMapper {
    // readMngrMyPage
    MngrVO readMngrMyPage(@Param("mngrId") String mngrId);

    // updateMngrMyPage
    int updateMngrMyPage(UpdateMngrVO param);

    // readMngrPwd
    String readMngrPwd(@Param("mngrId") String mngrId);

    // updateMngrPwd (XML 에 parameterType 없으니 @Param 3개로 Map 형태 전달)
    int updateMngrPwd(@Param("mngrId") String mngrId,
                      @Param("encodedPassword") String encodedPassword,
                      @Param("updrId") String updrId);
}
