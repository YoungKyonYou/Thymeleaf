package tmoney.co.kr.imports;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserImportRow {

    private String userId;
    private String userName;
    private String email;
    private Integer age;
    private String phone;

}
