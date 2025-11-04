package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.mapper.SprtLmtMapper;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtInstReqVO;
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

        List<SprtLmtReqVO> sprtLmtReqList = new ArrayList<>();
        for (SprtLmtRspVO a : sprtLmtDtlList) {
            SprtLmtReqVO b = new SprtLmtReqVO();
            b.setSpfnLmtMngNo(a.getSpfnLmtMngNo());
            b.setLmtSttYm(a.getLmtSttYm());
            b.setLmtEndYm(a.getLmtEndYm());
            b.setMinCndtVal(a.getMinCndtVal());
            b.setMaxCndtVal(a.getMaxCndtVal());
            b.setTgtAdptVal(a.getTgtAdptVal());
            sprtLmtReqList.add(b);
        }

        return new SprtLmtDtlRspVO(
                sprtLmtDtlList,
                sprtLmtReqList,
                sprtLmtDtlList.get(0).getTpwLmtDvsCd(),
                sprtLmtDtlList.get(0).getTpwLmtTypCd(),
                sprtLmtDtlList.get(0).getUseYn()
        );
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
    public void insertSprtLmtAmt(AmtInstReqVO req, String tpwSvcTypId) {
        //기존 데이터 가져오기
        List<SprtLmtRspVO> resList = readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, "Y");

        //월 한도인 경우 시작일자와 종료일자 맞춰줄 것
        if (req.getTpwLmtTypCd().equals("02")) {
            req.getList().forEach(a -> {
                a.setLmtEndYm(a.getLmtSttYm());
            });
        }

        int k = req.getList().size(); // 필요 개수
        List<SprtLmtReqVO> list = new ArrayList<>();

        //신규가 아니라 수정인 경우
        if(!resList.isEmpty()) {

            String curValueType = resList.get(0).getTpwLmtDvsCd();
            String nextValueType = req.getTpwLmtDvsCd();
            String curMonth = resList.get(0).getTpwLmtTypCd();
            String nextMonth = req.getTpwLmtTypCd();
            String curSno = resList.get(0).getSpfnLmtMngNo();
            boolean isSame = (curValueType.equals(nextValueType) && curMonth.equals(nextMonth));

            //한도유형이 같으면 그 같은 구간의 관리번호를 쓰거나 새로 채번
            Deque<String> mngNoPool = isSame
                    ? resList.stream()
                    .map(SprtLmtRspVO::getSpfnLmtMngNo)
                    .collect(Collectors.toCollection(ArrayDeque::new))
                    : new ArrayDeque<>(readNextMngNo(k));



            list = req.getList()
                    .stream()
                    .map(a -> new SprtLmtReqVO(
                            tpwSvcTypId,
                            req.getTpwSvcTypId(),
                            mngNoPool.removeFirst(),
                            readSpfnLmtSnoNextVal(curSno),
                            req.getTpwLmtDvsCd(),
                            req.getTpwLmtTypCd(),
                            a.getLmtSttYm(),
                            a.getLmtEndYm(),
                            0,
                            0,
                            a.getTgtAdptVal(),
                            "Y"


                    )).collect(Collectors.toList());
        }else{
            //신규인 경우
            Deque<String> mngNoPool = new ArrayDeque<>(readNextMngNo(k));

            list = req.getList()
                    .stream()
                    .map(a -> new SprtLmtReqVO(
                            tpwSvcTypId,
                            req.getTpwSvcTypId(),
                            mngNoPool.removeFirst(),
                            "0000000001",
                            req.getTpwLmtDvsCd(),
                            req.getTpwLmtTypCd(),
                            a.getLmtSttYm(),
                            a.getLmtEndYm(),
                            0,
                            0,
                            a.getTgtAdptVal(),
                            "Y"


                    )).collect(Collectors.toList());
        }

        //기존에 존재하는 서비스에 묶힌 한도가 존재한다면 useYn = 'N' 처리
        updateSprtLmtUseYn(req.getTpwSvcTypId());



        insertSprtLmt(list);

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
