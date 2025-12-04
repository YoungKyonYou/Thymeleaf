package tmoney.co.kr.imports;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tmoney.co.kr.hxz.common.error.exception.DomainExceptionCode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelImporter {

    public static <T> ImportResult<T> read(InputStream in, ImportProvider<T> provider) throws Exception {
        List<T> rows = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                return new ImportResult<>(rows, errors, 0, 0, 0);
            }

            DataFormatter formatter = new DataFormatter();
            List<ImportColumn<T>> cols = provider.columns();

            int total = 0;
            int success = 0;
            int failed = 0;

            // 0번 row는 header라고 가정
            int firstDataRow = 1;
            int lastRowNum = sheet.getLastRowNum();

            for (int rowIndex = firstDataRow; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                total++;

                T target = provider.newInstance();
                boolean rowEmpty = true;
                boolean rowError = false;

                for (ImportColumn<T> col : cols) {
                    int colIndex = col.getIndex();
                    Cell cell = row.getCell(colIndex);
                    String raw = formatter.formatCellValue(cell);

                    if (raw != null && !raw.trim().isEmpty()) {
                        rowEmpty = false;
                    }

                    try {
                        col.bind(target, raw);
                    } catch (Exception e) {
                        rowError = true;
                        failed++;

                        int displayRow = rowIndex + 1;   // 사람 기준 1-based
                        int displayCol = colIndex + 1;   // 사람 기준 1-based

                        // 에러 상세를 ImportError 리스트에도 넣어두고
                        errors.add(new ImportError(
                                displayRow,
                                displayCol,
                                col.getHeader(),
                                raw,
                                e.getMessage()
                        ));

                        // 그리고 곧바로 도메인 예외로 래핑해서 던짐 (fail-fast)
                        String detailMessage = String.format(
                                "엑셀 %d행 %d열(%s) 값이 올바르지 않습니다. 잘못된 값: '%s'",
                                displayRow,
                                displayCol,
                                col.getHeader(),
                                raw
                        );

                        throw DomainExceptionCode.EXCEL_FILE_INVALID.newInstance(e, detailMessage);
                    }
                }

                if (!rowEmpty && !rowError) {
                    rows.add(target);
                    success++;
                }
            }

            return new ImportResult<>(rows, errors, total, success, failed);
        }
    }
}