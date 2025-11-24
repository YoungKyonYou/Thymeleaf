package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service.impl;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.error.exception.DomainExceptionCode;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.domain.SprtLmtPeriodValidator;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.mapper.SprtLmtMapper;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.service.SprtLmtService;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.*;

/**
 * 지원 한도(금액/건수) 조회/저장 서비스 구현체.
 * <p>
 * - 지원 한도 모달 진입/조회/저장<br/>
 * - 금액/건수 한도 공통 빌더 및 기간/중복 검증 연계<br/>
 * - 서비스 단위 한도유형(금액-분기/월/건수) 강제 일치 검증
 */
@RequiredArgsConstructor
@Service
public class SprtLmtServiceImpl implements SprtLmtService {

    private final SprtLmtMapper sprtLmtMapper;
    private final SprtLmtPeriodValidator periodValidator; // 기간/중복 검증 컴포넌트

    /**
     * 지원 한도 설정 모달 초기 템플릿 생성.
     * <p>
     * - 분기/월/건수 기본 행 템플릿 생성<br/>
     * - 실제 데이터가 없을 때 화면 진입 시 사용
     *
     * @return 분기/월/건수 기본 템플릿 정보를 포함한 모달 VO
     */
    @Override
    public SprtLmtModalVO initModal() {
        return new SprtLmtModalVO(
                initQuarterList(),
                initMonList(),
                initNcntList()
        );
    }

    /**
     * 서비스유형별 거래(건수) 적용여부 수정.
     * <p>
     * - trd_ncnt_adpt_yn 필드를 Y/N 으로 갱신
     *
     * @param tpwSvcTypId 서비스유형 ID
     * @param adptYn      거래적용여부(Y/N)
     */
    @Override
    @Transactional
    public void updateTrdNcntLtnAdptYn(String tpwSvcTypId, String adptYn) {
        sprtLmtMapper.updateTrdNcntLtnAdptYn(tpwSvcTypId, adptYn);
    }

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
    @Override
    @Transactional(readOnly = true)
    public SprtLmtModalDtlVO readSprtLmtByTpwSvcTypId(String tpwSvcId, String tpwSvcTypId) {
        List<SprtLmtRspVO> rows = readSprtLmtDtlByTpwSvc(tpwSvcId, tpwSvcTypId, "Y");

        // 기존 한도 없을 때 : 기본 템플릿
        if (rows == null || rows.isEmpty()) {
            SprtLmtModalVO m = initModal();
            return new SprtLmtModalDtlVO(m.getQt(), m.getMon(), m.getArr(), "01", "01");
        }

        String dvs = rows.get(0).getTpwLmtDvsCd(); // 01=금액, 02=건수
        String typ = rows.get(0).getTpwLmtTypCd(); // 01=월,   02=분기/건수

        if ("02".equals(dvs)) return buildCount(rows, dvs, typ);
        if ("01".equals(typ)) return buildAmountMonthly(rows, dvs, typ);
        return buildAmountQuarterly(rows, dvs, typ);
    }

    /* ===================== Modal DTO 빌더 ===================== */

    /**
     * 금액-월 한도 모달 DTO 빌드.
     *
     * @param rows 조회된 한도 엔티티 리스트
     * @param dvs  한도구분(01=금액)
     * @param typ  한도유형(01=월)
     * @return 금액-월 기준 모달 상세 VO
     */
    private SprtLmtModalDtlVO buildAmountMonthly(List<SprtLmtRspVO> rows, String dvs, String typ) {
        List<AmtReqVO> mon = rows.stream()
                .map(a -> new AmtReqVO(
                        a.getSpfnLmtMngNo(),
                        a.getSpfnLmtSno(),
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        a.getTgtAdptVal()))
                .collect(Collectors.toList());

        return new SprtLmtModalDtlVO(
                initQuarterList(),
                mon,
                initNcntList(),
                dvs, typ
        );
    }

    /**
     * 금액-분기 한도 모달 DTO 빌드.
     *
     * @param rows 조회된 한도 엔티티 리스트
     * @param dvs  한도구분(01=금액)
     * @param typ  한도유형(02=분기)
     * @return 금액-분기 기준 모달 상세 VO
     */
    private SprtLmtModalDtlVO buildAmountQuarterly(List<SprtLmtRspVO> rows, String dvs, String typ) {
        List<AmtReqVO> qt = rows.stream()
                .map(a -> new AmtReqVO(
                        a.getSpfnLmtMngNo(),
                        a.getSpfnLmtSno(),
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        a.getTgtAdptVal()))
                .collect(Collectors.toList());

        return new SprtLmtModalDtlVO(
                qt,
                initMonList(),
                initNcntList(),
                dvs, typ
        );
    }

    /**
     * 건수 한도 모달 DTO 빌드.
     *
     * @param rows 조회된 한도 엔티티 리스트
     * @param dvs  한도구분(02=건수)
     * @param typ  한도유형(02)
     * @return 건수 기준 모달 상세 VO
     */
    private SprtLmtModalDtlVO buildCount(List<SprtLmtRspVO> rows, String dvs, String typ) {
        List<NcntReqVO> ncnt = rows.stream()
                .map(a -> new NcntReqVO(
                        a.getSpfnLmtMngNo(),
                        a.getSpfnLmtSno(),
                        a.getMinCndtVal(),
                        a.getMaxCndtVal(),
                        a.getTgtAdptVal()
                ))
                .collect(Collectors.toList());

        return new SprtLmtModalDtlVO(
                initQuarterList(),
                initMonList(),
                ncnt,
                dvs, typ
        );
    }

    /* ===================== 초기 템플릿 리스트 ===================== */

    /**
     * 분기용 금액 한도 초기 템플릿 리스트 생성.
     * <p>
     * - 분기는 UI에서 직접 행추가/기간 설정하므로 비어 있는 VO 4건 생성
     *
     * @return 빈 분기 금액 한도 VO 리스트
     */
    private List<AmtReqVO> initQuarterList() {
        // 분기는 UI에서 직접 행추가/기간 설정하므로 비어있는 4행 템플릿만 생성
        return IntStream.range(0, 4)
                .mapToObj(i -> new AmtReqVO())
                .collect(Collectors.toList());
    }

    /**
     * 금액-월 한도 초기 템플릿 리스트 생성.
     * <p>
     * - 현재 연도의 1~12월까지 월별 기본 행 생성<br/>
     * - 시작/종료년월 동일, 기본 금액 0
     *
     * @return 월별 기본 금액 한도 VO 리스트
     */
    private List<AmtReqVO> initMonList() {
        int year = LocalDate.now().getYear();
        return IntStream.rangeClosed(1, 12)
                .mapToObj(i -> {
                    String yyyymm = String.format("%d%02d", year, i);
                    return new AmtReqVO("", "", yyyymm, yyyymm, 0);
                })
                .collect(Collectors.toList());
    }

    /**
     * 건수 한도 초기 템플릿 리스트 생성.
     * <p>
     * - 빈 건수 VO 4건 생성
     *
     * @return 건수 한도 VO 리스트
     */
    private List<NcntReqVO> initNcntList() {
        return IntStream.range(0, 4)
                .mapToObj(i -> new NcntReqVO())
                .collect(Collectors.toList());
    }

    /* ===================== 페이징 조회 ===================== */

    /**
     * 지원 한도 내역 페이징 조회.
     * <p>
     * - page/size 기반 offset 계산<br/>
     * - 목록/전체건수 조회 후 PageDataVO 로 래핑
     *
     * @param req 검색 조건(서비스/서비스유형/한도유형 등 포함)
     * @return 페이징 처리된 지원 한도 내역
     */
    @Override
    @Transactional(readOnly = true)
    public PageDataVO<SprtLmtRspVO> readSprtLmtPtPaging(SprtLmtSrchReqVO req) {
        final int offset = req.getPage() * req.getSize();
        long total = readSprtLmtPtListCnt(req);

        SprtLmtSrchReqVO reqVO = new SprtLmtSrchReqVO(
                req.getTpwSvcId(),
                req.getTpwSvcNm(),
                req.getTpwSvcTypId(),
                req.getTpwSvcTypNm(),
                req.getSpfnLmtMngNo(),
                req.getSpfnLmtSno(),
                req.getUseYn(),
                req.getTpwLmtDvsCd(),
                offset,
                req.getSize(),
                req.getSort(),
                req.getDir()
        );

        List<SprtLmtRspVO> content = readSprtLmtPtList(reqVO);
        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }

    /**
     * 지원 한도 내역 리스트 조회.
     *
     * @param req 검색 조건
     * @return 지원 한도 내역 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtPtList(SprtLmtSrchReqVO req) {
        return sprtLmtMapper.readSprtLmtPtList(req);
    }

    /**
     * 지원 한도 내역 카운트 조회.
     *
     * @param req 검색 조건
     * @return 해당 조건의 전체 건수
     */
    @Override
    @Transactional(readOnly = true)
    public long readSprtLmtPtListCnt(SprtLmtSrchReqVO req) {
        return sprtLmtMapper.readSprtLmtPtListCnt(req);
    }

    /**
     * 서비스/유형 기준 활성 한도 존재 여부 조회.
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return 활성 한도(use_yn=Y)가 1건 이상 존재하면 true
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasExistingLimit(String tpwSvcId, String tpwSvcTypId) {
        Integer cnt = sprtLmtMapper.readSprtLmtCntBySvcTyp(tpwSvcId, tpwSvcTypId);
        return cnt != null && cnt > 0;
    }

    /**
     * 서비스/유형 기준 지원 한도 상세 조회.
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @param useYn       사용여부(Y/N)
     * @return 조건에 해당하는 한도 상세 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(String tpwSvcId,
                                                     String tpwSvcTypId,
                                                     String useYn) {
        return sprtLmtMapper.readSprtLmtDtlByTpwSvc(tpwSvcId, tpwSvcTypId, useYn);
    }

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
    @Override
    @Transactional
    public void updateSprtLmtUseYnByMngNos(String tpwSvcId,
                                           String tpwSvcTypId,
                                           String tpwLmtDvsCd,
                                           String prevSno,
                                           List<String> mngNos) {
        if (prevSno == null || mngNos == null || mngNos.isEmpty()) return;
        sprtLmtMapper.updateSprtLmtUseYnByMngNo(tpwSvcId, tpwSvcTypId, tpwLmtDvsCd, prevSno, mngNos);
    }

    /* ===================== 메인 저장 ===================== */

    /**
     * 메인 저장 (금액/건수 공통).
     * <p>
     * 1) 과거(현재 월 이전) 연월 금지 검증<br/>
     * 2) 기간/중복 등 도메인 검증<br/>
     * 3) 건수일 경우 유형코드 보정(기본 02)<br/>
     * 4) 서비스 단위 한도유형(금액-월/분기/건수) 강제 일치 검증<br/>
     * 5) 기존 활성 한도 조회 후, BuildResult 기반 이전버전 N 처리 및 신규 insert
     *
     * @param req 금액/건수 공통 저장 요청 VO
     */
    @Override
    @Transactional
    public void insertSprtLmtAmt(InstReqVO req) {
        if (req == null) return;

        // 0) 과거(현재 월 이전) 연월 금지
        periodValidator.validateNotPast(req);

        // 1) 도메인 검증 (기간 겹침/중복 등)
        periodValidator.validate(req);

        // 1-1) 실제로 저장할 유형(특히 건수일 때 typCd null 처리) 계산
        final String effectiveTyp = "01".equals(req.getTpwLmtDvsCd())
                ? req.getTpwLmtTypCd()                 // 금액: 화면에서 온 typ 그대로
                : Optional.ofNullable(req.getTpwLmtTypCd())
                .orElse("02");               // 건수: null이면 기본 02

        // 1-2) 서비스 단위 한도유형 강제 일치 검증
        validateSvcLimitKind(
                req.getTpwSvcId(),
                req.getTpwLmtDvsCd(),
                effectiveTyp
        );

        // 2) 기존 활성(Y) 한도 조회
        List<SprtLmtRspVO> existing =
                readSprtLmtDtlByTpwSvc(req.getTpwSvcId(), req.getTpwSvcTypId(), "Y");
        boolean hasExisting = !existing.isEmpty();

        // 3) 월(01)은 종료월 = 시작월 강제
        if ("01".equals(req.getTpwLmtTypCd()) && req.getAmtList() != null) {
            req.getAmtList().forEach(a -> a.setLmtEndYm(a.getLmtSttYm()));
        }

        BuildResult build = buildInserts(req, existing, hasExisting);
        List<SprtLmtReqVO> toInsert = build.rows;
        if (toInsert.isEmpty()) return;

        // 5) 이전 버전 N 처리
        if ("01".equals(req.getTpwLmtDvsCd())) {
            // 금액: 기존 방식 그대로 (기간별 mngNo 단위로 N 처리)
            if (build.prevSno != null && !build.touchedMngNos.isEmpty()) {
                updateSprtLmtUseYnByMngNos(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        req.getTpwLmtDvsCd(),
                        build.prevSno,
                        new ArrayList<>(build.touchedMngNos)
                );
            }
        } else if ("02".equals(req.getTpwLmtDvsCd())) {
            // 건수: 이 서비스/유형의 기존 건수 한도(useYn=Y)는 전부 N 처리
            sprtLmtMapper.updateSprtLmtUseYnAllForCount(
                    req.getTpwSvcId(),
                    req.getTpwSvcTypId()
            );
        }

        // 6) 새 버전 insert
        insertSprtLmt(toInsert);
    }

    /**
     * 현재 시스템 연월(YYYYMM) 문자열 반환.
     *
     * @return 현재 연월(YYYYMM)
     */
    private String currentYYYYMM() {
        LocalDate now = LocalDate.now();
        return String.format("%04d%02d", now.getYear(), now.getMonthValue());
    }

    /**
     * 지원금한도관리번호 시퀀스 다건 조회.
     * <p>
     * - sq_tbhxzd208_spfn_lmt_mng_no_01 기반<br/>
     * - 여러 행 insert 시 관리번호를 미리 채번하는 용도
     *
     * @param count 필요 개수
     * @return 새로 발급된 관리번호 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> readNextMngNo(int count) {
        return sprtLmtMapper.readNextMngNo(count);
    }

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
    @Override
    @Transactional(readOnly = true)
    public List<QuarterRangeVO> readQuarterRanges(String tpwSvcId, String tpwSvcTypId) {
        return sprtLmtMapper.readQuarterRanges(tpwSvcId, tpwSvcTypId);
    }

    /**
     * 지원 한도(금액/건수) 실제 insert 수행.
     * <p>
     * - buildInserts 에서 생성된 VO 리스트를 그대로 insert
     *
     * @param req 저장할 지원 한도 요청 리스트
     */
    @Override
    @Transactional
    public void insertSprtLmt(List<SprtLmtReqVO> req) {
        sprtLmtMapper.insertSprtLmt(req);
    }

    /**
     * 서비스/서비스유형 기준 분기 존재 여부 및 한도유형 정보 조회.
     * <p>
     * - 분기 구간 존재 여부 및 서비스 단위 한도유형 일관성 판단에 사용
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return 기존 분기범위/한도유형/다중유형 여부를 담은 결과 VO
     */
    @Override
    @Transactional(readOnly = true)
    public SprtLmtExistResVO checkExist(String tpwSvcId, String tpwSvcTypId) {
        SprtLmtExistResVO res = new SprtLmtExistResVO();

        // 1) 분기 범위는 그대로 내려줌 (분기 중복 체크용)
        List<QuarterRangeVO> qtRanges = sprtLmtMapper.readQuarterRanges(tpwSvcId, tpwSvcTypId);
        res.setQtRanges(qtRanges);

        // 2) exists는 "이 서비스/유형에 활성 한도가 하나라도 있냐" 기준으로 변경
        boolean hasAny = hasExistingLimit(tpwSvcId, tpwSvcTypId);
        res.setExists(hasAny);

        // 3) 서비스 단위 한도 유형 정보
        List<SprtLmtKindVO> kinds = sprtLmtMapper.readSvcLmtKinds(tpwSvcId);
        if (kinds != null && !kinds.isEmpty()) {
            if (kinds.size() == 1) {
                res.setSvcLmtDvsCd(kinds.get(0).getDvsCd());
                res.setSvcLmtTypCd(kinds.get(0).getLmtTypCd());
                res.setMultiKinds(false);
            } else {
                res.setMultiKinds(true);
            }
        }

        return res;
    }

    // ===================== 저장 시 서버측 검증 =====================

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
    @Override
    @Transactional(readOnly = true)
    public void validateSvcLimitKind(String tpwSvcId, String tpwLmtDvsCd, String tpwLmtTypCd) {
        List<SprtLmtKindVO> kinds = sprtLmtMapper.readSvcLmtKinds(tpwSvcId);
        if (kinds == null || kinds.isEmpty()) {
            // 아직 이 서비스에 아무 한도도 없으면 제약 없음
            return;
        }

        if (kinds.size() > 1) {
            // 이미 서비스 안의 서비스유형들이 서로 다른 유형으로 섞여 있는 상태
            throw DomainExceptionCode.VALIDATION_ERROR
                    .newInstance("해당 서비스의 기존 한도유형이 서로 다릅니다. 기존 데이터부터 통일해 주세요.");
        }

        SprtLmtKindVO k = kinds.get(0);
        boolean sameDvs = Objects.equals(k.getDvsCd(), tpwLmtDvsCd);
        boolean sameTyp = Objects.equals(k.getLmtTypCd(), tpwLmtTypCd);

        if (!sameDvs || !sameTyp) {
            String msgKind = toKindLabel(k.getDvsCd(), k.getLmtTypCd());
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance(
                    "해당 서비스는 이미 [" + msgKind + "] 유형으로 한도가 설정되어 있습니다. " +
                            "기존 한도유형과 동일한 유형으로만 추가할 수 있습니다."
            );
        }
    }

    /**
     * 한도유형 코드 → 사용자용 라벨 변환.
     *
     * @param dvs 한도구분 코드(01=금액, 02=건수)
     * @param typ 한도유형 코드(01=월, 02=분기/건수)
     * @return 예) 금액-분기, 금액-월, 건수
     */
    private String toKindLabel(String dvs, String typ) {
        if ("01".equals(dvs) && "02".equals(typ)) return "금액-분기";
        if ("01".equals(dvs) && "01".equals(typ)) return "금액-월";
        if ("02".equals(dvs))                    return "건수";
        return dvs + "-" + typ;
    }

    // ===================== 실제 저장 메서드 예시 =====================

    /**
     * YYYY-MM / YYYYMM → YYYYMM 포맷으로 정규화.
     *
     * @param v 연월 문자열
     * @return 정규화된 YYYYMM 문자열, 실패 시 null
     */
    private String normalizeYm(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;

        if (s.matches("^\\d{4}-\\d{2}$")) {      // YYYY-MM
            return s.substring(0, 4) + s.substring(5, 7);
        }
        if (s.matches("^\\d{6}$")) {            // YYYYMM
            return s;
        }
        return null;
    }

    /**
     * 일련번호를 10자리 0패딩 문자열로 포맷.
     *
     * @param sno 일련번호
     * @return 10자리 문자열(예: 0000000001)
     */
    private String formatSno(int sno) {
        if (sno < 0) sno = 0;
        return String.format("%010d", sno);
    }

    /**
     * 금액/건수 공통 insert 빌더.
     * <p>
     * 규칙<br/>
     * - spfn_lmt_sno : 한 번 저장 시 전체 행 동일 (버전)<br/>
     * - spfn_lmt_mng_no :<br/>
     * &nbsp;&nbsp;· 동일 서비스/유형 + 동일 기간(시작/종료년월, 유형)이면 → 기존 관리번호 재사용<br/>
     * &nbsp;&nbsp;· 완전 신규 기간이면 → 새 관리번호 채번<br/>
     * <br/>
     * 또한, 이전 버전 N 처리를 위해<br/>
     * - prevSno       : 직전 버전 sno<br/>
     * - touchedMngNos : 이번 저장에서 사용된 관리번호 집합<br/>
     * 을 같이 리턴한다.
     *
     * @param req         저장 요청 VO
     * @param existing    기존 한도 목록
     * @param hasExisting 기존 한도 존재 여부
     * @return insert 대상 행 리스트와 이전 버전 정보가 담긴 BuildResult
     */
    private BuildResult buildInserts(InstReqVO req,
                                     List<SprtLmtRspVO> existing,
                                     boolean hasExisting) {

        final String dvs = req.getTpwLmtDvsCd(); // 01=금액, 02=건수
        final boolean isAmount = "01".equals(dvs);

        final List<AmtReqVO> amtSrc =
                Optional.ofNullable(req.getAmtList()).orElse(Collections.emptyList());
        final List<NcntReqVO> ncntSrc =
                Optional.ofNullable(req.getNcntList()).orElse(Collections.emptyList());

        final int needCount = isAmount ? amtSrc.size() : ncntSrc.size();
        if (needCount == 0) {
            return new BuildResult(Collections.emptyList(), null, Collections.emptySet());
        }

        // 기존 한도의 유형 정보
        final String curDvs = hasExisting ? existing.get(0).getTpwLmtDvsCd() : null;
        final String curTyp = hasExisting ? existing.get(0).getTpwLmtTypCd() : null;

        // 이번 저장에서 사용할 유형 코드
        final String nextTyp = isAmount
                ? req.getTpwLmtTypCd()                                   // 금액: 화면에서 넘어온 값
                : Optional.ofNullable(req.getTpwLmtTypCd())               // 건수: null이면 기존 값 또는 기본 02
                .orElse(curTyp != null ? curTyp : "02");

        // ===== sno 계산: 같은 서비스/유형/한도구분 기준 max(sno) + 1 =====
        int maxSnoInt = existing.stream()
                .filter(r -> dvs.equals(r.getTpwLmtDvsCd()))
                .map(SprtLmtRspVO::getSpfnLmtSno)
                .filter(Objects::nonNull)
                .mapToInt(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        String prevSno = (maxSnoInt > 0) ? String.format("%010d", maxSnoInt) : null;
        String nextSno = String.format("%010d", maxSnoInt + 1);

        // 건수 한도용 기간 기본값
        final String nowYm = currentYYYYMM();
        final String sttYmForCount = hasExisting ? existing.get(0).getLmtSttYm() : nowYm;
        final String endYmForCount = hasExisting ? existing.get(0).getLmtEndYm() : nowYm;

        List<SprtLmtReqVO> out = new ArrayList<>(needCount);
        Set<String> touchedMngNos = new HashSet<>();

        // ============== 1) 금액 한도 (분기/월) ==============
        if (isAmount) {

            // 1-1. existing 을 기간 → 관리번호 맵으로 구성
            Map<PeriodKeyVO, String> mngNoByPeriod = new HashMap<>();
            if (hasExisting && "01".equals(curDvs)) {
                for (SprtLmtRspVO row : existing) {
                    if (!dvs.equals(row.getTpwLmtDvsCd())) continue;
                    if (!nextTyp.equals(row.getTpwLmtTypCd())) continue;

                    PeriodKeyVO key = new PeriodKeyVO(row.getLmtSttYm(), row.getLmtEndYm());
                    mngNoByPeriod.putIfAbsent(key, row.getSpfnLmtMngNo());
                }
            }

            // 1-2. 이번 요청에서 “완전 신규 기간”만 모으기
            Set<PeriodKeyVO> newKeys = new LinkedHashSet<>();
            for (AmtReqVO a : amtSrc) {
                PeriodKeyVO key = new PeriodKeyVO(a.getLmtSttYm(), a.getLmtEndYm());
                if (!mngNoByPeriod.containsKey(key)) {
                    newKeys.add(key);
                }
            }

            // 1-3. 신규 기간 개수만큼 readNextMngNo 로 한 번에 관리번호 채번
            if (!newKeys.isEmpty()) {
                List<String> newMngNos = readNextMngNo(newKeys.size());
                Iterator<String> it = newMngNos.iterator();
                for (PeriodKeyVO key : newKeys) {
                    if (!it.hasNext()) {
                        throw new IllegalStateException("관리번호 개수가 부족합니다.");
                    }
                    mngNoByPeriod.put(key, it.next());
                }
            }

            // 1-4. 최종 out 리스트 구성 + touchedMngNos 수집
            for (AmtReqVO a : amtSrc) {
                PeriodKeyVO key = new PeriodKeyVO(a.getLmtSttYm(), a.getLmtEndYm());
                String mngNo = mngNoByPeriod.get(key);
                if (mngNo == null) {
                    throw new IllegalStateException("기간(" + key + ")에 대한 관리번호가 없습니다.");
                }

                touchedMngNos.add(mngNo);

                out.add(new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        mngNo,
                        nextSno,
                        "01",       // 금액
                        nextTyp,    // 01=월, 02=분기
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        0,
                        0,
                        a.getTgtAdptVal(),
                        "Y"
                ));
            }

            // ============== 2) 건수 한도 ==============
        } else {
            // 기준 연월: 기존 데이터가 있으면 그 값을 쓰고, 없으면 현재 월
            String baseStt = normalizeYm(sttYmForCount);
            if (baseStt == null) baseStt = nowYm;

            String baseEnd = normalizeYm(endYmForCount);
            if (baseEnd == null) baseEnd = baseStt;

            // 건수는 요청 행 수만큼 관리번호를 미리 뽑는다
            Deque<String> mngNoPool = new ArrayDeque<>(readNextMngNo(needCount));

            for (NcntReqVO n : ncntSrc) {
                String mngNo = mngNoPool.removeFirst();

                out.add(new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        mngNo,
                        nextSno,                // 이번 저장의 공통 일련번호
                        "02",                   // 건수
                        nextTyp,                // 02
                        baseStt,
                        baseEnd,
                        n.getMinCndtVal(),
                        n.getMaxCndtVal(),
                        n.getTgtAdptVal(),
                        "Y"
                ));
            }
        }

        return new BuildResult(out, prevSno, touchedMngNos);
    }

    /**
     * buildInserts 결과 묶음.
     * <p>
     * - rows         : 실제 insert 대상 행 리스트<br/>
     * - prevSno      : 직전 버전 sno (이전 버전 N 처리용)<br/>
     * - touchedMngNos: 이번 저장에서 사용된 관리번호 집합
     */
    private static class BuildResult {
        final List<SprtLmtReqVO> rows;
        final String prevSno;
        final Set<String> touchedMngNos;

        BuildResult(List<SprtLmtReqVO> rows, String prevSno, Set<String> touchedMngNos) {
            this.rows = rows;
            this.prevSno = prevSno;
            this.touchedMngNos = touchedMngNos;
        }
    }
}
