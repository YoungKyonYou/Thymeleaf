package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
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

@RequiredArgsConstructor
@Service
public class SprtLmtServiceImpl implements SprtLmtService {

    private final SprtLmtMapper sprtLmtMapper;
    private final SprtLmtPeriodValidator periodValidator; // 기간/중복 검증 컴포넌트

    /* ===================== 모달 초기화 ===================== */

    /**
     * 한도 설정 모달(3in1) 진입 시 기본값(분기/월/건수 탭용 빈 Row) 생성.
     *
     * @return 분기/월/건수 리스트가 비어있는 기본 모달 VO
     */
    @Override
    @Transactional(readOnly = true)
    public SprtLmtModalVO initModal() {
        return new SprtLmtModalVO(
                initQuarterList(),
                initMonList(),
                initNcntList()
        );
    }

    /**
     * 설정하기(3in1) 모달 진입 시, 해당 서비스/서비스유형에 대한
     * 기존 한도 내역을 조회하고, 없으면 기본값으로 초기화해서 내려준다.
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return 모달에 뿌릴 분기/월/건수 리스트 및 현재 한도 구분/유형 정보
     */
    @Override
    @Transactional(readOnly = true)
    public SprtLmtModalDtlVO readSprtLmtByTpwSvcTypId(String tpwSvcId, String tpwSvcTypId) {
        List<SprtLmtRspVO> rows = readSprtLmtDtlByTpwSvc(tpwSvcId, tpwSvcTypId, "Y");

        if (rows == null || rows.isEmpty()) {
            SprtLmtModalVO m = initModal();
            return new SprtLmtModalDtlVO(m.getQt(), m.getMon(), m.getArr(), "01", "01");
        }

        List<SprtLmtRspVO> qtRows = rows.stream()
                .filter(r -> "01".equals(r.getTpwLmtDvsCd()))
                .filter(r -> "02".equals(r.getTpwLmtTypCd()))
                .collect(Collectors.toList());

        List<SprtLmtRspVO> monRows = rows.stream()
                .filter(r -> "01".equals(r.getTpwLmtDvsCd()))
                .filter(r -> "01".equals(r.getTpwLmtTypCd()))
                .collect(Collectors.toList());

        List<SprtLmtRspVO> ncntRows = rows.stream()
                .filter(r -> "02".equals(r.getTpwLmtDvsCd()))
                .collect(Collectors.toList());

        List<AmtReqVO> qt = qtRows.isEmpty()
                ? initQuarterList()
                : qtRows.stream()
                .map(a -> new AmtReqVO(
                        a.getSpfnLmtMngNo(),
                        a.getSpfnLmtSno(),
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        a.getTgtAdptVal()))
                .collect(Collectors.toList());

        List<AmtReqVO> mon = monRows.isEmpty()
                ? initMonList()
                : monRows.stream()
                .map(a -> new AmtReqVO(
                        a.getSpfnLmtMngNo(),
                        a.getSpfnLmtSno(),
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        a.getTgtAdptVal()))
                .collect(Collectors.toList());

        List<NcntReqVO> ncnt = ncntRows.isEmpty()
                ? initNcntList()
                : ncntRows.stream()
                .map(a -> new NcntReqVO(
                        a.getSpfnLmtMngNo(),
                        a.getSpfnLmtSno(),
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        a.getMinCndtVal(),
                        a.getMaxCndtVal(),
                        a.getTgtAdptVal()))
                .collect(Collectors.toList());

        String dvs = rows.get(0).getTpwLmtDvsCd();
        String typ = rows.get(0).getTpwLmtTypCd();

        return new SprtLmtModalDtlVO(qt, mon, ncnt, dvs, typ);
    }

    /* ===================== 트랜잭션/플래그 ===================== */

    /**
     * 거래건수제한적용여부 플래그 수정.
     *
     * @param tpwSvcTypId 서비스유형 ID
     * @param adptYn      적용 여부(Y/N)
     */
    @Override
    @Transactional
    public void updateTrdNcntLtnAdptYn(String tpwSvcTypId, String adptYn) {
        sprtLmtMapper.updateTrdNcntLtnAdptYn(tpwSvcTypId, adptYn);
    }

    /**
     * 거래건수제한적용여부 플래그 단건 조회.
     *
     * @param tpwSvcTypId 서비스유형 ID
     * @return Y / N / null
     */
    @Override
    @Transactional(readOnly = true)
    public String readTrdNcntAdptYn(String tpwSvcTypId) {
        return sprtLmtMapper.readTrdNcntAdptYn(tpwSvcTypId);
    }

    /**
     * 거래 건수 한도 적용 여부 기반 금액 한도 저장 금지 서버 검증.
     * - 건수제한적용여부가 Y인 경우 금액 한도 저장을 막는다.
     */
    private void validateTrdNcntAdptYnForSave(InstReqVO req) {
        if (req == null) return;

        if (!"01".equals(req.getTpwLmtDvsCd())) {
            return;
        }

        String tpwSvcTypId = req.getTpwSvcTypId();
        if (tpwSvcTypId == null || tpwSvcTypId.isBlank()) {
            return;
        }

        String adptYn = readTrdNcntAdptYn(tpwSvcTypId);
        if ("Y".equalsIgnoreCase(adptYn)) {
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance(
                    "해당 서비스/서비스유형은 \"거래건수제한적용여부\"가 Y로 설정되어 있습니다. " +
                            "건수 한도 탭에서만 설정할 수 있습니다. 건수 한도 대신 금액 한도를 사용하려면, " +
                            "서비스유형 관리 화면에서 거래건수제한적용여부를 N으로 변경한 후 다시 시도해주세요."
            );
        }
    }

    /* ===================== 페이징/목록 조회 ===================== */

    /**
     * 지원한도 관리 메인 그리드 페이징 조회.
     *
     * @param req 검색 조건(페이지/사이즈 포함)
     * @return PageDataVO 형태의 페이징 결과
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
     * 지원한도 메인 그리드 목록 조회(페이징 X).
     */
    @Override
    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtPtList(SprtLmtSrchReqVO req) {
        return sprtLmtMapper.readSprtLmtPtList(req);
    }

    /**
     * 지원한도 메인 그리드 전체 건수 조회.
     */
    @Override
    @Transactional(readOnly = true)
    public long readSprtLmtPtListCnt(SprtLmtSrchReqVO req) {
        return sprtLmtMapper.readSprtLmtPtListCnt(req);
    }

    /**
     * 서비스/서비스유형 기준 한도 설정 존재 여부 카운트 조회.
     */
    @Override
    @Transactional(readOnly = true)
    public Integer readSprtLmtCntBySvcTyp(String tpwSvcId, String tpwSvcTypId) {
        return sprtLmtMapper.readSprtLmtCntBySvcTyp(tpwSvcId, tpwSvcTypId);
    }

    /**
     * 서비스/서비스유형 기준 한도 설정 존재 여부 boolean 변환.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasExistingLimit(String tpwSvcId, String tpwSvcTypId) {
        Integer cnt = readSprtLmtCntBySvcTyp(tpwSvcId, tpwSvcTypId);
        return cnt != null && cnt > 0;
    }

    /**
     * 지원한도 상세(금액/건수 전체) 조회.
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @param useYn       사용 여부(Y/N)
     */
    @Override
    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(String tpwSvcId,
                                                     String tpwSvcTypId,
                                                     String useYn) {
        return sprtLmtMapper.readSprtLmtDtlByTpwSvc(tpwSvcId, tpwSvcTypId, useYn);
    }

    /* ===================== N 처리 / 저장 ===================== */

    /**
     * 이전 SNO에 해당하는 관리번호들의 USE_YN을 일괄 N 처리.
     * (신규 SNO로 재등록할 때, 이전 버전 이력 비활성화용)
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @param tpwLmtDvsCd 한도구분(01:금액, 02:건수)
     * @param prevSno     이전 일련번호
     * @param mngNos      비활성화할 관리번호 목록
     */
    @Override
    @Transactional
    public void updateSprtLmtUseYnByMngNos(String tpwSvcId,
                                           String tpwSvcTypId,
                                           String tpwLmtDvsCd,
                                           String prevSno,
                                           List<String> mngNos) {
        if (prevSno == null || mngNos == null || mngNos.isEmpty()) {
            return;
        }
        sprtLmtMapper.updateSprtLmtUseYnByMngNo(tpwSvcId, tpwSvcTypId, tpwLmtDvsCd, prevSno, mngNos);
    }

    /**
     * 금액/건수 한도 저장(3in1 저장 버튼).
     * - edit-3in1 모드인지 신규 모드인지에 따라 이력 관리 방식이 달라진다.
     * - 기간/중복/유형 제약을 모두 검증한 후 insert 쿼리를 빌드한다.
     *
     * @param req 통합 저장 요청 VO
     */
    @Override
    @Transactional
    public void insertSprtLmtAmt(InstReqVO req) {
        if (req == null) return;

        // 건수 제한 적용 여부 검증 (금액 한도 저장 금지 조건)
        validateTrdNcntAdptYnForSave(req);

        // 기간/중복 검증 (Cross Type 포함)
        periodValidator.validate(req);

        final String effectiveTyp = "01".equals(req.getTpwLmtDvsCd())
                ? req.getTpwLmtTypCd()
                : Optional.ofNullable(req.getTpwLmtTypCd()).orElse("02");

        final boolean editMode =
                "edit-3in1".equalsIgnoreCase(Optional.ofNullable(req.getMode()).orElse(""));

        List<SprtLmtRspVO> existing =
                readSprtLmtDtlByTpwSvc(req.getTpwSvcId(), req.getTpwSvcTypId(), "Y");
        boolean hasExisting = !existing.isEmpty();

        YearMonth cutOffYm = null;

        if (editMode) {
            // 수정 모드일 경우, 변경된 Row만 추려서 저장
            filterChangedRowsForEdit(req, existing, effectiveTyp);
            if ((req.getAmtList() == null || req.getAmtList().isEmpty())
                    && (req.getNcntList() == null || req.getNcntList().isEmpty())) {
                // 모두 기존과 동일하면 아무 것도 안 함
                return;
            }
        } else if (hasExisting) {
            boolean hasAmt = req.getAmtList() != null && !req.getAmtList().isEmpty();
            boolean hasNcnt = req.getNcntList() != null && !req.getNcntList().isEmpty();

            // 신규 등록인데 기존 한도가 있으면, 신규 시작 월 기준으로 이후 이력 N 처리
            if (("01".equals(req.getTpwLmtDvsCd()) && hasAmt)
                    || ("02".equals(req.getTpwLmtDvsCd()) && hasNcnt)) {

                cutOffYm = deactivateAllTypesFromMinYmForNewRegistration(req, existing);
            }
        }

        // 월/분기 유형 간 교차 중복 검증
        validateCrossTypeOverlap(req, existing, effectiveTyp, cutOffYm);

        // 월 단위 금액일 경우 종료일 = 시작일로 강제 세팅
        if ("01".equals(req.getTpwLmtTypCd()) && req.getAmtList() != null) {
            req.getAmtList().forEach(a -> a.setLmtEndYm(a.getLmtSttYm()));
        }

        BuildResult build = buildInserts(req, existing, hasExisting);
        List<SprtLmtReqVO> toInsert = build.rows;
        if (toInsert.isEmpty()) return;

        //  관리번호별 이전 SNO를 기준으로 N 처리
        if (build.prevSnoByMngNo != null && !build.prevSnoByMngNo.isEmpty()) {
            for (Map.Entry<String, String> e : build.prevSnoByMngNo.entrySet()) {
                String mngNo = e.getKey();
                String prevSno = e.getValue();
                if (prevSno == null || prevSno.isBlank()) {
                    continue;
                }
                updateSprtLmtUseYnByMngNos(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        req.getTpwLmtDvsCd(),
                        prevSno,
                        Collections.singletonList(mngNo)
                );
            }
        }

        // 신규 이력 insert
        insertSprtLmt(toInsert);
    }

    /**
     * 신규 등록(신규 SNO) 시, 신규 시작월 이후의 기존 이력을 모두 비활성화(N 처리)한다.
     *
     * @return 신규 시작 월(YearMonth) – 교차 중복 검증 시 cut-off 기준으로 사용
     */
    private YearMonth deactivateAllTypesFromMinYmForNewRegistration(
            InstReqVO req,
            List<SprtLmtRspVO> existing
    ) {
        if (existing == null || existing.isEmpty()) {
            return null;
        }

        YearMonth minNew = null;

        if ("01".equals(req.getTpwLmtDvsCd())) {
            List<AmtReqVO> amtList = Optional.ofNullable(req.getAmtList())
                    .orElse(Collections.emptyList());

            for (AmtReqVO a : amtList) {
                if (a == null) continue;
                String stt = normalizeYm(a.getLmtSttYm());
                YearMonth ym = toYearMonth(stt);
                if (ym == null) continue;
                if (minNew == null || ym.isBefore(minNew)) {
                    minNew = ym;
                }
            }
        } else if ("02".equals(req.getTpwLmtDvsCd())) {
            List<NcntReqVO> ncntList = Optional.ofNullable(req.getNcntList())
                    .orElse(Collections.emptyList());

            for (NcntReqVO n : ncntList) {
                if (n == null) continue;
                String stt = normalizeYm(n.getLmtSttYm());
                YearMonth ym = toYearMonth(stt);
                if (ym == null) continue;
                if (minNew == null || ym.isBefore(minNew)) {
                    minNew = ym;
                }
            }
        }

        if (minNew == null) {
            return null;
        }

        // dvsCd + sno 기준으로 관리번호 묶어서 비활성화
        Map<String, Map<String, Set<String>>> mngNosByDvsAndSno = new HashMap<>();

        for (SprtLmtRspVO row : existing) {
            YearMonth sttYm = toYearMonth(normalizeYm(row.getLmtSttYm()));
            if (sttYm == null) continue;

            if (!sttYm.isBefore(minNew)) {
                String dvsCd = row.getTpwLmtDvsCd();
                String sno = row.getSpfnLmtSno();
                String mngNo = row.getSpfnLmtMngNo();
                if (dvsCd == null || sno == null || mngNo == null) continue;

                mngNosByDvsAndSno
                        .computeIfAbsent(dvsCd, k -> new HashMap<>())
                        .computeIfAbsent(sno, k -> new HashSet<>())
                        .add(mngNo);
            }
        }

        if (!mngNosByDvsAndSno.isEmpty()) {
            for (Map.Entry<String, Map<String, Set<String>>> e : mngNosByDvsAndSno.entrySet()) {
                String dvsCd = e.getKey();
                Map<String, Set<String>> bySno = e.getValue();

                for (Map.Entry<String, Set<String>> e2 : bySno.entrySet()) {
                    String sno = e2.getKey();
                    List<String> mngNos = new ArrayList<>(e2.getValue());

                    updateSprtLmtUseYnByMngNos(
                            req.getTpwSvcId(),
                            req.getTpwSvcTypId(),
                            dvsCd,
                            sno,
                            mngNos
                    );
                }
            }
        }

        return minNew;
    }

    /**
     * yyyyMM 또는 yyyy-MM 문자열을 YearMonth로 변환.
     * 형식이 맞지 않으면 null 리턴.
     */
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
     * 금액 한도 월/분기 유형 간 교차 중복 검증.
     * - 이미 다른 유형(월 ↔ 분기)으로 설정된 기간과 겹치는지 체크한다.
     *
     * @param effectiveTyp 현재 저장하려는 금액 한도 유형(01:월, 02:분기)
     * @param cutOffYm     신규 시작 월 이후는 이미 N 처리되었으므로 검증 제외 기준
     */
    private void validateCrossTypeOverlap(InstReqVO req,
                                          List<SprtLmtRspVO> existing,
                                          String effectiveTyp,
                                          YearMonth cutOffYm) {

        if (!"01".equals(req.getTpwLmtDvsCd())) {
            return;
        }

        if (existing == null || existing.isEmpty()) {
            return;
        }

        // 다른 유형(월 ↔ 분기)의 월 집합 수집
        Set<Integer> otherMonths = new HashSet<>();
        for (SprtLmtRspVO row : existing) {
            if (!"01".equals(row.getTpwLmtDvsCd())) {
                continue;
            }

            String oldTyp = Optional.ofNullable(row.getTpwLmtTypCd()).orElse("02");
            if (effectiveTyp.equals(oldTyp)) {
                continue;
            }

            YearMonth from = toYearMonth(row.getLmtSttYm());
            YearMonth to = toYearMonth(
                    "01".equals(oldTyp) ? row.getLmtSttYm() : row.getLmtEndYm()
            );
            if (from == null || to == null) continue;

            for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
                if (cutOffYm != null && !ym.isBefore(cutOffYm)) {
                    continue;
                }
                otherMonths.add(ym.getYear() * 12 + ym.getMonthValue());
            }
        }

        if (otherMonths.isEmpty()) {
            return;
        }

        List<AmtReqVO> list = Optional.ofNullable(req.getAmtList())
                .orElse(Collections.emptyList());

        // 현재 입력값이 기존 다른 유형 기간과 겹치는지 체크
        for (int i = 0; i < list.size(); i++) {
            AmtReqVO row = list.get(i);

            YearMonth from = toYearMonth(row.getLmtSttYm());
            YearMonth to = toYearMonth(
                    "01".equals(effectiveTyp) ? row.getLmtSttYm() : row.getLmtEndYm()
            );
            if (from == null || to == null) {
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

    /**
     * 현재 시스템 날짜를 기준으로 yyyyMM 문자열 리턴.
     */
    private String currentYYYYMM() {
        LocalDate now = LocalDate.now();
        return String.format("%04d%02d", now.getYear(), now.getMonthValue());
    }

    /**
     * edit-3in1 모드일 때, 기존 값과 동일한 행은 제외하고
     * 실제로 변경된 Row만 req에 남겨서 insert 되도록 필터링한다.
     */
    private void filterChangedRowsForEdit(InstReqVO req,
                                          List<SprtLmtRspVO> existing,
                                          String effectiveTyp) {

        final String dvs = req.getTpwLmtDvsCd();
        if (existing == null) existing = Collections.emptyList();

        if ("01".equals(dvs)) {

            final boolean isMonth = "01".equals(effectiveTyp);

            Map<PeriodKeyVO, SprtLmtRspVO> existingByPeriod = existing.stream()
                    .filter(r -> "01".equals(r.getTpwLmtDvsCd()))
                    .filter(r -> effectiveTyp.equals(r.getTpwLmtTypCd()))
                    .collect(Collectors.toMap(
                            r -> {
                                String stt = normalizeYm(r.getLmtSttYm());
                                String end = normalizeYm(r.getLmtEndYm());
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
                        String end = normalizeYm(a.getLmtEndYm());
                        if (isMonth) end = stt;
                        PeriodKeyVO key = new PeriodKeyVO(stt, end);

                        SprtLmtRspVO prev = existingByPeriod.get(key);

                        if (prev == null) return true;
                        return !Objects.equals(a.getTgtAdptVal(), prev.getTgtAdptVal());
                    })
                    .collect(Collectors.toList());

            req.setAmtList(changed);
            return;
        }

        Map<String, SprtLmtRspVO> existingByYm = existing.stream()
                .filter(r -> "02".equals(r.getTpwLmtDvsCd()))
                .collect(Collectors.toMap(
                        r -> normalizeYm(r.getLmtSttYm()),
                        r -> r,
                        (a, b) -> a
                ));

        List<NcntReqVO> src = Optional.ofNullable(req.getNcntList())
                .orElse(Collections.emptyList());

        List<NcntReqVO> changed = src.stream()
                .filter(Objects::nonNull)
                .filter(n -> {
                    String ym = normalizeYm(n.getLmtSttYm());
                    if (ym == null) return true;

                    SprtLmtRspVO prev = existingByYm.get(ym);
                    if (prev == null) return true;

                    if (!Objects.equals(n.getMinCndtVal(), prev.getMinCndtVal())) return true;
                    if (!Objects.equals(n.getMaxCndtVal(), prev.getMaxCndtVal())) return true;
                    if (!Objects.equals(n.getTgtAdptVal(), prev.getTgtAdptVal())) return true;

                    return false;
                })
                .collect(Collectors.toList());

        req.setNcntList(changed);
    }

    /**
     * 관리번호 시퀀스를 count 개만큼 미리 조회.
     * - 금액/건수 한도 이력 insert 시 내부적으로 사용.
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> readNextMngNo(int count) {
        return sprtLmtMapper.readNextMngNo(count);
    }

    /**
     * 서비스/서비스유형 기준 분기별 한도 설정 범위 조회.
     * - 3in1 모달 진입 전 사전 체크용.
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuarterRangeVO> readQuarterRanges(String tpwSvcId, String tpwSvcTypId) {
        return sprtLmtMapper.readQuarterRanges(tpwSvcId, tpwSvcTypId);
    }

    /**
     * 지원한도 이력 일괄 insert.
     *
     * @param req insert 대상 레코드 목록
     */
    @Override
    @Transactional
    public void insertSprtLmt(List<SprtLmtReqVO> req) {
        sprtLmtMapper.insertSprtLmt(req);
    }

    /**
     * 금액/건수 한도 입력값을 기반으로 실제 insert 대상 레코드와
     * 관리번호별 이전 SNO(prevSnoByMngNo)를 빌드한다.
     *
     * 신규 모드:
     *   - 기존 이력이 있어도 모든 행 SNO = 1
     *
     * 수정 모드(edit-3in1):
     *   - 기존 이력이 있는 행(기간/관리번호)은 이전 SNO + 1 로 새 버전 생성
     *   - 완전 신규 행(이전에 없던 기간/관리번호)은 SNO = 1
     */
    private BuildResult buildInserts(InstReqVO req,
                                     List<SprtLmtRspVO> existing,
                                     boolean hasExisting) {

        final String dvs = req.getTpwLmtDvsCd();
        final boolean isAmount = "01".equals(dvs);

        final List<AmtReqVO> amtSrc =
                Optional.ofNullable(req.getAmtList()).orElse(Collections.emptyList());
        final List<NcntReqVO> ncntSrc =
                Optional.ofNullable(req.getNcntList()).orElse(Collections.emptyList());

        final int needCount = isAmount ? amtSrc.size() : ncntSrc.size();
        if (needCount == 0) {
            return new BuildResult(Collections.emptyList(), Collections.emptyMap());
        }

        final String curDvs = hasExisting ? existing.get(0).getTpwLmtDvsCd() : null;
        final String curTyp = hasExisting ? existing.get(0).getTpwLmtTypCd() : null;

        final String nextTyp = isAmount
                ? req.getTpwLmtTypCd()
                : Optional.ofNullable(req.getTpwLmtTypCd())
                .orElse(curTyp != null ? curTyp : "02");

        boolean editMode = "edit-3in1".equalsIgnoreCase(
                Optional.ofNullable(req.getMode()).orElse("")
        );

        List<SprtLmtReqVO> out = new ArrayList<>(needCount);
        Map<String, String> prevSnoByMngNo = new HashMap<>();

        /* ===================== 금액 한도 (월/분기) ===================== */
        if (isAmount) {

            Map<PeriodKeyVO, String> mngNoByPeriod = new HashMap<>();
            Map<PeriodKeyVO, String> prevSnoByPeriod = new HashMap<>();
            Set<PeriodKeyVO> newKeys = new LinkedHashSet<>();

            if (editMode && hasExisting && "01".equals(curDvs)) {
                // 수정 모드: 기존 동일 기간은 관리번호 & 이전 SNO 기억
                for (SprtLmtRspVO row : existing) {
                    if (!dvs.equals(row.getTpwLmtDvsCd())) continue;
                    if (!nextTyp.equals(row.getTpwLmtTypCd())) continue;

                    PeriodKeyVO key = new PeriodKeyVO(
                            normalizeYm(row.getLmtSttYm()),
                            normalizeYm(row.getLmtEndYm())
                    );
                    if (!mngNoByPeriod.containsKey(key)) {
                        mngNoByPeriod.put(key, row.getSpfnLmtMngNo());
                        prevSnoByPeriod.put(key, row.getSpfnLmtSno());
                    }
                }

                // 새 기간은 관리번호 신규 발급 대상
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
                // 신규 모드: 모든 기간이 신규(관리번호만 발급받으면 됨, SNO는 전부 1)
                for (AmtReqVO a : amtSrc) {
                    PeriodKeyVO key = new PeriodKeyVO(
                            normalizeYm(a.getLmtSttYm()),
                            normalizeYm(a.getLmtEndYm())
                    );
                    newKeys.add(key);
                }
            }

            // 신규 기간에 대해 관리번호 발급
            if (!newKeys.isEmpty()) {
                List<String> newMngNos = readNextMngNo(newKeys.size());
                Iterator<String> it = newMngNos.iterator();
                for (PeriodKeyVO key : newKeys) {
                    if (!it.hasNext()) {
                        throw new IllegalStateException("관리번호 개수가 부족합니다.");
                    }
                    mngNoByPeriod.put(key, it.next());
                    // 신규 기간은 이전 SNO 없음
                }
            }

            // insert 레코드 생성 + 관리번호별 이전 SNO 수집
            for (AmtReqVO a : amtSrc) {
                PeriodKeyVO key = new PeriodKeyVO(
                        normalizeYm(a.getLmtSttYm()),
                        normalizeYm(a.getLmtEndYm())
                );
                String mngNo = mngNoByPeriod.get(key);
                if (mngNo == null) {
                    throw new IllegalStateException("기간(" + key + ")에 대한 관리번호가 없습니다.");
                }

                String rowSno;

                if (editMode) {
                    // 수정 모드: 해당 기간에 이전 이력이 있으면 그 SNO + 1, 없으면 1
                    String prevSnoForPeriod = prevSnoByPeriod.get(key);
                    if (prevSnoForPeriod != null && !prevSnoForPeriod.isBlank()) {
                        int prevInt;
                        try {
                            prevInt = Integer.parseInt(prevSnoForPeriod);
                        } catch (NumberFormatException e) {
                            prevInt = 0;
                        }
                        rowSno = formatSno(prevInt + 1);
                        // N 처리용으로 관리번호별 이전 SNO 저장
                        prevSnoByMngNo.put(mngNo, prevSnoForPeriod);
                    } else {
                        // 완전 신규 기간 → 첫 버전
                        rowSno = formatSno(1);
                    }
                } else {
                    // 신규 모드: 항상 1
                    rowSno = formatSno(1);
                }

                out.add(new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        mngNo,
                        rowSno,
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

            return new BuildResult(out, prevSnoByMngNo);
        }

        /* ===================== 건수 한도 ===================== */

        // 관리번호별 현재 SNO (existing은 USE_YN=Y만 넘어온다고 가정)
        Map<String, String> currentSnoByMngNo = new HashMap<>();
        if (hasExisting && "02".equals(curDvs)) {
            for (SprtLmtRspVO row : existing) {
                if (!dvs.equals(row.getTpwLmtDvsCd())) continue;
                String mngNo = row.getSpfnLmtMngNo();
                if (mngNo == null) continue;
                currentSnoByMngNo.putIfAbsent(mngNo, row.getSpfnLmtSno());
            }
        }

        List<String> generated = Collections.emptyList();

        if (editMode && hasExisting && "02".equals(curDvs)) {
            // 수정 모드: 관리번호가 없는 행(신규)에 대해서만 시퀀스 발급
            int needNew = (int) ncntSrc.stream()
                    .filter(n -> n.getSpfnLmtMngNo() == null || n.getSpfnLmtMngNo().isBlank())
                    .count();

            if (needNew > 0) {
                generated = readNextMngNo(needNew);
            }
        } else {
            // 신규 모드: 전체 행에 대해 관리번호 발급
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

            String rowSno;

            if (editMode) {
                // 수정 모드: 기존 관리번호면 이전 SNO + 1, 신규 관리번호면 1
                String prevSno = currentSnoByMngNo.get(mngNo);
                if (prevSno != null && !prevSno.isBlank()) {
                    int prevInt;
                    try {
                        prevInt = Integer.parseInt(prevSno);
                    } catch (NumberFormatException e) {
                        prevInt = 0;
                    }
                    rowSno = formatSno(prevInt + 1);
                    prevSnoByMngNo.put(mngNo, prevSno);
                } else {
                    rowSno = formatSno(1);
                }
            } else {
                // 신규 모드: 모든 행 1
                rowSno = formatSno(1);
            }

            String sttYm = normalizeYm(n.getLmtSttYm());
            String endYm = normalizeYm(n.getLmtEndYm());
            if (sttYm == null) {
                sttYm = currentYYYYMM();
            }
            if (endYm == null) {
                endYm = sttYm;
            }

            out.add(new SprtLmtReqVO(
                    req.getTpwSvcId(),
                    req.getTpwSvcTypId(),
                    mngNo,
                    rowSno,
                    "02",
                    nextTyp,
                    sttYm,
                    endYm,
                    n.getMinCndtVal(),
                    n.getMaxCndtVal(),
                    n.getTgtAdptVal(),
                    "Y"
            ));
        }

        return new BuildResult(out, prevSnoByMngNo);
    }

    /* ===================== 한도 유형/존재 여부 체크 ===================== */

    /**
     * 서비스 기준 한도 유형(금액/건수) 목록 조회.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SprtLmtKindVO> readSvcLmtKinds(String tpwSvcId) {
        return sprtLmtMapper.readSvcLmtKinds(tpwSvcId);
    }

    /**
     * 특정 서비스/서비스유형에 대해 한도 존재 여부 및 분기 구간,
     * 한도 유형(단일/복수) 정보를 계산하여 반환.
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     */
    @Override
    @Transactional(readOnly = true)
    public SprtLmtExistResVO checkExist(String tpwSvcId, String tpwSvcTypId) {
        SprtLmtExistResVO res = new SprtLmtExistResVO();

        // 분기 범위 정보
        List<QuarterRangeVO> qtRanges = readQuarterRanges(tpwSvcId, tpwSvcTypId);
        res.setQtRanges(qtRanges);

        // 한도 존재 여부
        boolean anyExists = hasExistingLimit(tpwSvcId, tpwSvcTypId);
        res.setExists(anyExists);

        // 한도 유형(금액/건수) 정보
        List<SprtLmtKindVO> kinds = readSvcLmtKinds(tpwSvcId);
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

    /* ===================== 유틸리티 ===================== */

    /**
     * 분기 탭 기본 Row 4개 생성.
     */
    private List<AmtReqVO> initQuarterList() {
        return IntStream.range(0, 4)
                .mapToObj(i -> new AmtReqVO())
                .collect(Collectors.toList());
    }

    /**
     * 월 탭 기본 Row 12개 생성 (현재 연도 기준).
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
     * 건수 탭 기본 Row 4개 생성.
     */
    private List<NcntReqVO> initNcntList() {
        return IntStream.range(0, 4)
                .mapToObj(i -> new NcntReqVO())
                .collect(Collectors.toList());
    }

    /**
     * yyyyMM 또는 yyyy-MM 문자열을 내부 표준 yyyyMM으로 정규화.
     * 형식이 맞지 않으면 null.
     */
    private String normalizeYm(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;

        if (s.matches("^\\d{4}-\\d{2}$")) {
            return s.substring(0, 4) + s.substring(5, 7);
        }
        if (s.matches("^\\d{6}$")) {
            return s;
        }
        return null;
    }

    /**
     * SNO(일련번호)를 10자리 0 패딩 문자열로 포맷.
     * ex) 1 -> 0000000001
     */
    private String formatSno(int sno) {
        if (sno < 0) sno = 0;
        return String.format("%010d", sno);
    }

    /**
     * buildInserts 결과를 담는 내부 DTO.
     */
    private static class BuildResult {
        final List<SprtLmtReqVO> rows;
        /**
         * 관리번호별 이전 SNO (수정 모드일 때만 사용)
         * key: spfnLmtMngNo, value: 이전 spfnLmtSno
         */
        final Map<String, String> prevSnoByMngNo;

        BuildResult(List<SprtLmtReqVO> rows, Map<String, String> prevSnoByMngNo) {
            this.rows = rows;
            this.prevSnoByMngNo = prevSnoByMngNo != null ? prevSnoByMngNo : Collections.emptyMap();
        }
    }
}
