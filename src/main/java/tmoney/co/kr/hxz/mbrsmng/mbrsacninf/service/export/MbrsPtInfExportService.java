package tmoney.co.kr.hxz.mbrsmng.mbrsacninf.service.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmoney.co.kr.export.ExportColumn;
import tmoney.co.kr.export.ExportProvider;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.service.MbrsPtInfService;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfReqVO;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfRspVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 회원정보내역조회 엑셀 다운로드 서비스
 */
@RequiredArgsConstructor
@Component
public class MbrsPtInfExportService implements ExportProvider<MbrsPtInfRspVO> {

    private final MbrsPtInfService mbrsPtInfService;

    // 엑셀 다운로드 시 최대 조회 건수 제한 (필요에 따라 조정)
    private static final int QUERY_LIMIT = 5000;

    @Override
    public String name() {
        // 엑셀 파일명 등으로 사용될 이름
        return "회원정보내역조회";
    }

    @Override
    public List<ExportColumn<MbrsPtInfRspVO>> columns() {
        return List.of(
                // 화면 그리드 순서 및 상세 정보 포함하여 구성
                new ExportColumn<>("회원ID", MbrsPtInfRspVO::getMbrsId),
                new ExportColumn<>("회원명", MbrsPtInfRspVO::getMbrsNm),
                new ExportColumn<>("행정동코드", MbrsPtInfRspVO::getAddoCd),
                new ExportColumn<>("회원상태", MbrsPtInfRspVO::getMbrsStaCd), // 필요 시 코드변환 로직 추가 가능

                new ExportColumn<>("서비스명", MbrsPtInfRspVO::getTpwSvcNm),
                new ExportColumn<>("서비스유형명", MbrsPtInfRspVO::getTpwSvcTypNm),
                new ExportColumn<>("서비스가입일자", MbrsPtInfRspVO::getMbrsSvcJoinDt),
                new ExportColumn<>("회원서비스상태", MbrsPtInfRspVO::getTpwMbrsSvcStaCd),

                new ExportColumn<>("카드번호", MbrsPtInfRspVO::getCardNo),
                new ExportColumn<>("은행코드", MbrsPtInfRspVO::getBnkCd),
                new ExportColumn<>("계좌번호", MbrsPtInfRspVO::getAcntNo)
        );
    }

    @Override
    public Stream<MbrsPtInfRspVO> stream(Map<String, String> params) {
        // 1. 파라미터 추출 및 기본값 설정
        final String sort = params.getOrDefault("sort", "mbrs_svc_join_dt");
        final String dir = params.getOrDefault("dir", "desc");

        // TODO: 엑셀 다운 10개씩만 나오는중
        final int pageSize = parseIntOrDefault(params.get("size"), QUERY_LIMIT);

        // 검색 조건 매핑
        final String orgCd = params.getOrDefault("orgCd", "0000000"); // 기본값 설정

        // 2. ReqVO 생성 및 값 세팅
        MbrsPtInfReqVO req = new MbrsPtInfReqVO();

        req.setSttDt(params.get("sttDt"));
        req.setEndDt(params.get("endDt"));

        req.setMbrsId(params.get("mbrsId"));
        req.setMbrsNm(params.get("mbrsNm"));
        req.setMbrsStaCd(params.get("mbrsStaCd"));
        req.setTpwJoinTypCd(params.get("tpwJoinTypCd"));
        req.setAddoCd(params.get("addoCd"));
        req.setCardNo(params.get("cardNo"));

        // 정렬 및 페이징 설정 (엑셀은 보통 1페이지에 큰 사이즈로 조회)
        req.setSort(sort);
        req.setDir(dir);
        req.setPage(1); // 1페이지
        req.setSize(pageSize);
        req.setOffset(0); // 첫 페이지부터 조회

        // 3. 서비스 호출 및 스트림 반환
        List<MbrsPtInfRspVO> list = mbrsPtInfService.readMbrsPtInfList(req, orgCd);
        return list.stream();
    }

    /**
     * 안전한 정수 변환 유틸 메서드
     */
    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}