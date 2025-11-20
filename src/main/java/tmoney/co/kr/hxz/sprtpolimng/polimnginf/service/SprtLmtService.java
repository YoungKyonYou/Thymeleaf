package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.mapper.SprtLmtMapper;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.*;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class SprtLmtService  {

    private final SprtLmtMapper sprtLmtMapper;

    public SprtLmtModalVO initModal() {
        return new SprtLmtModalVO(
                initQuarterList(),
                initMonList(),
                initNcntList()
        );
    }

    @Transactional
    public void updateTrdNcntLtnAdptYn(String tpwSvcTypId, String adptYn) {
        sprtLmtMapper.updateTrdNcntLtnAdptYn(tpwSvcTypId, adptYn);
    }

    @Transactional(readOnly = true)
    public SprtLmtModalDtlVO readSprtLmtByTpwSvcTypId(String tpwSvcId, String tpwSvcTypId) {
        List<SprtLmtRspVO> rows = readSprtLmtDtlByTpwSvc(tpwSvcId, tpwSvcTypId, "Y");

        if (rows == null || rows.isEmpty()) {
            SprtLmtModalVO m = initModal();
            return new SprtLmtModalDtlVO(m.getQt(), m.getMon(), m.getArr(), "01", "01");
        }

        String dvs = rows.get(0).getTpwLmtDvsCd(); // 01=금액, 02=건수
        String typ = rows.get(0).getTpwLmtTypCd(); // 01=월,   02=분기

        if ("02".equals(dvs)) return buildCount(rows, dvs, typ);
        if ("01".equals(typ))  return buildAmountMonthly(rows, dvs, typ);
        return buildAmountQuarterly(rows, dvs, typ);
    }

    private SprtLmtModalDtlVO buildAmountMonthly(List<SprtLmtRspVO> rows, String dvs, String typ) {
        List<AmtReqVO> mon = rows.stream()
                .map(a -> new AmtReqVO(a.getSpfnLmtMngNo(),a.getSpfnLmtSno(), a.getLmtSttYm(), a.getLmtEndYm(), a.getTgtAdptVal()))
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
                .map(a -> new AmtReqVO(a.getSpfnLmtMngNo(),a.getSpfnLmtSno(), a.getLmtSttYm(), a.getLmtEndYm(), a.getTgtAdptVal()))
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
                .map(a -> new NcntReqVO(a.getSpfnLmtMngNo(),a.getSpfnLmtSno(), a.getMinCndtVal(), a.getMaxCndtVal(), a.getTgtAdptVal()))
                .collect(Collectors.toList());
        return new SprtLmtModalDtlVO(
                initQuarterList(),
                initMonList(),
                ncnt,
                dvs, typ
        );
    }

    private List<AmtReqVO> initQuarterList() {
        return IntStream.range(0, 4).mapToObj(i -> new AmtReqVO()).collect(Collectors.toList());
    }

    private List<AmtReqVO> initMonList() {
        int year = LocalDate.now().getYear();
        return IntStream.rangeClosed(1, 12)
                .mapToObj(i -> {
                    String yyyymm = String.format("%d%02d", year, i);
                    return new AmtReqVO("","", yyyymm, yyyymm, 0);
                })
                .collect(Collectors.toList());
    }

    private List<NcntReqVO> initNcntList() {
        return IntStream.range(0, 4).mapToObj(i -> new NcntReqVO()).collect(Collectors.toList());
    }

    /** 페이징 조회 */
    @Transactional(readOnly = true)
    public PageDataVO<SprtLmtRspVO> readSprtLmtPtPaging(SprtLmtSrchReqVO req) {
        final int offset = req.getPage() * req.getSize();
        long total = readSprtLmtPtListCnt(req);

        SprtLmtSrchReqVO reqVO = new SprtLmtSrchReqVO(
                req.getTpwSvcId(), req.getTpwSvcNm(), req.getTpwSvcTypId(), req.getTpwSvcTypNm(), req.getSpfnLmtMngNo(),
                req.getSpfnLmtSno(),
                req.getUseYn(), req.getTpwLmtDvsCd(),
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
    public boolean hasExistingLimit(String tpwSvcId, String tpwSvcTypId) {
        Integer cnt = sprtLmtMapper.readSprtLmtCntBySvcTyp(tpwSvcId, tpwSvcTypId);
        return cnt != null && cnt > 0;
    }

    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(String tpwSvcId, String tpwSvcTypId, String useYn) {
        return sprtLmtMapper.readSprtLmtDtlByTpwSvc(tpwSvcId, tpwSvcTypId, useYn);
    }

    @Transactional
    public void updateSprtLmtUseYn(String tpwSvcTypId) {
        sprtLmtMapper.updateSprtLmtUseYn(tpwSvcTypId);
    }

    /** insert 메인(유형 분기) */
    @Transactional
    public void insertSprtLmtAmt(InstReqVO req) {
        if (req == null) return;

        List<SprtLmtRspVO> existing = readSprtLmtDtlByTpwSvc(req.getTpwSvcId(), req.getTpwSvcTypId(), "Y");
        boolean hasExisting = !existing.isEmpty();

        // 월(01)은 종료월=시작월
        if ("01".equals(req.getTpwLmtTypCd()) && req.getAmtList() != null) {
            req.getAmtList().forEach(a -> a.setLmtEndYm(a.getLmtSttYm()));
        }

        List<SprtLmtReqVO> toInsert = buildInserts(req, existing, hasExisting);
        if (toInsert.isEmpty()) return;

        if (hasExisting) updateSprtLmtUseYn(req.getTpwSvcTypId());
        insertSprtLmt(toInsert);
    }

    private String currentYYYYMM() {
        LocalDate now = LocalDate.now();
        return String.format("%04d%02d", now.getYear(), now.getMonthValue());
    }

    @Transactional(readOnly = true)
    public List<String> readNextMngNo(int count) {
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

    /** 금액/건수 공통 insert 빌더 (단일 함수) */
    private List<SprtLmtReqVO> buildInserts(InstReqVO req, List<SprtLmtRspVO> existing, boolean hasExisting) {
        final String dvs = req.getTpwLmtDvsCd(); // 01=금액, 02=건수
        final boolean isAmount = "01".equals(dvs);

        final List<AmtReqVO> amtSrc = Optional.ofNullable(req.getAmtList()).orElse(Collections.emptyList());
        final List<NcntReqVO> ncntSrc = Optional.ofNullable(req.getNcntList()).orElse(Collections.emptyList());
        final int needCount = isAmount ? amtSrc.size() : ncntSrc.size();
        if (needCount == 0) return Collections.emptyList();

        final String curDvs = hasExisting ? existing.get(0).getTpwLmtDvsCd() : null;
        final String curTyp = hasExisting ? existing.get(0).getTpwLmtTypCd() : null;
        final String nextTyp = isAmount
                ? req.getTpwLmtTypCd()
                : Optional.ofNullable(req.getTpwLmtTypCd()).orElse(curTyp != null ? curTyp : "02");

        final boolean reuseMngNo = hasExisting && Objects.equals(curDvs, dvs) && Objects.equals(curTyp, nextTyp);

        // 관리번호 풀
        final Deque<String> mngNoPool;
        if (isAmount) {
            mngNoPool = reuseMngNo
                    ? existing.stream().map(SprtLmtRspVO::getSpfnLmtMngNo)
                    .limit(needCount).collect(Collectors.toCollection(ArrayDeque::new))
                    : new ArrayDeque<>(readNextMngNo(needCount));
        } else {
            // 건수는 행 변동 고려: 전량 새 채번(유형 같아도)
            mngNoPool = new ArrayDeque<>(readNextMngNo(needCount));
        }

        // SNO: 기존 있으면 다음 값부터 시작, 없으면 '0000000001'
        String nextSno = hasExisting
                ? readSpfnLmtSnoNextVal(existing.get(existing.size() - 1).getSpfnLmtSno())
                : "0000000001";

        final String nowYm = currentYYYYMM();
        final String sttYmForCount = hasExisting ? existing.get(0).getLmtSttYm() : nowYm;
        final String endYmForCount = hasExisting ? existing.get(0).getLmtEndYm() : nowYm;

        List<SprtLmtReqVO> out = new ArrayList<>(needCount);

        if (isAmount) {
            for (AmtReqVO a : amtSrc) {
                String mngNo = mngNoPool.removeFirst();
                String sno = nextSno;
                out.add(new SprtLmtReqVO(
                        req.getTpwSvcId(), req.getTpwSvcTypId(),
                        mngNo, sno,
                        "01", nextTyp,
                        a.getLmtSttYm(), a.getLmtEndYm(),
                        0, 0, a.getTgtAdptVal(),
                        "Y"
                ));
            }
        } else {
            for (NcntReqVO n : ncntSrc) {
                String mngNo = mngNoPool.removeFirst();
                String sno = nextSno;
                out.add(new SprtLmtReqVO(
                        req.getTpwSvcId(), req.getTpwSvcTypId(),
                        mngNo, sno,
                        "02", nextTyp,
                        sttYmForCount, endYmForCount,
                        n.getMinCndtVal(), n.getMaxCndtVal(), n.getTgtAdptVal(),
                        "Y"
                ));
            }
        }
        return out;
    }
}