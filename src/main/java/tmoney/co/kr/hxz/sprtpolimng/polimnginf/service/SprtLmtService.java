package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service;

import java.util.List;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.*;

/**
 * 지원 한도(금액/건수) 조회/저장 서비스 인터페이스.
 * <p>
 * - 설정하기(3in1) 모달 초기 데이터 조회<br/>
 * - 금액/건수 한도 조회 및 페이징<br/>
 * - 서비스 단위 한도유형(금액-월/분기/건수) 강제 일치 검증<br/>
 * - 이전 버전 한도 N 처리 및 신규 버전 저장
 */
public interface SprtLmtService {

    // ===== 모달 초기/조회 =====

    /**
     * 지원 한도 설정 모달 초기 템플릿 생성.
     * <p>
     * - 분기/월/건수 기본 행 템플릿 생성<br/>
     * - 실제 데이터가 없을 때 화면 진입 시 사용
     *
     * @return 분기/월/건수 기본 템플릿 정보를 포함한 모달 VO
     */
    SprtLmtModalVO initModal();

    /**
     * 서비스유형별 거래(건수) 적용여부 수정.
     * <p>
     * - trd_ncnt_adpt_yn 필드를 Y/N 으로 갱신
     *
     * @param tpwSvcTypId 서비스유형 ID
     * @param adptYn      거래적용여부(Y/N)
     */
    void updateTrdNcntLtnAdptYn(String tpwSvcTypId, String adptYn);

    /**
     * 설정하기(3in1) 모달 진입 시, 서비스/유형 기준 현재 설정된 한도 조회.
     * <p>
     * - 데이터가 없으면 기본 템플릿(분기/월/건수) 리턴<br/>
     * - 금액(월/분기)·건수 유형에 따라 각각 다른 DTO 구조로 매핑
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return 모달에 바인딩할 상세 한도 정보 VO
     */
    SprtLmtModalDtlVO readSprtLmtByTpwSvcTypId(String tpwSvcId, String tpwSvcTypId);

    // ===== 목록/페이징 =====

    /**
     * 지원 한도 내역 페이징 조회.
     * <p>
     * - page/size 기반 offset 계산<br/>
     * - 목록/전체건수 조회 후 PageDataVO 로 래핑
     *
     * @param req 검색 조건(서비스/서비스유형/한도유형 등 포함)
     * @return 페이징 처리된 지원 한도 내역
     */
    PageDataVO<SprtLmtRspVO> readSprtLmtPtPaging(SprtLmtSrchReqVO req);

    /**
     * 지원 한도 내역 리스트 조회.
     *
     * @param req 검색 조건
     * @return 지원 한도 내역 리스트
     */
    List<SprtLmtRspVO> readSprtLmtPtList(SprtLmtSrchReqVO req);

    /**
     * 지원 한도 내역 카운트 조회.
     *
     * @param req 검색 조건
     * @return 해당 조건의 전체 건수
     */
    long readSprtLmtPtListCnt(SprtLmtSrchReqVO req);

    // ===== 기존 한도 조회 =====

    /**
     * 서비스/유형 기준 활성 한도 존재 여부 조회.
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return 활성 한도(use_yn=Y)가 1건 이상 존재하면 true
     */
    boolean hasExistingLimit(String tpwSvcId, String tpwSvcTypId);

    /**
     * 서비스/유형 기준 지원 한도 상세 조회.
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @param useYn       사용여부(Y/N)
     * @return 조건에 해당하는 한도 상세 리스트
     */
    List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(String tpwSvcId,
                                              String tpwSvcTypId,
                                              String useYn);

    // ===== 이전 버전 N 처리 =====

    /**
     * 이전 버전 한도 N 처리.
     * <p>
     * - (서비스, 서비스유형, 한도구분, 직전 일련번호, 관리번호 집합) 기준으로 use_yn = 'N'<br/>
     * - 금액 한도에서 기간 단위로 관리번호 재사용 시, 이전 버전 비활성화 목적
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @param tpwLmtDvsCd 한도구분 코드(01=금액, 02=건수)
     * @param prevSno     직전 버전 일련번호
     * @param mngNos      N 처리 대상 관리번호 집합
     */
    void updateSprtLmtUseYnByMngNos(String tpwSvcId,
                                    String tpwSvcTypId,
                                    String tpwLmtDvsCd,
                                    String prevSno,
                                    List<String> mngNos);

    // ===== 메인 저장 =====

    /**
     * 메인 저장 (금액/건수 공통).
     * <p>
     * 1) 과거(현재 월 이전) 연월 금지 검증<br/>
     * 2) 기간/중복 등 도메인 검증<br/>
     * 3) 건수일 경우 유형코드 보정(기본 02)<br/>
     * 4) 서비스 단위 한도유형(금액-월/분기/건수) 강제 일치 검증<br/>
     * 5) 기존 활성 한도 조회 후, 이전버전 N 처리 및 신규 insert
     *
     * @param req 금액/건수 공통 저장 요청 VO
     */
    void insertSprtLmtAmt(InstReqVO req);

    // ===== 관리번호/분기 범위 조회 =====

    /**
     * 지원금한도관리번호 시퀀스 다건 조회.
     * <p>
     * - sq_tbhxzd208_spfn_lmt_mng_no_01 기반<br/>
     * - 여러 행 insert 시 관리번호를 미리 채번하는 용도
     *
     * @param count 필요 개수
     * @return 새로 발급된 관리번호 리스트
     */
    List<String> readNextMngNo(int count);

    /**
     * 분기 기준 한도 시작/종료년월 범위 조회.
     * <p>
     * - 서비스/서비스유형 기준<br/>
     * - 금액-분기 한도에서 기존 분기 구간 존재 여부 확인 용도
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return 분기별 시작/종료년월 정보 리스트
     */
    List<QuarterRangeVO> readQuarterRanges(String tpwSvcId, String tpwSvcTypId);

    // ===== 실제 insert =====

    /**
     * 지원 한도(금액/건수) 실제 insert 수행.
     * <p>
     * - buildInserts 에서 생성된 VO 리스트를 그대로 insert
     *
     * @param req 저장할 지원 한도 요청 리스트
     */
    void insertSprtLmt(List<SprtLmtReqVO> req);

    /**
     * 저장 시 서비스 단위 한도유형(금액-분기/월/건수) 강제 일치 검증.
     * <p>
     * - 해당 서비스에 기존 데이터가 없으면 패스<br/>
     * - 하나의 유형만 존재하면, 그 유형과 동일한 유형으로만 추가 허용<br/>
     * - 서로 다른 유형이 이미 섞여 있으면 VALIDATION_ERROR 발생
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwLmtDvsCd 한도구분 코드(01=금액, 02=건수)
     * @param tpwLmtTypCd 한도유형 코드(01=월, 02=분기/건수)
     */
    void validateSvcLimitKind(String tpwSvcId, String tpwLmtDvsCd, String tpwLmtTypCd);

    /**
     * 서비스/서비스유형 기준 분기 존재 여부 및 한도유형 정보 조회.
     * <p>
     * - 분기 구간 존재 여부 및 서비스 단위 한도유형 일관성 판단에 사용
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return 기존 분기범위/한도유형/다중유형 여부를 담은 결과 VO
     */
    SprtLmtExistResVO checkExist(String tpwSvcId, String tpwSvcTypId);
}
