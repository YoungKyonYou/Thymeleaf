package tmoney.co.kr.hxz.penstlmng.aplinf.service.export;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.penstlmng.aplinf.service.PenAplPtInfService;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfReqVO;
import tmoney.co.kr.hxz.penstlmng.aplinf.vo.PenAplPtInfRspVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class PenApiPtInfExportService implements ExportProvider<PenAplPtInfRspVO> {
    private final PenAplPtInfService penAplPtInfService;

    private static final int QUERY_LIMIT = 1000; // 기본값 추가
    @Override
    public String name() {
        return "지급금신청내역조회";
    }

    @Override
    public List<ExportColumn<PenAplPtInfRspVO>> columns()
    {
        return List.of(
            new ExportColumn<PenAplPtInfRspVO>("서비스유형명", t -> t.getTpwSvcTypNm() != null ? t.getTpwSvcTypNm() : ""),
            new ExportColumn<PenAplPtInfRspVO>("정산일자", t -> t.getStlmDt() != null ? t.getStlmDt().toString() : ""),
            new ExportColumn<PenAplPtInfRspVO>("회원ID", t -> t.getMbrsId() != null ? t.getMbrsId() : ""),
            new ExportColumn<PenAplPtInfRspVO>("신청일자", t -> t.getAplDt() != null ? t.getAplDt().toString() : ""),
            new ExportColumn<PenAplPtInfRspVO>("카드번호", t -> t.getCardNo() != null ? t.getCardNo() : ""),
            // TODO: rspvo 바꾸고 은행명 나오면 변경
            new ExportColumn<PenAplPtInfRspVO>("은행명", t -> t.getBnkCd() != null ? t.getBnkCd() : ""),
            new ExportColumn<PenAplPtInfRspVO>("계좌번호", t -> t.getAcntNo() != null ? t.getAcntNo() : ""),
            new ExportColumn<PenAplPtInfRspVO>("예금주명", t -> t.getOoaNm() != null ? t.getOoaNm() : ""),
            new ExportColumn<PenAplPtInfRspVO>("승인자ID", t -> t.getAproId() != null ? t.getAproId() : ""),
            new ExportColumn<PenAplPtInfRspVO>("승인일시", t -> t.getAprvDtm() != null ? t.getAprvDtm().toString() : ""),
            new ExportColumn<PenAplPtInfRspVO>("승인상태", t -> t.getAprvStaCd() != null ? t.getAprvStaCd() : ""),
            
            // new ExportColumn<PenAplPtInfRspVO>("지원 대상 유형", t -> t.getSupportType() != null ? t.getSupportType() : ""),
            // new ExportColumn<PenAplPtInfRspVO>("첨부파일관리번호(링크 미리보기)", t -> t.getAttachmentNo() != null ? t.getAttachmentNo() : ""),
            new ExportColumn<PenAplPtInfRspVO>("신청진행상태", t -> t.getTpwAplPrgsStaCd() != null ? t.getTpwAplPrgsStaCd() : "")
        );
    }



    @Override
    public Stream<PenAplPtInfRspVO> stream(Map<String, String> params) {
        final String sort = params.getOrDefault("sort", "mbrs_id");
        final String dir = params.getOrDefault("dir", "asc");
        final int pageSize = parseIntOrDefault(params.get("size"), QUERY_LIMIT);

        final String orgCd    = params.get("orgCd");
        final String sttDt    = params.get("sttDt");
        final String endDt    = params.get("endDt");

        PenAplPtInfReqVO r = new PenAplPtInfReqVO();
        if (sttDt != null) r.setSttDt(sttDt);
        if (endDt != null) r.setEndDt(endDt);
        r.setSort(sort);
        r.setDir(dir);
        r.setPage(0);
        r.setSize(pageSize);

        // PagingStream 대신 바로 리스트 스트림 반환
        List<PenAplPtInfRspVO> list = penAplPtInfService.readPenAplPtInfList(r, orgCd);
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
