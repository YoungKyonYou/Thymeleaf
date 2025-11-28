package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper.StlmTakPtInfMapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfRspVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * ============================================
 * StlmTakPtInfExportService
 * ============================================
 */


@RequiredArgsConstructor
@Component
public class StlmTakPtInfExportService implements ExportProvider<StlmTakPtInfRspVO> {
    private final StlmTakPtInfMapper stlmTakPtInfMapper;

    @Override
    public String name() {
        return "정산작업내역조회";
    }

    @Override
    public List<ExportColumn<StlmTakPtInfRspVO>> columns() {
        return List.of(
                new ExportColumn<>("기관명", StlmTakPtInfRspVO::getOrgNm),
                new ExportColumn<>("서비스명", StlmTakPtInfRspVO::getTpwSvcNm),
                new ExportColumn<>("서비스유형명", StlmTakPtInfRspVO::getTpwSvcTypNm),
                new ExportColumn<>("신청시작일자", StlmTakPtInfRspVO::getAplSttDt),
                new ExportColumn<>("신청종료일자", StlmTakPtInfRspVO::getAplEndDt),
                new ExportColumn<>("거래시작일자", StlmTakPtInfRspVO::getTpwTrdSttDt),
                new ExportColumn<>("거래종료일자", StlmTakPtInfRspVO::getTpwTrdEndDt),
                new ExportColumn<>("정산일자", StlmTakPtInfRspVO::getStlmDt),
                new ExportColumn<>("확정일자", StlmTakPtInfRspVO::getFixDt),
                new ExportColumn<>("마감확정여부", StlmTakPtInfRspVO::getClosCfmYn),
                new ExportColumn<>("정산확정여부", StlmTakPtInfRspVO::getStlmFixYn),
                new ExportColumn<>("회계처리완료여부", StlmTakPtInfRspVO::getAcngPrcgFnYn)
        );
    }


    @Override
    public Stream<StlmTakPtInfRspVO> stream(Map<String, String> params) {
        
        String orgCd = params.get("orgCd");
        String exeDiv = params.get("exeDiv");


        StlmTakPtInfReqVO req = new StlmTakPtInfReqVO(params);
        req.setPage(0);
        req.setSize(1000);

        List<StlmTakPtInfRspVO> content;

        if ("SIM".equals(exeDiv)) {
            content = stlmTakPtInfMapper.readSimStlmList(req, orgCd);
        }
        else{
            content = stlmTakPtInfMapper.readPerdStlmList(req, orgCd);
        }


        return content.stream();
    }
}
