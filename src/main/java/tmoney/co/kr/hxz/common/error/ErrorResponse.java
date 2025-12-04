package tmoney.co.kr.hxz.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private Integer code;
    private String message;

    public static ErrorResponse of(Integer code, String message) {
        return new ErrorResponse(code, message);
    }
}
