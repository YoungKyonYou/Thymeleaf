package tmoney.co.kr.hxz.common.otp.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class VerifyRequestVO {

    /** 숫자 6자리 */
    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String code;
}
