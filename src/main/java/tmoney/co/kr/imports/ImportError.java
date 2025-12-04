package tmoney.co.kr.imports;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImportError {
    private int row;           // 엑셀 상 row 번호 (1-based)
    private int col;           // 엑셀 상 column 번호 (1-based)
    private String header;     // 헤더명
    private String value;      // 원본 값
    private String message;    // 파싱/검증 에러 메시지
}
