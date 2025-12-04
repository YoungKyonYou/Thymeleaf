package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper.StlmTakPtInfMapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.StlmTakPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.StlmTakPtInfRspVO;

import java.util.List;

import java.math.BigDecimal;

/**
 * ==========================================================
 * StlmTakPtInfServiceImpl
 *
 * 정산작업내역(정기/PERD, 시뮬레이션/SIM) 관련 서비스 구현체
 *
 * - 검색: ReqVO 사용
 * - 등록/수정/상세조회: RspVO 사용
 * - exeDiv 기준 PERD / SIM 테이블 분기 처리
 * ==========================================================
 */
@Service
@RequiredArgsConstructor
public class StlmTakPtInfServiceImpl implements StlmTakPtInfService {

    private final StlmTakPtInfMapper stlmTakPtInfMapper;


    /**
     * ----`-------------------------------------------------------------
     * 1. 검색용 리스트 조회
     *
     * [Process]
     * 1. ReqVO 기반 검색 조건으로 리스트 조회
     * 2. 리스트 총 건수 조회
     * 3. PageDataVO 객체로 페이징 처리 후 반환
     *
     * @param req 검색 조건 (StlmTakPtInfReqVO)
     * @return PageDataVO<StlmTakPtInfRspVO> 페이징 처리된 리스트
     * -----------------------------------------------------------------
     */
    @Override
    @Transactional(readOnly = true)
    public PageDataVO<StlmTakPtInfRspVO> readStlmTakPtPaging(StlmTakPtInfReqVO req, String orgCd, String exeDiv) {


        System.out.println(exeDiv);
        // 🔹 exeDiv 기본값 설정 (값이 없으면 "PERD"로)
        // final String exeDiv = (req.getExeDiv() == null || req.getExeDiv().isEmpty()) ? "PERD" : req.getExeDiv().toUpperCase();
        // System.out.println(exeDiv);

        // 🔹 페이지네이션 offset 계산
        final int offset = req.getPage() * req.getSize();

        // 🔹 총 건수 조회 (exeDiv에 따라 Mapper 분기)
        Long total;
        List<StlmTakPtInfRspVO> content;

        // 🔹 요청 파라미터 복사
        StlmTakPtInfReqVO reqVO = new StlmTakPtInfReqVO(
                exeDiv,                     // 실행구분
                req.getAplSttDt(),          // 신청시작일
                req.getAplEndDt(),          // 신청종료일
                req.getStlmDt(),            // 정산일자
                req.getFixDt(),             // 확정일자
                (String) orgCd,                      // 기관코드
                req.getSttDt(),             // 검색시작일자
                req.getEndDt(),             // 검색종료일자
                req.getSvcNm(),             // 서비스명
                req.getSvcTypNm(),          // 서비스유형명
                req.getTpwSvcId(),          // 서비스ID
                req.getTpwSvcTypId(),       // 서비스유형ID
                req.getTpwSvcTypSno(),      // 서비스유형일련번호
                req.getSearchType(),        // 검색유형
                req.getPage(),              // 페이지
                req.getSize(),              // 페이지크기
                req.getSort(),              // 정렬컬럼
                req.getDir()                // 정렬방향
        );

        // 🔹 exeDiv 값에 따라 조회 분기
        if ("SIM".equals(exeDiv)) {
            total = stlmTakPtInfMapper.readSimStlmListCnt(reqVO, orgCd);
            content = stlmTakPtInfMapper.readSimStlmList(reqVO, orgCd);
        } else { // PERD 기본
            total = stlmTakPtInfMapper.readPerdStlmListCnt(reqVO, orgCd);
            content = stlmTakPtInfMapper.readPerdStlmList(reqVO, orgCd);
        }

        // 📌 Null 체크 추가: total이 null이면 0L로 설정 (NPE 방지)
        final Long safeTotal = (total == null) ? 0L : total;

        // ======================= 디버깅 출력 추가 =======================
        // **디버깅을 위해 total과 content 상태 출력**
        System.out.println("DEBUG: total (long) = " + total);
        System.out.println("DEBUG: content == null ? " + (content == null));
        if (content != null) {
            System.out.println("DEBUG: content size = " + content.size());
        }
        // =============================================================

        // 🔹 결과 반환
        return new PageDataVO<>(content, req.getPage(), req.getSize(), safeTotal);
    }

    /**
     * -----------------------------------------------------------------
     * 2. 서비스ID + 서비스번호 기준 단건 조회 (상세보기)
     * <p>
     * [Process]
     * 1. exeDiv 값에 따라 PERD/SIM 테이블 분기
     * 2. Mapper를 통해 단건 조회
     *
     * @param tpwSvcTypId  서비스ID
     * @param tpwSvcTypSno 서비스번호
     * @param exeDiv       실행구분(PERD/SIM)
     * @param stlmDt
     * @return StlmTakPtInfRspVO 조회된 단건 데이터
     * -----------------------------------------------------------------
     */
//    @Override
//    @Transactional(readOnly = true)
//    public StlmTakPtInfRspVO findTakPtInf(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String exeDiv, String tpwSvcId, String stlmDt) {
//        if ("SIM".equalsIgnoreCase(exeDiv)) {
//            return stlmTakPtInfMapper.findSimTakPtInf(tpwSvcTypId, tpwSvcTypSno, exeDiv, tpwSvcId);
//        } else {
//            return stlmTakPtInfMapper.findPerdTakPtInf(tpwSvcTypId, tpwSvcTypSno, exeDiv, tpwSvcId, stlmDt);
//        }
//    }



    /**
     * -----------------------------------------------------------------
     * 3. 단건 등록
     *
     * [Process]
     * 1. exeDiv 값에 따라 PERD/SIM 테이블 분기
     * 2. Mapper를 통해 insert 수행
     *
     * @param form 등록할 데이터 (StlmTakPtInfRspVO)
     * -----------------------------------------------------------------
     */
    @Override
    public void saveStlmTakPtInf(StlmTakPtInfRspVO form) {


        System.out.println("씸 구분 체크 ㄱㄱ");
        System.out.println(form);
        System.out.println(form.getExeDiv());


        if ("SIM".equalsIgnoreCase(form.getExeDiv())) {
            stlmTakPtInfMapper.saveSimTakPt(form);
        } else {
            stlmTakPtInfMapper.savePerdStlmTakPt(form);
        }
    }

    /**
     * -----------------------------------------------------------------
     * 4. 단건 수정 (서비스ID + 서비스번호 기준)
     *
     * [Process]
     * 1. exeDiv 값에 따라 PERD/SIM 테이블 분기
     * 2. Mapper를 통해 update 수행
     *
     * @param form 수정할 데이터 (StlmTakPtInfRspVO)
     * -----------------------------------------------------------------
     */
    @Override
    public void updateStlmTakPtInfByService(StlmTakPtInfRspVO form) {
        if ("SIM".equalsIgnoreCase(form.getExeDiv())) {
            stlmTakPtInfMapper.updateSimStlmTakPtByService(form);
        } else {
            stlmTakPtInfMapper.updatePerdStlmTakPtByService(form);
        }
    }

    @Override
    public StlmTakPtInfRspVO readSimTakPtInf(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String exeDiv, String tpwSvcId, String aplDt) {
        return stlmTakPtInfMapper.readSimTakPtInf(tpwSvcTypId, tpwSvcTypSno, exeDiv, tpwSvcId, aplDt);
    }

    @Override
    public StlmTakPtInfRspVO readPerdTakPtInf(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String exeDiv, String tpwSvcId, String stlmDt) {
        return stlmTakPtInfMapper.readPerdTakPtInf(tpwSvcTypId, tpwSvcTypSno, exeDiv, tpwSvcId, stlmDt);
    }
}
