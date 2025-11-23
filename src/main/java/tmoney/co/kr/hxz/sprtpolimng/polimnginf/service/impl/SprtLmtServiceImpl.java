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

        // 4) insert 빌드
        BuildResult build = buildInserts(req, existing, hasExisting);
        List<SprtLmtReqVO> toInsert = build.rows;
        if (toInsert.isEmpty()) return;

        // 5) 이전 버전 N 처리
        if (build.prevSno != null && !build.touchedMngNos.isEmpty()) {
            updateSprtLmtUseYnByMngNos(
                    req.getTpwSvcId(),
                    req.getTpwSvcTypId(),
                    req.getTpwLmtDvsCd(),
                    build.prevSno,
                    new ArrayList<>(build.touchedMngNos)
            );
        }

        // 6) 새 버전 insert
        insertSprtLmt(toInsert);
    }

    private String currentYYYYMM() {
        LocalDate now = LocalDate.now();
        return String.format("%04d%02d", now.getYear(), now.getMonthValue());
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
     *      · 동일 서비스/유형 + 동일 기간(시작/종료년월, 유형) 이면 → 기존 관리번호 재사용
     *      · 완전 신규 기간이면 → 새 관리번호 채번
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
            // 건수는 요청 행 수만큼 관리번호를 미리 뽑는다
            Deque<String> mngNoPool = new ArrayDeque<>(readNextMngNo(needCount));

            for (NcntReqVO n : ncntSrc) {
                String mngNo = mngNoPool.removeFirst();

                // 연월 정규화(YYYYMM / YYYY-MM → YYYYMM)
                String sttYm = normalizeYm(
                        Optional.ofNullable(n.getLmtSttYm()).orElse(sttYmForCount)
                );
                if (sttYm == null) {
                    throw DomainExceptionCode.VALIDATION_ERROR.newInstance("건수 한도: 적용 연월은 필수입니다.");
                }

                // 지금은 종료 연월 = 시작 연월로 고정
                String endYm = sttYm;
                // 만약 나중에 화면에서 종료 연월도 받게 되면:
                // String endYm = normalizeYm(
                //        Optional.ofNullable(n.getLmtEndYm()).orElse(n.getLmtSttYm())
                // );

                out.add(new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        mngNo,
                        nextSno,                // 이번 저장의 공통 일련번호
                        "02",                   // 건수
                        nextTyp,                // 02
                        sttYm,
                        endYm,
                        n.getMinCndtVal(),
                        n.getMaxCndtVal(),
                        n.getTgtAdptVal(),
                        "Y"
                ));
            }
        }

        return new BuildResult(out, prevSno, touchedMngNos);
    }

    @Override
    @Transactional(readOnly = true)
    public SprtLmtExistResVO checkExist(String tpwSvcId, String tpwSvcTypId) {
        SprtLmtExistResVO res = new SprtLmtExistResVO();

        // 1) 해당 서비스+서비스유형의 기존 분기 목록
        List<QuarterRangeVO> qtRanges = sprtLmtMapper.readQuarterRanges(tpwSvcId, tpwSvcTypId);
        res.setQtRanges(qtRanges);
        res.setExists(qtRanges != null && !qtRanges.isEmpty());

        // 2) 서비스 단위 한도유형 목록
        List<SprtLmtKindVO> kinds = sprtLmtMapper.readSvcLmtKinds(tpwSvcId);
        if (kinds != null && !kinds.isEmpty()) {
            if (kinds.size() == 1) {
                // 서비스 안의 모든 서비스유형이 동일한 유형으로 설정된 상태
                res.setSvcLmtDvsCd(kinds.get(0).getDvsCd());
                res.setSvcLmtTypCd(kinds.get(0).getLmtTypCd());
                res.setMultiKinds(false);
            } else {
                // 이미 서비스 안에 서로 다른 한도유형이 섞여 있는 상태
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