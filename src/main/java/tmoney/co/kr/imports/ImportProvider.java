package tmoney.co.kr.imports;


import java.util.Collections;
import java.util.List;


import java.util.List;


public interface ImportProvider<T> {

    String name();

    T newInstance();

    List<ImportColumn<T>> columns();

    String templateSheetName();

    String templateFilename();

    /**
     * 템플릿에 같이 내려줄 예시 데이터 행들
     * - 각 String[] 는 컬럼 순서대로 값 채우기
     * - 기본은 빈 리스트 (필수 아님)
     */
    default List<String[]> templateSampleRows() {
        return Collections.emptyList();
    }
}