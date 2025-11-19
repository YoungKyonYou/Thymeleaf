package tmoney.co.kr.hxz.sprtpolimng.polimnginf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.mapper.SprtLmtMapper;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtInstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntInstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtDtlRspVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtRspVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtSrchReqVO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Service
public class SprtLmtService  {

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
                req.getTpwSvcId(), req.getTpwSvcNm(), req.getTpwSvcTypId(), req.getTpwSvcTypNm(), req.getUseYn(), req.getTpwLmtDvsCd(),
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
    public void insertSprtLmtAmt(AmtInstReqVO req) {
        List<SprtLmtRspVO> resList = readSprtLmtDtlByTpwSvcTypId(req.getTpwSvcTypId(), "Y");

        List<SprtLmtReqVO> list = new ArrayList<>();
        if (resList.isEmpty()) {
            long num = Long.parseLong(readSpfnLmtMngNoNextVal());
            for (AmtReqVO a : req.getList()) {
                num += 1;
                SprtLmtReqVO reqVO = new SprtLmtReqVO(
                        req.getTpwSvcId(),
                        req.getTpwSvcTypId(),
                        String.format("%010d", num),
                        "0000000001",
                        "01",
                        req.getTpwLmtTypCd(),
                        a.getLmtSttYm(),
                        a.getLmtEndYm(),
                        0,
                        0,
                        a.getTgtAdptVal(),
                        "Y"
                );
                list.add(reqVO);
            }
            insertSprtLmt(list);
        } else {
            // 에러 메시지(기존 데이터 존재)
        }
    }


    @Transactional
    public void updateSprtLmtAmt(List<AmtReqVO> req, String tpwSvcTypId) {
        List<String> updList = new ArrayList<>();
        List<SprtLmtReqVO> list = new ArrayList<>();
        List<SprtLmtRspVO> resList = readSprtLmtDtlByTpwSvcTypId(tpwSvcTypId, "Y");
        SprtLmtRspVO res = resList.get(resList.size() - 1);

        for (AmtReqVO a : req) {
            SprtLmtReqVO reqVO = new SprtLmtReqVO(
                    res.getTpwSvcId(),
                    tpwSvcTypId,
                    a.getSpfnLmtMngNo(),
                    readSpfnLmtSnoNextVal(res.getSpfnLmtSno()),
                    res.getTpwLmtDvsCd(),
                    res.getTpwLmtTypCd(),
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
