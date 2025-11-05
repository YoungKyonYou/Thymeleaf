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
@Transactional
public class StlmTakPtInfServiceImpl implements StlmTakPtInfService {

    private final StlmTakPtInfMapper stlmTakPtInfMapper;

    /**
     * -----------------------------------------------------------------
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
    public PageDataVO<StlmTakPtInfRspVO> readStlmTakPtPaging(StlmTakPtInfReqVO req) {
        List<StlmTakPtInfRspVO> list = stlmTakPtInfMapper.readStlmTakPtInfList(req);
        int total = stlmTakPtInfMapper.StlmTakPtInfListCnt(req);
        return new PageDataVO<>(list, req.getPage(), req.getSize(), total);
    }

    /**
     * -----------------------------------------------------------------
     * 2. 서비스ID + 서비스번호 기준 단건 조회 (상세보기)
     *
     * [Process]
     * 1. exeDiv 값에 따라 PERD/SIM 테이블 분기
     * 2. Mapper를 통해 단건 조회
     *
     * @param tpwSvcTypId 서비스ID
     * @param tpwSvcTypSno 서비스번호
     * @param exeDiv 실행구분(PERD/SIM)
     * @return StlmTakPtInfRspVO 조회된 단건 데이터
     * -----------------------------------------------------------------
     */
    @Override
    public StlmTakPtInfRspVO findStlmTakPtInfByService(String tpwSvcTypId, String tpwSvcTypSno, String exeDiv) {
        if ("SIM".equalsIgnoreCase(exeDiv)) {
            return stlmTakPtInfMapper.findSimStlmTakPtByService(tpwSvcTypId, tpwSvcTypSno);
        } else {
            return stlmTakPtInfMapper.findPerdStlmTakPtByService(tpwSvcTypId, tpwSvcTypSno);
        }
    }

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
}
