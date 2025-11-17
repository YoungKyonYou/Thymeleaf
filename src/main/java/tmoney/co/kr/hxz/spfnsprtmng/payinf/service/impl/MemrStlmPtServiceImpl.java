package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper.MemrStlmPtMapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.MemrStlmPtService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtRspVO;

import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MemrStlmPtServiceImpl implements MemrStlmPtService {

    private final MemrStlmPtMapper memrStlmPtMapper;

    @Override
    @Transactional(readOnly = true)
    public PageDataVO<MemrStlmPtRspVO> readMemrStlmPtPaging(MemrStlmPtReqVO req, String orgCd) {
        final int offset = req.getPage() * req.getSize();
        long total = readMemrStlmPtListCnt(req, orgCd);

        // 요청 VO를 기반으로 새로운 VO를 생성하여 페이징 관련 필드를 설정
        MemrStlmPtReqVO reqVO = new MemrStlmPtReqVO(
            req.getReqDtm(),
            req.getPrcgDt(),
            req.getSearchType(),
            req.getKeyword(),
            req.getOrgCd() // 이 생성자는 이미지 10에서 유추한 것입니다.
        );
        // 추가적인 검색 조건 설정 (이미지 5에서 누락된 부분은 주석 처리 또는 유추하여 삽입)
        // reqVO.setReqDtm(); // 중복 설정
        // reqVO.setPrcgDt(); // 중복 설정
        reqVO.setSearchType(req.getSearchType());
        reqVO.setKeyword(req.getKeyword());
        reqVO.setTpwMemrPrcgStaCd(req.getTpwMemrPrcgStaCd());
        reqVO.setMbrsId(req.getMbrsId());
        reqVO.setOrgCd(orgCd); // orgCd는 파라미터로 받음
        reqVO.setMbrsNm(req.getMbrsNm());
        reqVO.setTpwSvcId(req.getTpwSvcId());
        reqVO.setTpwSvcNm(req.getTpwSvcNm());
        reqVO.setTpwSvcTypId(req.getTpwSvcTypId());
        reqVO.setTpwSvcTypNm(req.getTpwSvcTypNm());
        reqVO.setTpwSvcTypSno(req.getTpwSvcTypSno());
        reqVO.setSttDt(req.getSttDt());
        reqVO.setEndDt(req.getEndDt());

        // 페이징/정렬 정보 설정
        reqVO.setOffset(offset);
        reqVO.setSize(req.getSize());
        reqVO.setSort(req.getSort());
        reqVO.setDir(req.getDir());

        List<MemrStlmPtRspVO> content = readMemrStlmPtList(reqVO, orgCd);

        return new PageDataVO<>(content, req.getPage(), req.getSize(), total);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MemrStlmPtRspVO> readMemrStlmPtList(MemrStlmPtReqVO req, String orgCd) {
        return memrStlmPtMapper.readMemrStlmPtList(req, orgCd);
    }

    @Transactional(readOnly = true)
    @Override
    public long readMemrStlmPtListCnt(MemrStlmPtReqVO req, String orgCd) {
        return memrStlmPtMapper.readMemrStlmPtListCnt(req, orgCd);
    }
}