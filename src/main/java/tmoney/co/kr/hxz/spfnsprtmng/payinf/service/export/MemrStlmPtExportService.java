package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.MemrStlmPtService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtRspVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class MemrStlmPtExportService implements ExportProvider<MemrStlmPtRspVO> {
    private final MemrStlmPtService memrStlmPtService;

    private static final int QUERY_LIMIT = 1000; // 기본값 추가
    @Override
    public String name() {
        return "수기정산내역조회";
    }


    @Override
    public List<ExportColumn<MemrStlmPtRspVO>> columns()
    {
        return List.of(
            new ExportColumn<>("거래일자", v -> v.getRideDmndDt()),
            new ExportColumn<>("총사용금액", v -> v.getLstTrdAmt()),
            new ExportColumn<>("회원ID", v -> v.getMbrsId()),
            new ExportColumn<>("회원명", v -> v.getMbrsNm()),
            new ExportColumn<>("회원상태", v -> v.getTpwMbrsSvcStaCd()),
            new ExportColumn<>("행정동명", v -> v.getAddoNm()),
            new ExportColumn<>("요청일자", v -> v.getReqDtm()),
            new ExportColumn<>("처리일자", v -> v.getPrcgDt()),
            new ExportColumn<>("처리상태", v -> v.getTpwMemrPrcgStaCd()),
            new ExportColumn<>("요청금액", v -> v.getAplAmt()),
            new ExportColumn<>("서비스명", v -> v.getTpwSvcNm()),
            new ExportColumn<>("서비스유형명", v -> v.getTpwSvcTypNm()),
            new ExportColumn<>("서비스유형번호", v -> v.getTpwSvcTypSno())
        );
    }


    @Override
    public Stream<MemrStlmPtRspVO> stream(Map<String, String> params) {
        final String sort = params.getOrDefault("sort", "mbrs_id");
        final String dir = params.getOrDefault("dir", "asc");
        final int pageSize = parseIntOrDefault(params.get("size"), QUERY_LIMIT);

        final String orgCd    = params.get("orgCd");
        final String sttDt    = params.get("sttDt");
        final String endDt    = params.get("endDt");

        MemrStlmPtReqVO r = new MemrStlmPtReqVO();
        if (sttDt != null) r.setSttDt(sttDt);
        if (endDt != null) r.setEndDt(endDt);
        r.setSort(sort);
        r.setDir(dir);
        r.setPage(0);
        r.setSize(pageSize);

        // PagingStream 대신 바로 리스트 스트림 반환
        List<MemrStlmPtRspVO> list = memrStlmPtService.readMemrStlmPtList(r, orgCd);
        return list.stream();
    }

    // 안전한 int 변환 메서드
    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
