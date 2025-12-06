package tmoney.co.kr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultipartEchoReq {
    private String mngrId;
    private String mngrName;
    private String email;
    private String phone;
    private String roleName;
    private String useYn;
}
