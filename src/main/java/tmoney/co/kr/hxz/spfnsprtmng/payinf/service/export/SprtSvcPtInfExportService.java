package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SprtSvcPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcDtlRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfReqVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * ============================================
 * SprtSvcPtInfExportService
 * - 201 서비스 내역 Export (엑셀/CSV)
 * ============================================
 */


@RequiredArgsConstructor
@Component
public class SprtSvcPtInfExportService implements ExportProvider<SprtSvcDtlRspVO> {
    private final SprtSvcPtInfService sprtSvcPtInfService;

    @Override
    public String name() {
        return "지원 서비스 내역조회";
    }

    // 순번	기관	지원서비스내용	서비스시작일자	서비스종료일자	서비스유형
    @Override
    public List<ExportColumn<SprtSvcDtlRspVO>> columns() {
        return List.of(
                // new ExportColumn<>("기관코드", SprtSvcDtlRspVO::getOrgCd),
                new ExportColumn<>("기관", SprtSvcDtlRspVO::getTpwOrgNm),
                new ExportColumn<>("지원서비스내용", SprtSvcDtlRspVO::getTpwSvcCtt),
                new ExportColumn<>("서비스시작일자", SprtSvcDtlRspVO::getTpwSvcSttDt),
                new ExportColumn<>("서비스종료일자", SprtSvcDtlRspVO::getTpwSvcEndDt),
                new ExportColumn<>("서비스유형", SprtSvcDtlRspVO::getTpwSvcNm)
        );
    }

    @Override
    public Stream<SprtSvcDtlRspVO> stream(Map<String, String> params) {
        String orgCd = params.get("orgCd");
        SprtSvcPtInfReqVO req = new SprtSvcPtInfReqVO(params);
        req.setPage(0);
        req.setSize(1000);

        return sprtSvcPtInfService.readSprtSvcPtInfList(req, orgCd).getContent().stream();
    }
}
