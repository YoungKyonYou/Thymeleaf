package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.BatTakPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.BatTakPtInfRspVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 배치작업 포인트 내역 엑셀/CSV Export 서비스
 */
@RequiredArgsConstructor
@Component
public class BatTakPtInfExportService implements ExportProvider<BatTakPtInfRspVO> {

    private final BatTakPtInfService batTakPtInfService;

    private static final int QUERY_LIMIT = 1000; // 기본값 추가

    @Override
    public String name() {
        return "배치작업내역조회";
    }

    @Override
    public List<ExportColumn<BatTakPtInfRspVO>> columns()
    {
        return List.of(
                new ExportColumn<>("작업일자", BatTakPtInfRspVO::getBatTakDt),
                new ExportColumn<>("작업ID", BatTakPtInfRspVO::getBatTakId),
                new ExportColumn<>("배치유형코드", BatTakPtInfRspVO::getTpwBatTypCd),
                new ExportColumn<>("배치명", BatTakPtInfRspVO::getBatTakNm),
                new ExportColumn<>("배치시작일시", BatTakPtInfRspVO::getBatTakSttDtm),
                new ExportColumn<>("배치종료일시", BatTakPtInfRspVO::getBatTakEndDtm),
                new ExportColumn<>("처리건수", vo -> vo.getPrcgNcnt() != null ? vo.getPrcgNcnt().toString() : ""), // null 체크 필요
                new ExportColumn<>("배치처리상태코드", BatTakPtInfRspVO::getBatPrcgStaCd),
                new ExportColumn<>("등록자ID", BatTakPtInfRspVO::getRgsrId),
                new ExportColumn<>("등록일시", BatTakPtInfRspVO::getRgtDtm),
                new ExportColumn<>("수정자ID", BatTakPtInfRspVO::getUpdrId),
                new ExportColumn<>("수정일시", BatTakPtInfRspVO::getUpdDtm)
        );
    }


    @Override
    public Stream<BatTakPtInfRspVO> stream(Map<String, String> params) {
        final String sort = params.getOrDefault("sort", "bat_tak_dt");
        final String dir = params.getOrDefault("dir", "asc");
        final int pageSize = parseIntOrDefault(params.get("size"), QUERY_LIMIT);

        final String orgCd    = params.get("orgCd");
        final String batTakDt = params.get("batTakDt");
        final String batTakId = params.get("batTakId");
        final String sttDt    = params.get("sttDt");
        final String endDt    = params.get("endDt");

        BatTakPtInfReqVO r = new BatTakPtInfReqVO();
        if (sttDt != null) r.setSttDt(sttDt);
        if (endDt != null) r.setEndDt(endDt);
        r.setSort(sort);
        r.setDir(dir);
        r.setPage(0);
        r.setSize(pageSize);

        // PagingStream 대신 바로 리스트 스트림 반환
        List<BatTakPtInfRspVO> list = batTakPtInfService.readBatTakPtInfList(r, orgCd);
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
