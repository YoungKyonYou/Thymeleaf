package tmoney.co.kr.hxz.spfnsprtmng.payinf.service;

import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcDtlRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcTypRspVO;

import java.math.BigDecimal;
import java.util.List;


/**
 * ============================================
 * SprtSvcPtInfService
 * - 지원 서비스(201) / 서비스유형(202) CRUD 및 조회 서비스
 * ============================================
 */
public interface SprtSvcPtInfService { // 인
    // =====================
    // 201 지원서비스 (상위)
    // =====================
    PageDataVO<SprtSvcDtlRspVO> readSprtSvcPtInfList(SprtSvcPtInfReqVO reqVO, String orgCd);

    SprtSvcDtlRspVO readSprtSvcPtInf(String tpwSvcId, String orgCd, int page, int size);
    void saveSprtSvcPtInf(SprtSvcPtInfRspVO form);
    void updateSprtSvcPtInfByService(SprtSvcPtInfRspVO form);

    // =====================
    // 202 서비스유형 (하위)
    // =====================
    // ===============================
    // 202. 지원서비스유형 관리 관련
    // ===============================

    // 리스트 조회
    List<SprtSvcTypRspVO> readSprtSvcTypList(String tpwSvcId);

    // 단건 상세보기
    SprtSvcTypRspVO readSprtSvcTyp(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String tpwSvcId);

    // 신규 등록
    void saveSprtSvcTyp(SprtSvcTypRspVO form);

    // 수정
    void updateSprtSvcTyp(SprtSvcTypRspVO form);
    //수정할때 기록 조회용
    void updateUseYnN(SprtSvcTypRspVO form);

    long readSprtSvcTypListCnt(String tpwSvcId);
}