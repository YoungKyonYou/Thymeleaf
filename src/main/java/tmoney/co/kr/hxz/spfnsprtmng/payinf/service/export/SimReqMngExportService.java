package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimReqMngService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimReqMngRspVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class SimReqMngExportService implements ExportProvider<SimReqMngRspVO> {
    private final SimReqMngService simreqmngservice;

    private static final int QUERY_LIMIT = 1000; // 기본값 추가
    @Override
    public String name() {
        return "시뮬레이션요청관리";
    }

    @Override
    public List<ExportColumn<SimReqMngRspVO>> columns() {
        return List.of();
    }

    @Override
    public Stream<SimReqMngRspVO> stream(Map<String, String> params) {
        final String sort = params.getOrDefault("sort", "mbrs_id");
        final String dir = params.getOrDefault("dir", "asc");
        final int pageSize = parseIntOrDefault(params.get("size"), QUERY_LIMIT);

        final String orgCd    = params.get("orgCd");
        final String sttDt    = params.get("sttDt");
        final String endDt    = params.get("endDt");

        SimReqMngReqVO r = new SimReqMngReqVO();
        if (sttDt != null) r.setSttDt(sttDt);
        if (endDt != null) r.setEndDt(endDt);
        r.setSort(sort);
        r.setDir(dir);
        r.setPage(0);
        r.setSize(pageSize);

        // PagingStream 대신 바로 리스트 스트림 반환
        List<SimReqMngRspVO> list = simreqmngservice.readSimReqMngList(r, orgCd);
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
