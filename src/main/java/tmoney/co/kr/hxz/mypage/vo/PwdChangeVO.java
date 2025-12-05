package tmoney.co.kr.hxz.mypage.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PwdChangeVO {
    @NotBlank
    @Size(min = 8, max = 50)
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 50)
    private String newPassword;
}
