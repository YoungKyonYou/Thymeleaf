package tmoney.co.kr.imports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportDemoRequest  {
    private String userId;
    private String userName;
    private String email;
    private String age;
    private String phone;


}
