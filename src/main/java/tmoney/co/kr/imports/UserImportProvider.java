package tmoney.co.kr.imports;


import org.springframework.stereotype.Component;
import tmoney.co.kr.imports.ImportColumn;
import tmoney.co.kr.imports.ImportProvider;

import java.util.Arrays;
import java.util.List;

@Component
public class UserImportProvider implements ImportProvider<UserImportRow> {

    @Override
    public String name() {
        return "user";
    }

    @Override
    public UserImportRow newInstance() {
        return new UserImportRow();
    }

    @Override
    public List<ImportColumn<UserImportRow>> columns() {
        return Arrays.asList(
                new ImportColumn<>("사용자ID", 0, (row, v) -> row.setUserId(v)),
                new ImportColumn<>("이름",     1, (row, v) -> row.setUserName(v)),
                new ImportColumn<>("이메일",   2, (row, v) -> row.setEmail(v)),
                new ImportColumn<>("나이",     3, (row, v) -> {
                    if (v == null || v.isEmpty()) {
                        row.setAge(null);
                    } else {
                        try {
                            row.setAge(Integer.parseInt(v));
                        } catch (NumberFormatException e) {
                            // 여기서 예외 던지면 ImportError에 기록됨
                            throw new IllegalArgumentException("나이는 숫자여야 합니다: " + v);
                        }
                    }
                }),
                new ImportColumn<>("전화번호", 4, (row, v) -> row.setPhone(v))
        );
    }

    @Override
    public String templateSheetName() {
        return "사용자목록";
    }

    @Override
    public String templateFilename() {
        return "user-import-template.xlsx";
    }

    /**
     * 템플릿 다운로드 시 같이 내려줄 예시 데이터 3줄
     * 컬럼 순서: [사용자ID, 이름, 이메일, 나이, 전화번호]
     */
    @Override
    public List<String[]> templateSampleRows() {
        return Arrays.asList(
                new String[] { "user01", "홍길동", "user01@example.com", "30", "010-1234-5678" },
                new String[] { "user02", "김철수", "user02@example.com", "41", "010-2345-6789" },
                new String[] { "user03", "이영희", "user03@example.com", "20",   "02-123-4567" }
        );
    }
}