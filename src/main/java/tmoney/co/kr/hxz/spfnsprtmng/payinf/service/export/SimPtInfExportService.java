package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SimPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.SimPtInfRspVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class SimPtInfExportService implements ExportProvider<SimPtInfRspVO> {
    private final SimPtInfService simPtInfService;
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
        return Stream.empty();
    }
}
