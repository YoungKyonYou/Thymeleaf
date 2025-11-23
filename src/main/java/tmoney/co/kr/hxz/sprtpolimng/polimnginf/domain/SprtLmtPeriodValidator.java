package tmoney.co.kr.hxz.sprtpolimng.polimnginf.domain;


import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Component;
import tmoney.co.kr.hxz.common.error.exception.DomainExceptionCode;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;

/**
 * 한도 기간/중복 검증 전담 클래스.
 * - 금액(분기/월)의 기간 중복/중첩 검증
 * - 건수 한도 최소/최대 기본 검증
 */
@Component
public class SprtLmtPeriodValidator {

    private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 메인 검증 진입점
     */
    public void validate(InstReqVO req) {
        if (req == null) {
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance("요청 데이터가 없습니다.");
        }

        String dvs = Optional.ofNullable(req.getTpwLmtDvsCd()).orElse("").trim(); // 01=금액, 02=건수
        String typ = Optional.ofNullable(req.getTpwLmtTypCd()).orElse("").trim(); // 01=월, 02=분기/건수

        if ("01".equals(dvs)) {
            validateAmount(req.getAmtList(), typ);
        } else if ("02".equals(dvs)) {
            validateCount(req.getNcntList());
        }
    }

    /* ===================== 금액 한도(분기/월) ===================== */

    private void validateAmount(List<AmtReqVO> list, String typ) {
        if (list == null || list.isEmpty()) {
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance("한도 행을 하나 이상 입력하세요.");
        }

        // 화면상 빈 행(연월/금액 모두 빈 값)은 허용하지 않고 에러로 처리
        boolean hasEmptyRow = list.stream().anyMatch(a ->
                isBlank(a.getLmtSttYm()) && isBlank(a.getLmtEndYm()) && a.getTgtAdptVal() == 0
        );
        if (hasEmptyRow) {
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance("빈 행이 있습니다. 사용하지 않는 행은 삭제해주세요.");
        }

        if ("01".equals(typ)) {
            // 월 한도
            validateMonthly(list);
        } else {
            // 기본: 분기 한도(typ=02)
            validateQuarter(list);
        }
    }

    /**
     * 분기 한도: 기간이 서로 겹치면 안 됨
     * 예) 2025-01 ~ 2025-03, 2025-04 ~ 2025-06 → OK
     *     2025-01 ~ 2025-03, 2025-02 ~ 2025-04 → 겹침(에러)
     */
    private void validateQuarter(List<AmtReqVO> list) {
        List<Range> ranges = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            AmtReqVO row = list.get(i);
            String stt = row.getLmtSttYm();
            String end = row.getLmtEndYm();

            YearMonth from = parseYm(stt, "분기 한도 시작월");
            YearMonth to = parseYm(end, "분기 한도 종료월");

            if (to.isBefore(from)) {
                throw DomainExceptionCode.VALIDATION_ERROR.newInstance(
                        String.format("분기 한도의 종료월이 시작월보다 앞설 수 없습니다. (행 %d)", i + 1)
                );
            }

            ranges.add(new Range(from, to, i + 1));
        }

        // 시작월 기준 정렬 후 인접 구간끼리 겹침 확인
        ranges.sort(Comparator.comparing(r -> r.start));

        Range prev = null;
        for (Range curr : ranges) {
            if (prev != null) {
                // prev.end >= curr.start 이면 기간이 겹친 것(연속 X, 겹침)
                if (!curr.start.isAfter(prev.end)) {
                    String msg = String.format(
                            "분기 한도의 기간이 서로 겹치는 행이 있습니다. (행 %d, 행 %d)",
                            prev.rowIndex,
                            curr.rowIndex
                    );
                    throw DomainExceptionCode.VALIDATION_ERROR.newInstance(msg);
                }
            }
            prev = curr;
        }
    }

    /**
     * 월 한도: 같은 연월이 두 번 이상 나오면 안 됨
     * 예) 2025-01, 2025-02, 2025-03 → OK
     *     2025-01, 2025-01 → 중복(에러)
     */
    private void validateMonthly(List<AmtReqVO> list) {
        Map<YearMonth, Integer> seen = new HashMap<>();

        for (int i = 0; i < list.size(); i++) {
            AmtReqVO row = list.get(i);
            YearMonth ym = parseYm(row.getLmtSttYm(), "월 한도 적용월");
            Integer dupRow = seen.putIfAbsent(ym, i + 1);
            if (dupRow != null) {
                String msg = String.format(
                        "월 한도의 적용 월이 중복되었습니다. (행 %d, 행 %d: %s)",
                        dupRow,
                        i + 1,
                        ym
                );
                throw DomainExceptionCode.VALIDATION_ERROR.newInstance(msg);
            }
        }
    }

    /* ===================== 건수 한도 ===================== */

    private void validateCount(List<NcntReqVO> list) {
        if (list == null || list.isEmpty()) {
            // 건수도 아무 행도 없으면 굳이 저장할 필요 없음 → 여기서는 그냥 에러로 처리
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance("건수 한도 행을 하나 이상 입력하세요.");
        }

        for (int i = 0; i < list.size(); i++) {
            NcntReqVO row = list.get(i);
            // 적용 연월 필수
            if (isBlank(row.getLmtSttYm())) {
                throw DomainExceptionCode.VALIDATION_ERROR.newInstance(
                        String.format("건수 한도: 적용 연월은 필수입니다. (행 %d)", i + 1)
                );
            }
            // 최소/최대 건수 기본 관계 검증
            if (row.getMinCndtVal() > 0 && row.getMaxCndtVal() > 0
                    && row.getMaxCndtVal() < row.getMinCndtVal()) {
                throw DomainExceptionCode.VALIDATION_ERROR.newInstance(
                        String.format("건수 한도: 최대 건수는 최소 건수보다 크거나 같아야 합니다. (행 %d)", i + 1)
                );
            }
        }
    }

    /* ===================== 공통 유틸 ===================== */

    private YearMonth parseYm(String ym, String label) {
        if (isBlank(ym)) {
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance(label + "은(는) 필수입니다.");
        }
        String v = ym.replace("-", "").trim();
        if (!v.matches("\\d{6}")) {
            throw DomainExceptionCode.VALIDATION_ERROR.newInstance(
                    label + " 형식이 올바르지 않습니다. (yyyyMM 또는 yyyy-MM)"
            );
        }
        return YearMonth.parse(v, YM_FMT);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static class Range {
        final YearMonth start;
        final YearMonth end;
        final int rowIndex;

        Range(YearMonth start, YearMonth end, int rowIndex) {
            this.start = start;
            this.end = end;
            this.rowIndex = rowIndex;
        }
    }
}
