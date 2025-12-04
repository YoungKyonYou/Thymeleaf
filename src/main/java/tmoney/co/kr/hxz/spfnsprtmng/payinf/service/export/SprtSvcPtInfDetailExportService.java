package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SprtSvcPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcTypRspVO;

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
public class SprtSvcPtInfDetailExportService implements ExportProvider<SprtSvcTypRspVO> {
    private final SprtSvcPtInfService sprtSvcPtInfService;

    @Override
    public String name() {
        return "지원서비스상세관리";
    }

    // 순번	기관	지원서비스내용	서비스시작일자	서비스종료일자	서비스유형
    @Override
    public List<ExportColumn<SprtSvcTypRspVO>> columns() {
        return List.of(
                new ExportColumn<>("기관코드", vo -> vo.getOrgCd()),
                new ExportColumn<>("서비스ID", vo -> vo.getTpwSvcId()),
                new ExportColumn<>("서비스유형ID", vo -> vo.getTpwSvcTypId()),

                // 숫자/BigDecimal -> String 변환
                new ExportColumn<>("서비스유형순번", vo -> vo.getTpwSvcTypSno() != null ? vo.getTpwSvcTypSno().toPlainString() : ""),

                new ExportColumn<>("서비스유형명", vo -> vo.getTpwSvcTypNm()),
                new ExportColumn<>("서비스유형시작일", vo -> vo.getTpwSvcTypSttDt()),
                new ExportColumn<>("서비스유형종료일", vo -> vo.getTpwSvcTypEndDt()),
                new ExportColumn<>("서비스유형내용", vo -> vo.getTpwSvcTypCtt()),
                new ExportColumn<>("회원유형코드", vo -> vo.getTpwMbrsTypCd()),
                new ExportColumn<>("지원교통수단코드", vo -> vo.getTpwMntnCd()),
                new ExportColumn<>("정산주기구분코드", vo -> vo.getTpwStlmCycDvsCd()),
                new ExportColumn<>("정산분류코드", vo -> vo.getTpwStlmCtgCd()),

                // BigDecimal -> String
                new ExportColumn<>("정산분류적용값", vo -> vo.getStlmCtgAdptVal() != null ? vo.getStlmCtgAdptVal().toPlainString() : ""),

                new ExportColumn<>("교통거래요청", vo -> vo.getTrnsTrdReqYn()),
                new ExportColumn<>("원장거래요청", vo -> vo.getLdgrTrdReqYn()),
                new ExportColumn<>("택시거래요청", vo -> vo.getTaxiTrdReqYn()),
                new ExportColumn<>("지역거래요청", vo -> vo.getAreaTrdReqYn()),
                new ExportColumn<>("정산실행단계코드", vo -> vo.getTpwStlmActDvsCd()),
                new ExportColumn<>("사용여부", vo -> vo.getUseYn()),
                new ExportColumn<>("등록자", vo -> vo.getRgsrId()),
                new ExportColumn<>("등록일시", vo -> vo.getRgtDtm()),
                new ExportColumn<>("수정자", vo -> vo.getUpdrId()),
                new ExportColumn<>("수정일시", vo -> vo.getUpdDtm())
        );
    }


    @Override
    public Stream<SprtSvcTypRspVO> stream(Map<String, String> params) {
        // String orgCd = params.get("orgCd");
        String tpwSvcId = params.get("tpwSvcId");
        SprtSvcPtInfReqVO req = new SprtSvcPtInfReqVO(params);
        req.setPage(0);
        req.setSize(1000);

        return sprtSvcPtInfService.readSprtSvcTypList(tpwSvcId).stream();
    }
}
