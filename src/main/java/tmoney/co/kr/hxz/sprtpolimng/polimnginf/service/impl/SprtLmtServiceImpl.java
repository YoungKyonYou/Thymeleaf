package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service.impl;


import java.time.LocalDate;
import java.time.YearMonth;
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
 * 지원 한도(금액/건수) 조회/저장 서비스 구현체
 */
@RequiredArgsConstructor
@Service
public class SprtLmtServiceImpl implements SprtLmtService {

    private final SprtLmtMapper sprtLmtMapper;
    private final SprtLmtPeriodValidator periodValidator; // 기간/중복 검증 컴포넌트

    @Override
    public SprtLmtModalVO initModal() {
        return new SprtLmtModalVO(
                initQuarterList(),
                initMonList(),
                initNcntList()
        );
    }

    @Override
    @Transactional
    public void updateTrdNcntLtnAdptYn(String tpwSvcTypId, String adptYn) {
        sprtLmtMapper.updateTrdNcntLtnAdptYn(tpwSvcTypId, adptYn);
    }

    /**
     * 설정하기(3in1) 모달 진입 시 데이터 조회
     */
    @Override
    @Transactional(readOnly = true)
    public SprtLmtModalDtlVO readSprtLmtByTpwSvcTypId(String tpwSvcId, String tpwSvcTypId) {
        // 해당 서비스/유형의 전체 활성 한도(금액/건수, 월/분기 포함)를 한 번에 조회
        List<SprtLmtRspVO> rows = readSprtLmtDtlByTpwSvc(tpwSvcId, tpwSvcTypId, "Y");

        // 기존 한도 없을 때 : 기존과 동일하게 기본 템플릿 리턴
        if (rows == null || rows.isEmpty()) {
            SprtLmtModalVO m = initModal();
            return new SprtLmtModalDtlVO(m.getQt(), m.getMon(), m.getArr(), "01", "01");
        }

        // 1) rows 를 유형별로 분리
        List<SprtLmtRspVO> qtRows = rows.stream()
                .filter(r -> "01".equals(r.getTpwLmtDvsCd()))   // 금액
                .filter(r -> "02".equals(r.getTpwLmtTypCd()))   // 분기
                .collect(Collectors.toList());

        List<SprtLmtRspVO> monRows = rows.stream()
                .filter(r -> "01".equals(r.getTpwLmtDvsCd()))   // 금액
                .filter(r -> "01".equals(r.getTpwLmtTypCd()))   // 월
                .collect(Collectors.toList());

        List<SprtLmtRspVO> ncntRows = rows.stream()
                .filter(r -> "02".equals(r.getTpwLmtDvsCd()))   // 건수
                .collect(Collectors.toList());

        // 2) 각 유형별 리스트를 DTO 로 변환
        //    - 데이터가 없으면 기존처럼 initXXXList() 템플릿 사용
        List<AmtReqVO> qt;
        if (qtRows.isEmpty()) {
            qt = initQuarterList();
        } else {
            // 기존 buildAmountQuarterly 로 매핑 로직 재사용
            qt = qtRows.stream()
                    .map(a -> new AmtReqVO(
                            a.getSpfnLmtMngNo(),
                            a.getSpfnLmtSno(),
                            a.getLmtSttYm(),
                            a.getLmtEndYm(),
                            a.getTgtAdptVal()))
                    .collect(Collectors.toList());
        }

        List<AmtReqVO> mon;
        if (monRows.isEmpty()) {
            mon = initMonList();
        } else {
            mon = monRows.stream()
                    .map(a -> new AmtReqVO(
                            a.getSpfnLmtMngNo(),
                            a.getSpfnLmtSno(),
                            a.getLmtSttYm(),
                            a.getLmtEndYm(),
                            a.getTgtAdptVal()))
                    .collect(Collectors.toList());
        }

        List<NcntReqVO> ncnt;
        if (ncntRows.isEmpty()) {
            ncnt = initNcntList();
        } else {
            ncnt = ncntRows.stream()
                    .map(a -> new NcntReqVO(
                            a.getSpfnLmtMngNo(),
                            a.getSpfnLmtSno(),
                            a.getLmtSttYm(),
                            a.getLmtEndYm(),
                            a.getMinCndtVal(),
                            a.getMaxCndtVal(),
                            a.getTgtAdptVal()
                    ))
                    .collect(Collectors.toList());
        }

        // 3) modal 의 "대표 dvs/typ" 은 기존대로 첫 행 기준 유지
        String dvs = rows.get(0).getTpwLmtDvsCd(); // 01=금액, 02=건수
        String typ = rows.get(0).getTpwLmtTypCd(); // 01=월, 02=분기/건수

        return new SprtLmtModalDtlVO(qt, mon, ncnt, dvs, typ);
    }

    /* ===================== Modal DTO 빌더 ===================== */

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

    private SprtLmtModalDtlVO buildCount(List<SprtLmtRspVO> rows, String dvs, String typ) {
        List<NcntReqVO> ncnt = rows.stream()
                .map(a -> new NcntReqVO(
                        a.getSpfnLmtMngNo(),
                        a.getSpfnLmtSno(),
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
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

    private List<AmtReqVO> initQuarterList() {
        // 분기는 UI에서 직접 행추가/기간 설정하므로 비어있는 4행 템플릿만 생성
        return IntStream.range(0, 4)
                .mapToObj(i -> new AmtReqVO())
                .collect(Collectors.toList());
    }

    private List<AmtReqVO> initMonList() {
        int year = LocalDate.now().getYear();
        return IntStream.rangeClosed(1, 12)
                .mapToObj(i -> {
                    String yyyymm = String.format("%d%02d", year, i);
                    return new AmtReqVO("", "", yyyymm, yyyymm, 0);
                })
                .collect(Collectors.toList());
    }

    private List<NcntReqVO> initNcntList() {
        return IntStream.range(0, 4)
                .mapToObj(i -> new NcntReqVO())
                .collect(Collectors.toList());
    }

    /* ===================== 페이징 조회 ===================== */

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

    @Override
    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtPtList(SprtLmtSrchReqVO req) {
        return sprtLmtMapper.readSprtLmtPtList(req);
    }

    @Override
    @Transactional(readOnly = true)
    public long readSprtLmtPtListCnt(SprtLmtSrchReqVO req) {
        return sprtLmtMapper.readSprtLmtPtListCnt(req);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasExistingLimit(String tpwSvcId, String tpwSvcTypId) {
        Integer cnt = sprtLmtMapper.readSprtLmtCntBySvcTyp(tpwSvcId, tpwSvcTypId);
        return cnt != null && cnt > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(String tpwSvcId,
                                                     String tpwSvcTypId,
                                                     String useYn) {
        return sprtLmtMapper.readSprtLmtDtlByTpwSvc(tpwSvcId, tpwSvcTypId, useYn);
    }

    /**
     * 이전 버전 N 처리 – (svcId, svcTypId, dvs, 직전 sno, 관리번호 집합) 기준
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
     * 메인 저장 (금액/건수 공통)
     */
    @Override
    @Transactional
    public void insertSprtLmtAmt(InstReqVO req) {
        if (req == null) return;

        periodValidator.validate(req);

        final String effectiveTyp = "01".equals(req.getTpwLmtDvsCd())
                ? req.getTpwLmtTypCd()
                : Optional.ofNullable(req.getTpwLmtTypCd()).orElse("02");

        final boolean editMode =
                "edit-3in1".equalsIgnoreCase(Optional.ofNullable(req.getMode()).orElse(""));

        List<SprtLmtRspVO> existing =
                readSprtLmtDtlByTpwSvc(req.getTpwSvcId(), req.getTpwSvcTypId(), "Y");
        boolean hasExisting = !existing.isEmpty();

        //  신규등록에서 가장 이른 YYYYMM 기준으로 기존 한도 N 처리
        YearMonth cutOffYm = null;

        if (editMode) {
            filterChangedRowsForEdit(req, existing, effectiveTyp);
            if ((req.getAmtList() == null || req.getAmtList().isEmpty())
                    && (req.getNcntList() == null || req.getNcntList().isEmpty())) {
                return;
            }
        } else {
            // 신규등록 모드 + 금액(dvs=01)
            if (hasExisting
                    && "01".equals(req.getTpwLmtDvsCd())
                    && req.getAmtList() != null
                    && !req.getAmtList().isEmpty()) {

                cutOffYm = deactivateFromMinYmForNewRegistration(req, existing);

                // 신규등록 모드 + 건수(dvs=02)
            } else if (hasExisting
                    && "02".equals(req.getTpwLmtDvsCd())
                    && req.getNcntList() != null
                    && !req.getNcntList().isEmpty()) {

                // cutOffYm은 현재 cross-type 검증(금액)에서만 사용하지만
                // 패턴 맞춰서 리턴값만 받아두면 됩니다.
                cutOffYm = deactivateCountFromMinYmForNewRegistration(req, existing);
            }
        }

        // 월 vs 분기 cross-type 검증 (cutOffYm 이후 구간은 이미 N 처리될 예정이므로 제외하고 검사)
        validateCrossTypeOverlap(req, existing, effectiveTyp, cutOffYm);

        // 3) 월(01)은 종료월 = 시작월 강제
        if ("01".equals(req.getTpwLmtTypCd()) && req.getAmtList() != null) {
            req.getAmtList().forEach(a -> a.setLmtEndYm(a.getLmtSttYm()));
        }

        BuildResult build = buildInserts(req, existing, hasExisting);
        List<SprtLmtReqVO> toInsert = build.rows;
        if (toInsert.isEmpty()) return;

        // edit-3in1 쪽 버전업 N 처리 (기존 로직 그대로)
        if (build.prevSno != null && !build.touchedMngNos.isEmpty()) {
            updateSprtLmtUseYnByMngNos(
                    req.getTpwSvcId(),
                    req.getTpwSvcTypId(),
                    req.getTpwLmtDvsCd(),
                    build.prevSno,
                    new ArrayList<>(build.touchedMngNos)
            );
        }

        insertSprtLmt(toInsert);
    }
    /**
     * 신규등록 모드에서
     *  - 이번에 저장하려는 금액 한도(월/분기)의 전체 구간 중
     *    "가장 이른 시작월(minNewYm)" 을 구해서
     *  - 동일 서비스/서비스유형 + 금액(dvs=01)의 기존 활성 한도 중
     *    시작월이 minNewYm 이상(>=)인 것들을 전부 use_yn='N' 처리한다.
     *
     * 예)
     *   기존 : 2025-01, 2025-02, 2025-03, 2025-04 (월)
     *   신규 : 2025-02 (월)      → minNewYm = 2025-02
     *          → 기존 2025-02,03,04 전부 N 처리
     *
     *   신규 : 2025-01~2025-03 (분기) → minNewYm = 2025-01
     *          → 기존 2025-01 이후 전부 N 처리
     *
     * @return 이번 요청에서 계산된 minNewYm (없으면 null)
     */
    private YearMonth deactivateFromMinYmForNewRegistration(InstReqVO req,
                                                            List<SprtLmtRspVO> existing) {

        // 금액이 아니면 대상 아님
        if (!"01".equals(req.getTpwLmtDvsCd())) {
            return null;
        }
        if (existing == null || existing.isEmpty()) {
            return null;
        }
        List<AmtReqVO> amtList = Optional.ofNullable(req.getAmtList())
                .orElse(Collections.emptyList());
        if (amtList.isEmpty()) {
            return null;
        }

        // 1) 이번 요청(월/분기)에서 가장 이른 시작월(minNewYm) 찾기
        YearMonth minNew = null;
        for (AmtReqVO a : amtList) {
            if (a == null) continue;
            String stt = normalizeYm(a.getLmtSttYm());
            YearMonth ym = toYearMonth(stt);
            if (ym == null) continue;

            if (minNew == null || ym.isBefore(minNew)) {
                minNew = ym;
            }
        }

        if (minNew == null) {
            return null;
        }

        // 2) 기존 활성 금액 한도 중에서,
        //    시작월이 minNewYm 이상(>=)인 것들을 모두 use_yn='N' 대상에 포함
        Map<String, Set<String>> mngNosBySno = new HashMap<>();

        for (SprtLmtRspVO row : existing) {
            if (!"01".equals(row.getTpwLmtDvsCd())) {
                continue;   // 금액 아니면 무시
            }

            YearMonth sttYm = toYearMonth(normalizeYm(row.getLmtSttYm()));
            if (sttYm == null) continue;

            // 기존 시작월이 minNewYm 이상이면 N 처리 대상
            if (!sttYm.isBefore(minNew)) { // sttYm >= minNew
                String sno = row.getSpfnLmtSno();
                String mngNo = row.getSpfnLmtMngNo();
                if (sno == null || mngNo == null) continue;

                mngNosBySno
                        .computeIfAbsent(sno, k -> new HashSet<>())
                        .add(mngNo);
            }
        }

        // 3) sno 별로 묶어서 N 처리 (버전 개념이 다를 수 있으니 sno 단위로 update)
        if (!mngNosBySno.isEmpty()) {
            for (Map.Entry<String, Set<String>> e : mngNosBySno.entrySet()) {
                String sno = e.getKey();
                List<String> mngNos = new ArrayList<>(e.getValue());
                updateSprtLmtUseYnByMngNos(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        req.getTpwLmtDvsCd(),   // 항상 "01"
                        sno,
                        mngNos
                );
            }
        }

        return minNew;
    }

    /**
     * 신규등록 모드에서 (건수 한도용)
     *  - 이번에 저장하려는 건수 한도들의 적용 연월 중
     *    "가장 이른 연월(minNewYm)" 을 구해서
     *  - 동일 서비스/서비스유형 + 건수(dvs=02)의 기존 활성 한도 중
     *    시작월이 minNewYm 이상(>=)인 것들을 전부 use_yn='N' 처리한다.
     */
    private YearMonth deactivateCountFromMinYmForNewRegistration(
            InstReqVO req,
            List<SprtLmtRspVO> existing
    ) {
        // 건수가 아니면 대상 아님
        if (!"02".equals(req.getTpwLmtDvsCd())) {
            return null;
        }
        if (existing == null || existing.isEmpty()) {
            return null;
        }

        List<NcntReqVO> ncntList = Optional.ofNullable(req.getNcntList())
                .orElse(Collections.emptyList());
        if (ncntList.isEmpty()) {
            return null;
        }

        // 1) 이번 요청(건수)에서 가장 이른 연월(minNewYm) 찾기
        YearMonth minNew = null;
        for (NcntReqVO n : ncntList) {
            if (n == null) continue;
            String stt = normalizeYm(n.getLmtSttYm());
            YearMonth ym = toYearMonth(stt);
            if (ym == null) continue;

            if (minNew == null || ym.isBefore(minNew)) {
                minNew = ym;
            }
        }

        if (minNew == null) {
            return null;
        }

        // 2) 기존 활성 건수 한도 중에서,
        //    시작월이 minNewYm 이상(>=)인 것들을 모두 use_yn='N' 대상에 포함
        Map<String, Set<String>> mngNosBySno = new HashMap<>();

        for (SprtLmtRspVO row : existing) {
            // 건수(dvs=02)만 대상
            if (!"02".equals(row.getTpwLmtDvsCd())) {
                continue;
            }

            YearMonth sttYm = toYearMonth(normalizeYm(row.getLmtSttYm()));
            if (sttYm == null) continue;

            if (!sttYm.isBefore(minNew)) { // sttYm >= minNew
                String sno = row.getSpfnLmtSno();
                String mngNo = row.getSpfnLmtMngNo();
                if (sno == null || mngNo == null) continue;

                mngNosBySno
                        .computeIfAbsent(sno, k -> new HashSet<>())
                        .add(mngNo);
            }
        }

        // 3) sno 별로 묶어서 N 처리
        if (!mngNosBySno.isEmpty()) {
            for (Map.Entry<String, Set<String>> e : mngNosBySno.entrySet()) {
                String sno = e.getKey();
                List<String> mngNos = new ArrayList<>(e.getValue());
                updateSprtLmtUseYnByMngNos(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        "02",   // 건수
                        sno,
                        mngNos
                );
            }
        }

        return minNew;
    }
    /** YYYY-MM / YYYYMM → YearMonth (잘못된 형식이면 null) */
    private YearMonth toYearMonth(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;

        String norm = s.replace("-", "");
        if (!norm.matches("\\d{6}")) {
            return null;
        }
        int year = Integer.parseInt(norm.substring(0, 4));
        int month = Integer.parseInt(norm.substring(4, 6));
        return YearMonth.of(year, month);
    }

    /**
     * 같은 서비스/서비스유형(tpwSvcId + tpwSvcTypId) 안에서
     *  - 금액(dvs=01) 이고,
     *  - 기존 데이터 중 "타입이 다른 것(월 vs 분기)"과
     * 신규 요청의 기간이 월 단위로 겹치는지 검사.
     *
     * cutOffYm 이 있으면, 그 이후(>= cutOffYm)의 기존 구간은
     * 이미 use_yn='N' 처리될 예정이므로 겹침 검사에서 제외한다.
     *
     * 겹치면 DomainExceptionCode.VALIDATION_ERROR 던짐.
     */
    private void validateCrossTypeOverlap(InstReqVO req,
                                          List<SprtLmtRspVO> existing,
                                          String effectiveTyp,
                                          YearMonth cutOffYm) {

        // 건수(dvs=02)는 대상 아님
        if (!"01".equals(req.getTpwLmtDvsCd())) {
            return;
        }

        if (existing == null || existing.isEmpty()) {
            return;
        }

        // 1) 기존 데이터 중 "타입이 다른 금액 한도(월 vs 분기)"만 월 단위로 Set에 담기
        Set<Integer> otherMonths = new HashSet<>();
        for (SprtLmtRspVO row : existing) {
            if (!"01".equals(row.getTpwLmtDvsCd())) {
                continue; // 금액 아닌 건수는 무시
            }

            String oldTyp = Optional.ofNullable(row.getTpwLmtTypCd()).orElse("02");
            if (effectiveTyp.equals(oldTyp)) {
                // 같은 타입끼리는 여기서 제외
                continue;
            }

            // 월 타입이면 stt == end, 분기 타입이면 stt~end 전체 월
            YearMonth from = toYearMonth(row.getLmtSttYm());
            YearMonth to   = toYearMonth(
                    "01".equals(oldTyp) ? row.getLmtSttYm() : row.getLmtEndYm()
            );
            if (from == null || to == null) continue;

            for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
                // 🔥 cutOffYm 이후(>= cutOffYm)는 이번 신규 요청에서 N 처리될 예정이므로 무시
                if (cutOffYm != null && !ym.isBefore(cutOffYm)) {
                    continue;
                }
                otherMonths.add(ym.getYear() * 12 + ym.getMonthValue());
            }
        }

        if (otherMonths.isEmpty()) {
            return;
        }

        // 2) 이번에 저장하려는 금액 리스트에서, 타입이 다른 기존 기간과 겹치는 월이 있는지 검사
        List<AmtReqVO> list = Optional.ofNullable(req.getAmtList())
                .orElse(Collections.emptyList());

        for (int i = 0; i < list.size(); i++) {
            AmtReqVO row = list.get(i);

            YearMonth from = toYearMonth(row.getLmtSttYm());
            YearMonth to   = toYearMonth(
                    "01".equals(effectiveTyp) ? row.getLmtSttYm() : row.getLmtEndYm()
            );
            if (from == null || to == null) {
                // 형식이 이상하면 기존 periodValidator에서 걸릴 것이므로 여기서는 패스
                continue;
            }

            for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
                int key = ym.getYear() * 12 + ym.getMonthValue();
                if (otherMonths.contains(key)) {
                    String msg = String.format(
                            "이미 다른 유형의 금액 한도(월/분기)가 설정된 기간과 겹칩니다. (행 %d, %s)",
                            i + 1,
                            ym
                    );
                    throw DomainExceptionCode.VALIDATION_ERROR.newInstance(msg);
                }
            }
        }
    }

    private String currentYYYYMM() {
        LocalDate now = LocalDate.now();
        return String.format("%04d%02d", now.getYear(), now.getMonthValue());
    }
    /**
     * 설정하기(edit-3in1)에서
     * - 날짜/기간은 고정(readOnly)
     * - 금액/건수/지급률만 수정 가능
     *
     * 이라서, 실제로 값이 바뀐 행만 남기고 나머지는 버린다.
     * (→ 안 바뀐 행은 기존 row 그대로 유지, 새 버전 insert 불필요)
     */
    private void filterChangedRowsForEdit(InstReqVO req,
                                          List<SprtLmtRspVO> existing,
                                          String effectiveTyp) {

        final String dvs = req.getTpwLmtDvsCd(); // 01=금액, 02=건수
        if (existing == null) existing = Collections.emptyList();

        // ===== 금액(분기/월) =====
        if ("01".equals(dvs)) {

            //  월 탭인지 여부
            final boolean isMonth = "01".equals(effectiveTyp);

            // 기간(시작/종료) 기준으로 기존 row 인덱스
            Map<PeriodKeyVO, SprtLmtRspVO> existingByPeriod = existing.stream()
                    .filter(r -> "01".equals(r.getTpwLmtDvsCd()))
                    .filter(r -> effectiveTyp.equals(r.getTpwLmtTypCd()))
                    .collect(Collectors.toMap(
                            r -> {
                                String stt = normalizeYm(r.getLmtSttYm());
                                String end = normalizeYm(r.getLmtEndYm());
                                // 월 탭이면 (stt, stt) 로 통일
                                if (isMonth) end = stt;
                                return new PeriodKeyVO(stt, end);
                            },
                            r -> r,
                            (a, b) -> a
                    ));

            List<AmtReqVO> src = Optional.ofNullable(req.getAmtList())
                    .orElse(Collections.emptyList());

            List<AmtReqVO> changed = src.stream()
                    .filter(Objects::nonNull)
                    .filter(a -> {
                        String stt = normalizeYm(a.getLmtSttYm());
                        // 요청에서는 endYm 이 거의 null → 월 탭이면 stt 로 강제
                        String end = normalizeYm(a.getLmtEndYm());
                        if (isMonth) end = stt;
                        PeriodKeyVO key = new PeriodKeyVO(stt, end);

                        SprtLmtRspVO prev = existingByPeriod.get(key);

                        // 완전 신규 기간 → 무조건 insert 대상
                        if (prev == null) return true;

                        // 금액 변경 여부만 비교
                        return !Objects.equals(a.getTgtAdptVal(), prev.getTgtAdptVal());
                    })
                    .collect(Collectors.toList());

            req.setAmtList(changed);
            return;
        }

        // ========== 건수 ==========
        Map<String, SprtLmtRspVO> existingByYm = existing.stream()
                .filter(r -> "02".equals(r.getTpwLmtDvsCd()))
                .collect(Collectors.toMap(
                        r -> normalizeYm(r.getLmtSttYm()), // YYYYMM 로 정규화
                        r -> r,
                        (a, b) -> a
                ));

        List<NcntReqVO> src = Optional.ofNullable(req.getNcntList())
                .orElse(Collections.emptyList());

        List<NcntReqVO> changed = src.stream()
                .filter(Objects::nonNull)
                .filter(n -> {
                    String ym = normalizeYm(n.getLmtSttYm());
                    if (ym == null) return true; // 형식 이상하면 일단 insert 대상으로

                    SprtLmtRspVO prev = existingByYm.get(ym);
                    if (prev == null) return true; // 완전 신규 연월

                    // 최소/최대/지급률 중 하나라도 달라지면 변경
                    if (!Objects.equals(n.getMinCndtVal(), prev.getMinCndtVal())) return true;
                    if (!Objects.equals(n.getMaxCndtVal(), prev.getMaxCndtVal())) return true;
                    if (!Objects.equals(n.getTgtAdptVal(), prev.getTgtAdptVal())) return true;

                    // 전부 동일 → 변경 없음
                    return false;
                })
                .collect(Collectors.toList());

        req.setNcntList(changed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readNextMngNo(int count) {
        return sprtLmtMapper.readNextMngNo(count);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuarterRangeVO> readQuarterRanges(String tpwSvcId, String tpwSvcTypId) {
        return sprtLmtMapper.readQuarterRanges(tpwSvcId, tpwSvcTypId);
    }

    @Override
    @Transactional
    public void insertSprtLmt(List<SprtLmtReqVO> req) {
        sprtLmtMapper.insertSprtLmt(req);
    }

    /**
     * 금액/건수 공통 insert 빌더
     *
     * 규칙
     *  - spfn_lmt_sno : 한 번 저장 시 전체 행 동일 (버전)
     *  - spfn_lmt_mng_no :
     *      · [edit-3in1] 동일 서비스/유형 + 동일 기간(또는 동일 관리번호) 이면 → 기존 관리번호 재사용
     *      · [신규등록]    이번에 들어온 행은 모두 신규로 보고 → 새 관리번호 채번
     *
     * 또한, 이전 버전 N 처리를 위해
     *  - prevSno        : 직전 버전 sno
     *  - touchedMngNos  : 이번 저장에서 사용된 관리번호 집합
     * 을 같이 리턴한다.
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

        // ===== sno 계산 =====
        boolean editMode = "edit-3in1".equalsIgnoreCase(
                Optional.ofNullable(req.getMode()).orElse("")
        );

        String prevSno;
        String nextSno;

        if (editMode) {
            // 설정하기(수정) 모드 → 기존 sno 기준으로 버전 업
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

            prevSno = (maxSnoInt > 0) ? formatSno(maxSnoInt) : null;
            nextSno = formatSno(maxSnoInt + 1);
        } else {
            // 🔥 신규등록 모드 → 일련번호는 항상 1부터
            prevSno = null;              // 이전 버전 개념 없음
            nextSno = formatSno(1);      // "0000000001"
        }

        List<SprtLmtReqVO> out = new ArrayList<>(needCount);
        Set<String> touchedMngNos = new HashSet<>();

        // ============== 1) 금액 한도 (분기/월) ==============
        if (isAmount) {

            Map<PeriodKeyVO, String> mngNoByPeriod = new HashMap<>();
            Set<PeriodKeyVO> newKeys = new LinkedHashSet<>();

            if (editMode && hasExisting && "01".equals(curDvs)) {
                //  설정하기(edit)일 때만 "기간 동일하면 기존 관리번호 재사용"
                for (SprtLmtRspVO row : existing) {
                    if (!dvs.equals(row.getTpwLmtDvsCd())) continue;
                    if (!nextTyp.equals(row.getTpwLmtTypCd())) continue;

                    PeriodKeyVO key = new PeriodKeyVO(
                            normalizeYm(row.getLmtSttYm()),
                            normalizeYm(row.getLmtEndYm())
                    );
                    mngNoByPeriod.putIfAbsent(key, row.getSpfnLmtMngNo());
                }

                // 기존에 없는 기간만 새 관리번호 대상
                for (AmtReqVO a : amtSrc) {
                    PeriodKeyVO key = new PeriodKeyVO(
                            normalizeYm(a.getLmtSttYm()),
                            normalizeYm(a.getLmtEndYm())
                    );
                    if (!mngNoByPeriod.containsKey(key)) {
                        newKeys.add(key);
                    }
                }
            } else {
                //  신규등록(또는 기존이 건수만 있는 경우 등)에서는
                //    → 모든 기간에 대해 새 관리번호를 채번
                for (AmtReqVO a : amtSrc) {
                    PeriodKeyVO key = new PeriodKeyVO(
                            normalizeYm(a.getLmtSttYm()),
                            normalizeYm(a.getLmtEndYm())
                    );
                    newKeys.add(key);
                }
            }

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

            for (AmtReqVO a : amtSrc) {
                PeriodKeyVO key = new PeriodKeyVO(
                        normalizeYm(a.getLmtSttYm()),
                        normalizeYm(a.getLmtEndYm())
                );
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
                        "01",
                        nextTyp,
                        normalizeYm(a.getLmtSttYm()),
                        normalizeYm(a.getLmtEndYm()),
                        0,
                        0,
                        a.getTgtAdptVal(),
                        "Y"
                ));
            }

            return new BuildResult(out, prevSno, touchedMngNos);
        }

        // ============== 2) 건수 한도 ==============
        // 규칙
        //  - [신규등록] : 행마다 무조건 새 관리번호 (날짜 같아도 전부 별도)
        //  - [edit-3in1] :
        //        · 요청 VO(NcntReqVO)에 spfnLmtMngNo 가 있으면 → 그 번호 재사용
        //        · 없으면 새 관리번호 채번
        List<String> generated = Collections.emptyList();

        if (editMode && hasExisting && "02".equals(curDvs)) {
            int needNew = (int) ncntSrc.stream()
                    .filter(n -> n.getSpfnLmtMngNo() == null || n.getSpfnLmtMngNo().isBlank())
                    .count();

            if (needNew > 0) {
                generated = readNextMngNo(needNew);
            }
        } else {
            //  신규등록이거나 기존이 금액만 있는 경우 → 모든 행 신규 관리번호
            if (!ncntSrc.isEmpty()) {
                generated = readNextMngNo(ncntSrc.size());
            }
        }

        Iterator<String> it = generated.iterator();

        for (NcntReqVO n : ncntSrc) {
            String mngNo = n.getSpfnLmtMngNo();
            if (mngNo == null || mngNo.isBlank()) {
                if (!it.hasNext()) {
                    throw new IllegalStateException("관리번호 개수가 부족합니다.");
                }
                mngNo = it.next();
            }

            touchedMngNos.add(mngNo);

            String sttYm = normalizeYm(n.getLmtSttYm());
            String endYm = normalizeYm(n.getLmtEndYm());
            if (sttYm == null) {
                sttYm = currentYYYYMM();   // 실제로는 periodValidator 에서 형식을 막고 있음
            }
            if (endYm == null) {
                endYm = sttYm;            // 건수는 월 단위라고 가정
            }

            out.add(new SprtLmtReqVO(
                    req.getTpwSvcId(),
                    req.getTpwSvcTypId(),
                    mngNo,
                    nextSno,
                    "02",              // dvs: 건수
                    nextTyp,           // typ: 01/02 중 정책에 따라
                    sttYm,
                    endYm,
                    n.getMinCndtVal(),
                    n.getMaxCndtVal(),
                    n.getTgtAdptVal(),
                    "Y"
            ));
        }

        return new BuildResult(out, prevSno, touchedMngNos);
    }

    @Override
    @Transactional(readOnly = true)
    public SprtLmtExistResVO checkExist(String tpwSvcId, String tpwSvcTypId) {
        SprtLmtExistResVO res = new SprtLmtExistResVO();

        // 1) 해당 서비스+서비스유형의 기존 분기 목록 (분기 신규 등록 시 겹침 체크용)
        List<QuarterRangeVO> qtRanges = sprtLmtMapper.readQuarterRanges(tpwSvcId, tpwSvcTypId);
        res.setQtRanges(qtRanges);

        // 🔥 2) "이 서비스/유형에 한도 데이터가 하나라도 있는지"로 exists 판단
        boolean anyExists = hasExistingLimit(tpwSvcId, tpwSvcTypId);
        res.setExists(anyExists);

        // 3) 서비스 단위 한도유형(금액-분기/월/건수) 정보
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
     * 저장 시 서비스 단위 한도유형(금액-분기/월/건수) 강제 일치 검증
     *  - 이 서비스에 기존 데이터가 없으면 패스
     *  - 하나의 유형만 존재하면, 그 유형과 동일해야만 신규 저장 허용
     *  - 여러 유형이 이미 섞여 있으면, 먼저 기존 데이터부터 정리해야 함
     */
    @Override
    @Transactional(readOnly = true)
    public void validateSvcLimitKind(String tpwSvcId, String tpwLmtDvsCd, String tpwLmtTypCd) {
        List<SprtLmtKindVO> kinds = sprtLmtMapper.readSvcLmtKinds(tpwSvcId);
        if (kinds == null || kinds.isEmpty()) {
            // 아직 이 서비스에 아무 한도도 없으면 제약 없음
            return;
        }

        // 이 서비스에 이미 등록된 dvs(01=금액, 02=건수) 집합
        boolean hasAmount = kinds.stream().anyMatch(k -> "01".equals(k.getDvsCd()));
        boolean hasCount  = kinds.stream().anyMatch(k -> "02".equals(k.getDvsCd()));

        // 금액/건수는 섞지 않게 하고 싶으면 여기서 막기
        if (hasAmount && hasCount) {
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance(
                    "하나의 서비스에는 금액/건수 유형을 동시에 설정할 수 없습니다. 기존 한도유형을 먼저 정리해 주세요."
            );
        }

        // 이미 등록된 게 전부 금액인데, 이번에 건수를 넣으려고 하면 막기 (반대도 동일)
        String existingDvs = kinds.get(0).getDvsCd(); // 전부 같은 dvs 라는 전제
        if (!Objects.equals(existingDvs, tpwLmtDvsCd)) {
            String msgKind = "01".equals(existingDvs) ? "금액" : "건수";
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance(
                    "해당 서비스는 이미 [" + msgKind + "] 한도로 설정되어 있습니다. " +
                            "기존 한도유형(금액/건수)과 동일한 유형으로만 추가할 수 있습니다."
            );
        }
    }

    private String toKindLabel(String dvs, String typ) {
        if ("01".equals(dvs) && "02".equals(typ)) return "금액-분기";
        if ("01".equals(dvs) && "01".equals(typ)) return "금액-월";
        if ("02".equals(dvs))                    return "건수";
        return dvs + "-" + typ;
    }

    // ===================== 실제 저장 메서드 예시 =====================




    /** YYYY-MM / YYYYMM → YYYYMM */
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

    private String formatSno(int sno) {
        if (sno < 0) sno = 0;
        return String.format("%010d", sno);
    }

    /**
     * buildInserts 결과 묶음
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