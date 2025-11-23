package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service;


import java.util.List;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.*;

/**
 * 지원 한도(금액/건수) 조회/저장 서비스 인터페이스
 */
public interface SprtLmtService {

    // ===== 모달 초기/조회 =====
    SprtLmtModalVO initModal();

    void updateTrdNcntLtnAdptYn(String tpwSvcTypId, String adptYn);

    SprtLmtModalDtlVO readSprtLmtByTpwSvcTypId(String tpwSvcId, String tpwSvcTypId);

    // ===== 목록/페이징 =====
    PageDataVO<SprtLmtRspVO> readSprtLmtPtPaging(SprtLmtSrchReqVO req);

    List<SprtLmtRspVO> readSprtLmtPtList(SprtLmtSrchReqVO req);

    long readSprtLmtPtListCnt(SprtLmtSrchReqVO req);

    // ===== 기존 한도 조회 =====
    boolean hasExistingLimit(String tpwSvcId, String tpwSvcTypId);

    List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(String tpwSvcId,
                                              String tpwSvcTypId,
                                              String useYn);

    // ===== 이전 버전 N 처리 =====
    void updateSprtLmtUseYnByMngNos(String tpwSvcId,
                                    String tpwSvcTypId,
                                    String tpwLmtDvsCd,
                                    String prevSno,
                                    List<String> mngNos);

    // ===== 메인 저장 =====
    void insertSprtLmtAmt(InstReqVO req);

    // ===== 관리번호/분기 범위 조회 =====
    List<String> readNextMngNo(int count);

    List<QuarterRangeVO> readQuarterRanges(String tpwSvcId, String tpwSvcTypId);

    // ===== 실제 insert =====
    void insertSprtLmt(List<SprtLmtReqVO> req);

    void validateSvcLimitKind(String tpwSvcId, String tpwLmtDvsCd, String tpwLmtTypCd);
    SprtLmtExistResVO checkExist(String tpwSvcId, String tpwSvcTypId);
}
