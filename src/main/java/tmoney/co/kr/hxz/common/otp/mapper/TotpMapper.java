package tmoney.co.kr.hxz.common.otp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;

@HxzDb
@Mapper
public interface TotpMapper {
    int existsById(@Param("mngrId") String mngrId);

    int updateOtpSecretById(@Param("mngrId") String mngrId,
                            @Param("otpScrtKeyVal") String otpScrtKeyVal);

    String readOtpSecretById(@Param("mngrId") String mngrId);
}
