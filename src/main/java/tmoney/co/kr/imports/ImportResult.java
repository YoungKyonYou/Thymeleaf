package tmoney.co.kr.imports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult<T> {
    private List<T> rows;            // 성공한 row들 (그리드에 바로 바인딩)
    private List<ImportError> errors;// 에러 목록
    private int totalRows;           // 읽은 총 row 수
    private int successRows;         // 성공 row 수
    private int errorRows;           // 에러 row 수
}
