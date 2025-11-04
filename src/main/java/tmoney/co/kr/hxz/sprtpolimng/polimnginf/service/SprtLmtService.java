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
        //기존 값 조회
        List<SprtLmtRspVO> rspVO = readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, "Y");

        //기존 값이 없는 경우
        if (rspVO.isEmpty()) {
            SprtLmtModalVO sprtLmtModalVO = initModal();
            return new SprtLmtModalDtlVO(
                    sprtLmtModalVO.getQt(),
                    sprtLmtModalVO.getMon(),
                    sprtLmtModalVO.getArr(),
                    "01",
                    "01"
            );
        }

        String dvsCd = rspVO.get(0).getTpwLmtDvsCd();
        String typCd = rspVO.get(0).getTpwLmtTypCd();



        if (dvsCd.equals("01")) {
            //월 한도인 경우
            if(typCd.equals("01")){
                List<AmtReqVO> monList = rspVO.stream()
                        .map(a -> new AmtReqVO(
                                a.getSpfnLmtMngNo(),
                                a.getLmtSttYm(),
                                a.getLmtEndYm(),
                                a.getTgtAdptVal()
                        )).collect(Collectors.toList());

                return new SprtLmtModalDtlVO(
                        initQuarterList(),
                        monList,
                        initNcntList(),
                        dvsCd,
                        typCd
                );
            }
            //분기 한도인 경우
            List<AmtReqVO> qtList = rspVO.stream()
                    .map(a -> new AmtReqVO(
                            a.getSpfnLmtMngNo(),
                            a.getLmtSttYm(),
                            a.getLmtEndYm(),
                            a.getTgtAdptVal()
                    )).collect(Collectors.toList());

            return new SprtLmtModalDtlVO(
                    qtList,
                    initMonList(),
                    initNcntList(),
                    dvsCd,
                    typCd
            );
        }
        //건수 한도인 경우
        List<NcntReqVO> ncntList = rspVO.stream()
                .map(a -> new NcntReqVO(
                        a.getSpfnLmtMngNo(),
                        a.getMinCndtVal(),
                        a.getMaxCndtVal(),
                        a.getTgtAdptVal()
                )).collect(Collectors.toList());

        return new SprtLmtModalDtlVO(
                initQuarterList(),
                initMonList(),
                ncntList,
                dvsCd,
                typCd
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



    /**
     * 지원 한도 내역 조회
     *
     * @return SprtLmtRspVO 지원 한도 내역
     */

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
    public SprtLmtDtlRspVO readSprtLmtDtl(String tpwSvcTypId, String useYn) {
        if (tpwSvcTypId == null) {
            List<SprtLmtRspVO> list = new ArrayList<>();
            List<SprtLmtReqVO> reqList = new ArrayList<>();
            return new SprtLmtDtlRspVO(list, reqList, "01", "01", "Y");
        }

        List<SprtLmtRspVO> sprtLmtDtlList = readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, useYn);

        List<SprtLmtReqVO> sprtLmtReqList = sprtLmtDtlList.stream()
                .map(a -> new SprtLmtReqVO(
                        a.getTpwSvcId(),
                        a.getTpwSvcTypId(),
                        a.getSpfnLmtMngNo(),
                        a.getSpfnLmtSno(),
                        a.getTpwLmtDvsCd(),
                        a.getTpwLmtTypCd(),
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        a.getMinCndtVal(),
                        a.getMaxCndtVal(),
                        a.getTgtAdptVal(),
                        a.getUseYn()
                )).collect(Collectors.toList());


        return null;
    }


    @Transactional(readOnly = true)
    public List<SprtLmtRspVO> readSprtLmtDtlByTpwSvcTypId(String tpwSvcTypId, String useYn) {
        return sprtLmtMapper.readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, useYn);
    }

    @Transactional
    public void updateSprtLmtUseYn(String tpwSvcTypId) {
        sprtLmtMapper.updateSprtLmtUseYn(tpwSvcTypId);
    }


    @Transactional
    public void insertSprtLmtAmt(InstReqVO req) {
        if (req == null) return;

        // 1) 기존 사용중 데이터
        final List<SprtLmtRspVO> resList = readSprtLmtDtlByTpwSvcTypId(req.getTpwSvcTypId(), "Y");
        final boolean hasExisting = !resList.isEmpty();

        // 2) 금액-월(01)인 경우 종료월 = 시작월
        if ("01".equals(req.getTpwLmtTypCd()) && req.getAmtList() != null) {
            req.getAmtList().forEach(a -> a.setLmtEndYm(a.getLmtSttYm()));
        }

        final List<SprtLmtReqVO> toInsert = new ArrayList<>();
        final String dvs = req.getTpwLmtDvsCd(); // 01=금액, 02=건수

        if ("01".equals(dvs)) {
            // =========================
            // 금액(월/분기) 한도 처리
            // =========================
            final List<AmtReqVO> src = Optional.ofNullable(req.getAmtList()).orElseGet(Collections::emptyList);
            if (src.isEmpty()) return;

            final int k = src.size();
            // 기존과 동일 유형 여부(금액/건수 + 월/분기)
            final String curDvs  = hasExisting ? resList.get(0).getTpwLmtDvsCd()  : null;
            final String curTyp  = hasExisting ? resList.get(0).getTpwLmtTypCd()  : null;
            final String nextTyp = req.getTpwLmtTypCd();
            final boolean isSame = hasExisting && "01".equals(curDvs) && Objects.equals(curTyp, nextTyp);

            // 관리번호 풀: 동일 유형이면 기존값 재사용, 아니면 새 채번
            final Deque<String> mngNoPool = isSame
                    ? resList.stream()
                    .map(SprtLmtRspVO::getSpfnLmtMngNo)
                    .collect(Collectors.toCollection(ArrayDeque::new))
                    : new ArrayDeque<>(readNextMngNo(k));

            // SNO 기준: 최신 SNO를 넘겨 다음값 채번(※ 기존 코드의 mngNo 전달 버그를 보완)
            final String baseSno = hasExisting ? resList.get(resList.size() - 1).getSpfnLmtSno() : "0000000000";

            for (AmtReqVO a : src) {
                final String mngNo = mngNoPool.removeFirst();
                final String sno   = hasExisting ? readSpfnLmtSnoNextVal(baseSno) : "0000000001";

                toInsert.add(new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        mngNo,
                        sno,
                        "01",                     // 금액
                        nextTyp,                  // 01=월, 02=분기
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        0,
                        0,
                        a.getTgtAdptVal(),
                        "Y"
                ));
            }

        } else if ("02".equals(dvs)) {
            // =========================
            // 건수 한도 처리
            // =========================
            final List<NcntReqVO> src = Optional.ofNullable(req.getNcntList()).orElseGet(Collections::emptyList);
            if (src.isEmpty()) return;

            final int k = src.size();
            // 기존과 동일 유형인지 판단(건수 + typ)
            final String curDvs  = hasExisting ? resList.get(0).getTpwLmtDvsCd() : null; // 보통 02
            final String curTyp  = hasExisting ? resList.get(0).getTpwLmtTypCd() : null; // 프로젝트 규약에 맞게 유지
            final String nextTyp = Optional.ofNullable(req.getTpwLmtTypCd()).orElse(curTyp != null ? curTyp : "02");
            final boolean isSame = hasExisting && "02".equals(curDvs) && Objects.equals(curTyp, nextTyp);

            // 관리번호 풀
            final Deque<String> mngNoPool = isSame
                    ? resList.stream()
                    .map(SprtLmtRspVO::getSpfnLmtMngNo)
                    .collect(Collectors.toCollection(ArrayDeque::new))
                    : new ArrayDeque<>(readNextMngNo(k));

            // 기간(YYYYMM): 신규는 당월, 기존 있으면 기존 기간 유지
            final String nowYm  = currentYYYYMM();
            final String sttYm  = hasExisting ? resList.get(0).getLmtSttYm() : nowYm;
            final String endYm  = hasExisting ? resList.get(0).getLmtEndYm() : nowYm;

            // SNO 기준
            final String baseSno = hasExisting ? resList.get(resList.size() - 1).getSpfnLmtSno() : "0000000000";

            for (NcntReqVO n : src) {
                final String mngNo = mngNoPool.removeFirst();
                final String sno   = hasExisting ? readSpfnLmtSnoNextVal(baseSno) : "0000000001";

                toInsert.add(new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        mngNo,
                        sno,
                        "02",                   // 건수
                        nextTyp,                // 규약 유지(기존 코드와 일관)
                        sttYm,
                        endYm,
                        n.getMinCndtVal(),
                        n.getMaxCndtVal(),
                        n.getTgtAdptVal(),
                        "Y"
                ));
            }
        } else {
            // 알 수 없는 구분코드면 무시
            return;
        }

        // 3) 기존 사용중(useYn='Y') 데이터는 일괄 'N' 처리
        updateSprtLmtUseYn(req.getTpwSvcTypId());

        // 4) 신규 버전 데이터 일괄 insert
        insertSprtLmt(toInsert);
    }

    /** 당월 YYYYMM */
    private String currentYYYYMM() {
        LocalDate now = LocalDate.now();
        return String.format("%04d%02d", now.getYear(), now.getMonthValue());
    }


    @Transactional(readOnly = true)
    public List<String> readNextMngNo(int count){
        return sprtLmtMapper.readNextMngNo(count);
    }


    @Transactional
    public void updateSprtLmtAmt(AmtLmtModReqVO req, String tpwSvcTypId) {
        List<String> updList = new ArrayList<>();
        List<SprtLmtReqVO> list = new ArrayList<>();
        List<SprtLmtRspVO> resList = readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, "Y");
        SprtLmtRspVO res = resList.get(resList.size() - 1);

        for (AmtReqVO a : req.getList()) {
            SprtLmtReqVO reqVO = new SprtLmtReqVO(
                    res.getTpwSvcId(),
                    tpwSvcTypId,
                    a.getSpfnLmtMngNo(),
                    readSpfnLmtSnoNextVal(res.getSpfnLmtSno()),
                    res.getTpwLmtDvsCd(),
                    req.getTpwLmtTypCd(),
                    a.getLmtSttYm(),
                    a.getLmtEndYm(),
                    0,
                    0,
                    a.getTgtAdptVal(),
                    "Y"
            );
            updList.add(a.getSpfnLmtMngNo());
            list.add(reqVO);
        }

        updatePrevSprtLmt(updList, tpwSvcTypId);
        insertNextSprtLmt(list, tpwSvcTypId);
    }


    @Transactional
    public void insertSprtLmtNcnt(NcntInstReqVO req) {
        List<SprtLmtReqVO> list = new ArrayList<>();
        List<SprtLmtRspVO> resList = readSprtLmtDtlByTpwSvcTypId(req.getTpwSvcTypId(), "Y");
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        String yyyymm = String.format("%d%02d", year, month);

        if (resList.isEmpty()) {
            long num = Long.parseLong(readSpfnLmtMngNoNextVal());
            for (NcntReqVO a : req.getList()) {
                num += 1;
                SprtLmtReqVO reqVO = new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        String.format("%010d", num),
                        "0000000001",
                        "02",
                        "01",
                        yyyymm,
                        yyyymm,
                        a.getMinCndtVal(),
                        a.getMaxCndtVal(),
                        a.getTgtAdptVal(),
                        "Y"
                );
                list.add(reqVO);
            }
            insertSprtLmt(list);
        } else {
            // 기존 데이터 존재
        }
    }


    @Transactional
    public void updateSprtLmtNcnt(List<NcntReqVO> req, String tpwSvcTypId) {
        List<String> updList = new ArrayList<>();
        List<SprtLmtReqVO> list = new ArrayList<>();
        List<SprtLmtRspVO> resList = readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, "Y");
        SprtLmtRspVO res = resList.get(resList.size() - 1);

        for (NcntReqVO a : req) {
            SprtLmtReqVO reqVO = new SprtLmtReqVO(
                    res.getTpwSvcId(),
                    tpwSvcTypId,
                    a.getSpfnLmtMngNo(),
                    readSpfnLmtSnoNextVal(res.getSpfnLmtSno()),
                    res.getTpwLmtDvsCd(),
                    res.getTpwLmtTypCd(),
                    res.getLmtSttYm(),
                    res.getLmtEndYm(),
                    a.getMinCndtVal(),
                    a.getMaxCndtVal(),
                    a.getTgtAdptVal(),
                    "Y"
            );
            updList.add(a.getSpfnLmtMngNo());
            list.add(reqVO);
        }

        updatePrevSprtLmt(updList, tpwSvcTypId);
        insertNextSprtLmt(list, tpwSvcTypId);
    }

    @Transactional
    public void insertSprtLmt(List<SprtLmtReqVO> req) {
        sprtLmtMapper.insertSprtLmt(req);
    }

    @Transactional
    public void updatePrevSprtLmt(List<String> req, String tpwSvcTypId) {
        sprtLmtMapper.updatePrevSprtLmt(req, tpwSvcTypId);
    }

    @Transactional
    public void insertNextSprtLmt(List<SprtLmtReqVO> req, String tpwSvcTypId) {
        sprtLmtMapper.insertNextSprtLmt(req, tpwSvcTypId);
    }

    @Transactional(readOnly = true)
    public String readSpfnLmtMngNoNextVal() {
        return sprtLmtMapper.readSpfnLmtMngNoNextVal();
    }

    @Transactional(readOnly = true)
    public String readSpfnLmtSnoNextVal(String spfnLmtSno) {
        return sprtLmtMapper.readSpfnLmtSnoNextVal(spfnLmtSno);
    }
}
