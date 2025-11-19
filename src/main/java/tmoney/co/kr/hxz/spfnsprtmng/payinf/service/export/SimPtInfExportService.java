package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfRspVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class SimPtInfExportService implements ExportProvider<SimPtInfRspVO> {
    private final SimPtInfService simPtInfService;

    private static final int QUERY_LIMIT = 1000; // 기본값 추가
    @Override
    public String name() {
        return "시뮬레이션내역조회";
    }

    @Override
    public List<ExportColumn<SimPtInfRspVO>> columns() {
        return List.of();
    }

    @Override
    public Stream<SimPtInfRspVO> stream(Map<String, String> params) {
        final String sort = params.getOrDefault("sort", "mbrs_id");
        final String dir = params.getOrDefault("dir", "asc");
        final int pageSize = parseIntOrDefault(params.get("size"), QUERY_LIMIT);

        final String orgCd    = params.get("orgCd");
        final String sttDt    = params.get("sttDt");
        final String endDt    = params.get("endDt");

        SimPtInfReqVO r = new SimPtInfReqVO();
        if (sttDt != null) r.setSttDt(sttDt);
        if (endDt != null) r.setEndDt(endDt);
        r.setSort(sort);
        r.setDir(dir);
        r.setPage(0);
        r.setSize(pageSize);

        // PagingStream 대신 바로 리스트 스트림 반환
        List<SimPtInfRspVO> list = simPtInfService.readSimPtList(r, orgCd);
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
