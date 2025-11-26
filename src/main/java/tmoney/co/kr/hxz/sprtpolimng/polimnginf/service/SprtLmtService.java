package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service;

import java.util.List;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.QuarterRangeVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtExistResVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtKindVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtModalDtlVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtModalVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtRspVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtSrchReqVO;


public interface SprtLmtService {

    /* ===================== 모달/초기 템플릿 ===================== */

    /**
     * "설정하기(3in1)" 모달 최초 진입 시 사용할
     * 분기/월/건수 템플릿 리스트를 생성한다.
     */
    SprtLmtModalVO initModal();

    /**
     * 설정하기(3in1) 모달 진입 시 데이터 조회.
     *
     * @param tpwSvcId    서비스ID
     * @param tpwSvcTypId 서비스유형ID
     */
    SprtLmtModalDtlVO readSprtLmtByTpwSvcTypId(String tpwSvcId, String tpwSvcTypId);

    /* ===================== 트랜잭션/플래그 ===================== */

    /**
     * 거래 건수 한도 적용 여부(adptYn)를 업데이트한다.
     *
     * @param tpwSvcTypId 서비스유형ID
     * @param adptYn      적용 여부(Y/N)
     */
    void updateTrdNcntLtnAdptYn(String tpwSvcTypId, String adptYn);

    /**
     * 서비스유형별 거래건수적용여부 조회.
     * (sprtLmtMapper.readTrdNcntAdptYn 래핑)
     */
    String readTrdNcntAdptYn(String tpwSvcTypId);

    /* ===================== 페이징/목록 조회 ===================== */

    /**
     * 지원 한도 목록 페이징 조회.
     */
    PageDataVO<SprtLmtRspVO> readSprtLmtPtPaging(SprtLmtSrchReqVO req);

    /**
     * 지원 한도 목록 조회.
     * (sprtLmtMapper.readSprtLmtPtList 래핑)
     */
    List<SprtLmtRspVO> readSprtLmtPtList(SprtLmtSrchReqVO req);

    /**
     * 지원 한도 목록 총건수 조회.
     * (sprtLmtMapper.readSprtLmtPtListCnt 래핑)
     */
    long readSprtLmtPtListCnt(SprtLmtSrchReqVO req);

    /**
     * 서비스/서비스유형 기준 한도 데이터 건수 조회.
     * (sprtLmtMapper.readSprtLmtCntBySvcTyp 래핑)
     */
    Integer readSprtLmtCntBySvcTyp(String tpwSvcId, String tpwSvcTypId);

    /**
     * 해당 서비스/서비스유형에 한도 데이터가 존재하는지 여부 조회.
     *
     * @return true: 1건 이상 존재, false: 없음
     */
    boolean hasExistingLimit(String tpwSvcId, String tpwSvcTypId);

    /**
     * 서비스/서비스유형/사용여부 기준 한도 상세 목록 조회.
     * (sprtLmtMapper.readSprtLmtDtlByTpwSvc 래핑)
     */
    List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(String tpwSvcId,
                                              String tpwSvcTypId,
                                              String useYn);

    /* ===================== N 처리 / 저장 ===================== */

    /**
     * 이전 버전 N 처리 – (svcId, svcTypId, dvs, 직전 sno, 관리번호 집합) 기준.
     * (sprtLmtMapper.updateSprtLmtUseYnByMngNo 래핑)
     */
    void updateSprtLmtUseYnByMngNos(String tpwSvcId,
                                    String tpwSvcTypId,
                                    String tpwLmtDvsCd,
                                    String prevSno,
                                    List<String> mngNos);

    /**
     * 메인 저장 (금액/건수 공통).
     */
    void insertSprtLmtAmt(InstReqVO req);

    /**
     * 새로 필요한 관리번호 개수(count)만큼
     * 시퀀스에서 spfn_lmt_mng_no 리스트를 조회한다.
     * (sprtLmtMapper.readNextMngNo 래핑)
     */
    List<String> readNextMngNo(int count);

    /**
     * 해당 서비스/서비스유형의 기존 분기(시작~종료년월) 목록 조회.
     * (sprtLmtMapper.readQuarterRanges 래핑)
     */
    List<QuarterRangeVO> readQuarterRanges(String tpwSvcId, String tpwSvcTypId);

    /**
     * sprt_lmt 테이블에 한도 데이터를 일괄 insert 한다.
     * (sprtLmtMapper.insertSprtLmt 래핑)
     */
    void insertSprtLmt(List<SprtLmtReqVO> req);

    /* ===================== 한도 유형/존재 여부 체크 ===================== */

    /**
     * 서비스 단위 한도 유형 목록 조회.
     * (sprtLmtMapper.readSvcLmtKinds 래핑)
     */
    List<SprtLmtKindVO> readSvcLmtKinds(String tpwSvcId);

    /**
     * 기존 한도 존재 여부 및 서비스 단위 한도 유형 정보 조회.
     */
    SprtLmtExistResVO checkExist(String tpwSvcId, String tpwSvcTypId);

}
