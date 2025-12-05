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

@RequiredArgsConstructor
@Component
public class StlmTakPtInfExportService implements ExportProvider<StlmTakPtInfRspVO> {
    private final StlmTakPtInfMapper stlmTakPtInfMapper;

    @Override
    public String name() {
        // 이 이름은 보통 Bean 이름이나 Provider ID로 쓰입니다.
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

        // [수정 1] orgCd가 넘어오지 않았을 경우 방어 로직 (기본값 설정 or 에러 방지)
        String orgCd = params.get("orgCd");
        if (orgCd == null || orgCd.isEmpty()) {
            orgCd = "0000000"; // 컨트롤러에 있던 기본값 사용
        }

        String exeDiv = params.get("exeDiv");

        // Request VO 생성
        StlmTakPtInfReqVO req = new StlmTakPtInfReqVO(params);

        // [수정 2] 엑셀은 페이징 없이 '전체'를 뽑아야 하므로 매우 큰 수로 설정
        // 0으로 설정 시 Mapper에서 'LIMIT' 구문을 태우지 않도록 처리되어 있다면 0도 좋음
        // 여기서는 안전하게 Integer.MAX_VALUE 사용
        req.setPage(0);
        req.setSize(999999);

        List<StlmTakPtInfRspVO> content;

        if ("SIM".equals(exeDiv)) {
            content = stlmTakPtInfMapper.readSimStlmList(req, orgCd);
        } else {
            content = stlmTakPtInfMapper.readPerdStlmList(req, orgCd);
        }

        return content.stream();
    }
}