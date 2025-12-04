package tmoney.co.kr.hxz.common.otp.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class EnrollRequestVO {
    @NotBlank
    @Size(max = 100)
    private String mngrId = "admin";

}
