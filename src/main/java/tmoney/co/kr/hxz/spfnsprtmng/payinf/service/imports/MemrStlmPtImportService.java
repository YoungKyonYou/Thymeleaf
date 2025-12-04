package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.imports.ImportColumn;
import tmoney.co.kr.imports.ImportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtRspVO;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class MemrStlmPtImportService implements ImportProvider<MemrStlmPtRspVO> {

    @Override
    public String name() {
        return "수기정산내역조회";
    }

    @Override
    public MemrStlmPtRspVO newInstance() {
        return new MemrStlmPtRspVO();
    }

    @Override
    public String templateFilename() {
        return "수기정산내역조회_업로드.xlsx";
    }

    @Override
    public String templateSheetName() {
        return "수기정산내역조회";
    }

    @Override
    public List<ImportColumn<MemrStlmPtRspVO>> columns() {
        return List.of(
            new ImportColumn<>("신청일시", 0, (vo, val) -> vo.setReqDtmRaw(val)),
            new ImportColumn<>("서비스유형일련번호", 1, (vo, val) -> vo.setTpwSvcTypSno(val)),
            new ImportColumn<>("회원ID", 2, (vo, val) -> vo.setMbrsId(val)),
            new ImportColumn<>("회원명", 3, (vo, val) -> vo.setMbrsNm(val)),
            new ImportColumn<>("서비스ID", 4, (vo, val) -> vo.setTpwSvcId(val)),
            new ImportColumn<>("서비스유형ID", 5, (vo, val) -> vo.setTpwSvcTypId(val)),
            new ImportColumn<>("지급처리여부", 6, (vo, val) -> vo.setPayPrcgYn(val)),
            new ImportColumn<>("처리일자", 7, (vo, val) -> vo.setPrcgDt(val)),
            new ImportColumn<>("지급금액", 8, (vo, val) -> vo.setPayAmt(val)),
            new ImportColumn<>("요청금액", 9, (vo, val) -> vo.setAplAmt(val)),
            new ImportColumn<>("수기처리상태", 10, (vo, val) -> vo.setManualPrcgSta(val)),
            new ImportColumn<>("행정동명", 11, (vo, val) -> vo.setAddoNm(val)),
            new ImportColumn<>("이체계좌", 12, (vo, val) -> vo.setAcctNo(val))
        );
    }

    @Override
    public List<String[]> templateSampleRows() {
        return Collections.singletonList(
            new String[]{
                "2025-01-01",
                "1",
                "USER01",
                "홍길동",
                "SV001",
                "TYPE01",
                "N",
                "2025-10-10",
                "20000",
                "10000",
                "01",
                "행정동",
                "123456-12-1234567"
            }
        );
    }
}
