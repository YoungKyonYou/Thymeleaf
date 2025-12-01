package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.imports.ImportColumn;
import tmoney.co.kr.imports.ImportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngRspVO;

import java.util.Collections;
import java.util.List;



@RequiredArgsConstructor
@Component
public class SimReqMngImportService implements ImportProvider<SimReqMngRspVO> {

    @Override
    public String name() {
        return "시뮬레이션요청관리";
    }

    @Override
    public SimReqMngRspVO newInstance() {
        return new SimReqMngRspVO();
    }

    @Override
    public String templateFilename() {
        return "시뮬레이션_요청_관리_업로드.xlsx";
    }

    @Override
    public String templateSheetName() {
        return "시뮬레이션요청관리";
    }

    @Override
    public List<ImportColumn<SimReqMngRspVO>> columns() {
        return List.of(
            new ImportColumn<>("신청일자", 0, (vo, val) -> vo.setAplDt(val)),
            new ImportColumn<>("회원ID", 1, (vo, val) -> vo.setMbrsId(val)),
            new ImportColumn<>("회원명", 2, (vo, val) -> vo.setMbrsNm(val)),
            new ImportColumn<>("서비스ID", 3, (vo, val) -> vo.setTpwSvcId(val)),
            new ImportColumn<>("서비스유형ID", 4, (vo, val) -> vo.setTpwSvcTypId(val)),
            new ImportColumn<>("서비스유형일련번호", 5, (vo, val) -> vo.setTpwSvcTypSno(val == null ? null : Integer.parseInt(val))),
            new ImportColumn<>("카드번호", 6, (vo, val) -> vo.setCardNo(val)),
            new ImportColumn<>("카드시작일자", 7, (vo, val) -> vo.setCardSttDt(val)),
            new ImportColumn<>("카드종료일자", 8, (vo, val) -> vo.setCardEndDt(val)),
            new ImportColumn<>("처리완료여부", 9, (vo, val) -> vo.setPrcgFnYn(val))
        );
    }

    @Override
    public List<String[]> templateSampleRows() {
        return Collections.singletonList(
            new String[]{
                "20250101",             // 0 신청일자
                "USER001",              // 1 회원ID
                "홍길동",               // 2 회원명
                "SV00001",              // 3 서비스ID
                "PREMIUM",              // 4 서비스유형ID
                "1",                    // 5 서비스유형일련번호
                "1234-5678-0000-1111",  // 6 카드번호
                "20250101",             // 7 카드시작일자
                "20251231",             // 8 카드종료일자
                "N"                     // 9 처리완료여부
            }
        );
    }
}