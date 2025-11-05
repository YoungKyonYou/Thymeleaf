package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service;

import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.mapper.SprtLmtMapper;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtLmtModReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntInstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SprtLmtService {

    private final SprtLmtMapper sprtLmtMapper;

    public SprtLmtModalVO initModal(){
        return new SprtLmtModalVO(
                initQuarterList(),
                initMonList(),
                initNcntList()
        );
    }

    @Transactional(readOnly = true)
    public SprtLmtModalDtlVO readSprtLmtByTpwSvcTypId(String tpwSvcTypId) {
        List<SprtLmtRspVO> rows = readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, "Y");

        if (rows == null || rows.isEmpty()) {
            SprtLmtModalVO m = initModal();
            return new SprtLmtModalDtlVO(m.getQt(), m.getMon(), m.getArr(), "01", "01");
        }

        String dvs = rows.get(0).getTpwLmtDvsCd(); // 01=금액, 02=건수
        String typ = rows.get(0).getTpwLmtTypCd(); // 01=월,   02=분기

        if ("02".equals(dvs))
            return buildCount(rows, dvs, typ);
        if ("01".equals(typ))
            return buildAmountMonthly(rows, dvs, typ);
        return buildAmountQuarterly(rows, dvs, typ);
    }

    private SprtLmtModalDtlVO buildAmountMonthly(List<SprtLmtRspVO> rows, String dvs, String typ) {
        List<AmtReqVO> mon = rows.stream()
                .map(a -> new AmtReqVO(a.getSpfnLmtMngNo(), a.getLmtSttYm(), a.getLmtEndYm(), a.getTgtAdptVal()))
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
                .map(a -> new AmtReqVO(a.getSpfnLmtMngNo(), a.getLmtSttYm(), a.getLmtEndYm(), a.getTgtAdptVal()))
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
                .map(a -> new NcntReqVO(a.getSpfnLmtMngNo(), a.getMinCndtVal(), a.getMaxCndtVal(), a.getTgtAdptVal()))
                .collect(Collectors.toList());
        return new SprtLmtModalDtlVO(
                initQuarterList(),
                initMonList(),
                ncnt,
                dvs, typ
        );
    }

    private List<AmtReqVO> initQuarterList(){
        return IntStream.range(0, 4)
                .mapToObj(i -> new AmtReqVO())
                .collect(Collectors.toList());
    }

    private List<AmtReqVO> initMonList(){
        int year = LocalDate.now().getYear();
        return IntStream.rangeClosed(1, 12)
                .mapToObj(i -> {
                    String yyyymm = String.format("%d%02d", year, i);
                    return new AmtReqVO("", yyyymm, yyyymm, 0);
                })
                .collect(Collectors.toList());
    }

    private List<NcntReqVO> initNcntList() {
        return IntStream.range(0, 4)
                .mapToObj(i -> new NcntReqVO())
                .collect(Collectors.toList());
    }

    /** 페이징 조회 */
    @Transactional(readOnly = true)
    public PageDataVO<SprtLmtRspVO> readSprtLmtPtPaging(SprtLmtSrchReqVO req) {
        final int offset = req.getPage() * req.getSize();
        long total = readSprtLmtPtListCnt(req);

        SprtLmtSrchReqVO reqVO = new SprtLmtSrchReqVO(
                req.getTpwSvcNm(), req.getUseYn(), req.getTpwLmtDvsCd(),
                offset, req.getSize(), req.getSort(), req.getDir()
        );

        List<SprtLmtRspVO> content = readSprtLmtPtList(reqVO);
        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }

    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtPtList(SprtLmtSrchReqVO req) {
        return sprtLmtMapper.readSprtLmtPtList(req);
    }

    @Transactional(readOnly = true)
    public long readSprtLmtPtListCnt(SprtLmtSrchReqVO req) {
        return sprtLmtMapper.readSprtLmtPtListCnt(req);
    }

    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtDtlByTpwSvcTypId(String tpwSvcTypId, String useYn) {
        return sprtLmtMapper.readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, useYn);
    }

    @Transactional
    public void updateSprtLmtUseYn(String tpwSvcTypId) {
        sprtLmtMapper.updateSprtLmtUseYn(tpwSvcTypId);
    }

    /** insert 메인 (유형 분기) */
    @Transactional
    public void insertSprtLmtAmt(InstReqVO req) {
        if (req == null) return;

        List<SprtLmtRspVO> existing = readSprtLmtDtlByTpwSvcTypId(req.getTpwSvcTypId(), "Y");
        boolean hasExisting = !existing.isEmpty();

        // 월(01) → 종료월 = 시작월
        if ("01".equals(req.getTpwLmtTypCd()) && req.getAmtList() != null) {
            req.getAmtList().forEach(a -> a.setLmtEndYm(a.getLmtSttYm()));
        }

        // 통합 빌더 호출
        List<SprtLmtReqVO> toInsert = buildInserts(req, existing, hasExisting);
        if (toInsert.isEmpty())
            return;

        if (hasExisting)
            updateSprtLmtUseYn(req.getTpwSvcTypId());
        insertSprtLmt(toInsert);
    }

    private String currentYYYYMM() {
        LocalDate now = LocalDate.now();
        return String.format("%04d%02d", now.getYear(), now.getMonthValue());
    }

    @Transactional(readOnly = true)
    public List<String> readNextMngNo(int count){
        return sprtLmtMapper.readNextMngNo(count);
    }

    @Transactional
    public void insertSprtLmt(List<SprtLmtReqVO> req) {
        sprtLmtMapper.insertSprtLmt(req);
    }

    @Transactional(readOnly = true)
    public String readSpfnLmtSnoNextVal(String spfnLmtSno) {
        return sprtLmtMapper.readSpfnLmtSnoNextVal(spfnLmtSno);
    }

    private void ensureEndYmForMonthly(InstReqVO req) {
        if ("01".equals(req.getTpwLmtTypCd()) && req.getAmtList() != null) {
            req.getAmtList().forEach(a -> a.setLmtEndYm(a.getLmtSttYm()));
        }
    }

    private Deque<String> prepareMngNoPool(boolean reuse, List<SprtLmtRspVO> existing, int needCount) {
        if (reuse) {
            return existing.stream()
                    .map(SprtLmtRspVO::getSpfnLmtMngNo)
                    .collect(Collectors.toCollection(ArrayDeque::new));
        }
        return new ArrayDeque<>(readNextMngNo(needCount));
    }

    /** 금액/건수 공통 insert 빌더 (단일 함수) */
    private List<SprtLmtReqVO> buildInserts(InstReqVO req, List<SprtLmtRspVO> existing, boolean hasExisting) {
        final String dvs = req.getTpwLmtDvsCd();           // 01=금액, 02=건수
        final boolean isAmount = "01".equals(dvs);

        // 소스 리스트 선택
        final List<AmtReqVO> amtSrc  = Optional.ofNullable(req.getAmtList()).orElse(Collections.emptyList());
        final List<NcntReqVO> ncntSrc = Optional.ofNullable(req.getNcntList()).orElse(Collections.emptyList());
        final int needCount = isAmount ? amtSrc.size() : ncntSrc.size();
        if (needCount == 0) return Collections.emptyList();

        // 현재/다음 타입
        final String curDvs = hasExisting ? existing.get(0).getTpwLmtDvsCd() : null;
        final String curTyp = hasExisting ? existing.get(0).getTpwLmtTypCd() : null;
        final String nextTyp = isAmount
                ? req.getTpwLmtTypCd()                                       // 금액: 요청값 그대로 (01=월, 02=분기)
                : Optional.ofNullable(req.getTpwLmtTypCd()).orElse(curTyp != null ? curTyp : "02"); // 건수: 기존/기본 유지

        // 관리번호 재사용 여부
        final boolean reuseMngNo = hasExisting && Objects.equals(curDvs, dvs) && Objects.equals(curTyp, nextTyp);

        // 관리번호 풀, SNO 기준값
        final Deque<String> mngNoPool = prepareMngNoPool(reuseMngNo, existing, needCount);
        final String baseSno = hasExisting ? existing.get(existing.size() - 1).getSpfnLmtSno() : "0000000000";

        // 건수 한도의 기간(YYYYMM) 결정: 신규는 당월, 기존 있으면 유지
        final String nowYm = currentYYYYMM();
        final String sttYmForCount = hasExisting ? existing.get(0).getLmtSttYm() : nowYm;
        final String endYmForCount = hasExisting ? existing.get(0).getLmtEndYm() : nowYm;

        final List<SprtLmtReqVO> out = new ArrayList<>(needCount);

        if (isAmount) {
            // 금액: 행별 시작/종료월 사용, min/max=0, tgt=금액
            for (AmtReqVO a : amtSrc) {
                final String mngNo = mngNoPool.removeFirst();
                final String sno   = hasExisting ? readSpfnLmtSnoNextVal(baseSno) : "0000000001";

                out.add(new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        mngNo,
                        sno,
                        "01",                   // 금액
                        nextTyp,                // 01=월, 02=분기
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        0,
                        0,
                        a.getTgtAdptVal(),
                        "Y"
                ));
            }
        } else {
            // 건수: 공통 기간 사용, min/max/tgt=행별 값
            for (NcntReqVO n : ncntSrc) {
                final String mngNo = mngNoPool.removeFirst();
                final String sno   = hasExisting ? readSpfnLmtSnoNextVal(baseSno) : "0000000001";

                out.add(new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        mngNo,
                        sno,
                        "02",                   // 건수
                        nextTyp,                // 규약 유지
                        sttYmForCount,
                        endYmForCount,
                        n.getMinCndtVal(),
                        n.getMaxCndtVal(),
                        n.getTgtAdptVal(),
                        "Y"
                ));
            }
        }
        return out;
    }
}
