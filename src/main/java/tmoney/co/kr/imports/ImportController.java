package tmoney.co.kr.imports;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tmoney.co.kr.hxz.common.error.exception.DomainExceptionCode;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportProviderRegistry registry;

    /**
     * 엑셀 업로드 (기존)
     */
    @PostMapping(
            value = "/xlsx",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public <T> ResponseEntity<ImportResult<T>> importXlsx(
            @RequestParam String provider,
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestPart("file") MultipartFile file
    ) throws Exception {

        ImportProvider<T> p = registry.get(provider);
        if (p == null) {
            // provider 이름이 잘못된 경우도 엑셀 관련 에러로 처리
            throw DomainExceptionCode.EXCEL_FILE_INVALID
                    .newInstance("알 수 없는 provider 입니다. provider=" + provider);
        }

        ImportResult<T> result = ExcelImporter.read(file.getInputStream(), p);
        return ResponseEntity.ok(result);
    }
    /**
     * 엑셀 템플릿 다운로드 (재사용 가능한 버전)
     * 예: GET /import/template/xlsx?provider=user
     */
    @GetMapping("/template/xlsx")
    public void downloadTemplate(
            @RequestParam String provider,
            HttpServletResponse response
    ) throws Exception {

        ImportProvider<?> p = registry.get(provider);
        if (p == null) {
            throw DomainExceptionCode.EXCEL_FILE_INVALID
                    .newInstance("알 수 없는 provider 입니다. provider=" + provider);
        }

        String filename = URLEncoder
                .encode(p.templateFilename(), StandardCharsets.UTF_8.name())
                .replace("+", "%20");

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + filename
        );

        try (Workbook wb = new XSSFWorkbook();
             OutputStream os = response.getOutputStream()) {

            // provider 에서 시트명/컬럼 헤더 재사용
            Sheet sheet = wb.createSheet(p.templateSheetName());
            Row headerRow = sheet.createRow(0);

            List<? extends ImportColumn<?>> cols = castColumns(p.columns());

            // 0행: 헤더
            for (int i = 0; i < cols.size(); i++) {
                ImportColumn<?> col = cols.get(i);
                headerRow.createCell(i).setCellValue(col.getHeader());
            }

            // 🔹 1행 이후: provider 가 준 샘플 데이터 채우기
            List<String[]> samples = p.templateSampleRows();
            if (samples != null && !samples.isEmpty()) {
                int rowIdx = 1; // 데이터는 1번 행부터 시작
                for (String[] rowValues : samples) {
                    Row row = sheet.createRow(rowIdx++);
                    if (rowValues == null) continue;

                    // 컬럼 개수 초과 안 하도록 보정
                    int len = Math.min(rowValues.length, cols.size());
                    for (int c = 0; c < len; c++) {
                        String v = rowValues[c];
                        row.createCell(c).setCellValue(v == null ? "" : v);
                    }
                }
            }


            int paddingChars = 2;
            for (int i = 0; i < cols.size(); i++) {
                autoSizeWithPadding(sheet, i, paddingChars);
            }

            wb.write(os);
            os.flush();
        }
    }
    /**
     * autoSizeColumn 호출 후, 글자 padding 만큼 여유 폭을 더 주는 헬퍼
     * - POI 는 1글자 폭을 256 단위로 본다.
     */
    private void autoSizeWithPadding(Sheet sheet, int colIndex, int paddingChars) {
        sheet.autoSizeColumn(colIndex);

        int currentWidth = sheet.getColumnWidth(colIndex);
        int paddedWidth = currentWidth + (256 * Math.max(paddingChars, 0));
        int maxWidth = 255 * 256; // 엑셀 최대 폭 보호

        sheet.setColumnWidth(colIndex, Math.min(paddedWidth, maxWidth));
    }

    /**
     * 제네릭 타입 지워진 ImportColumn 리스트를 다루기 위한 헬퍼
     */
    @SuppressWarnings("unchecked")
    private List<? extends ImportColumn<?>> castColumns(List<?> cols) {
        return (List<? extends ImportColumn<?>>) cols;
    }
}
