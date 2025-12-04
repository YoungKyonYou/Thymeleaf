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

    @Override
    @Transactional
    public void saveMemrStlmPt(MemrStlmPtRspVO form) {
        memrStlmPtMapper.saveMemrStlmPt(form);
    }

    @Override
    @Transactional
    public void updateMemrStlmPt(MemrStlmPtRspVO form) {
        int updatedRows = memrStlmPtMapper.updateMemrStlmPt(form);
        if (updatedRows == 0) {
            // PK가 틀렸거나, 이미 지급처리(Y)되어 수정 조건에 맞지 않는 경우
            throw new RuntimeException("수정할 수 없거나 존재하지 않는 데이터입니다. (이미 지급처리 되었을 수 있음)");
        }
    }

    @Override
    @Transactional
    public void deleteMemrStlmPt(MemrStlmPtRspVO form) {
        memrStlmPtMapper.deleteMemrStlmPt(form);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 하나라도 실패하면 전체 롤백
    public void deleteMemrStlmPtList(List<MemrStlmPtRspVO> list) {
        for (MemrStlmPtRspVO vo : list) {

            // 1. 지급처리여부('Y') 검증 (프론트에서 막았어도 백엔드에서 한 번 더)
            if ("Y".equals(vo.getPayPrcgYn())) {
                throw new IllegalArgumentException("지급처리 완료된 건(ID: " + vo.getMbrsId() + ")은 삭제할 수 없습니다.");
            }

            // 2. 기관코드 방어 로직
            if (vo.getOrgCd() == null || vo.getOrgCd().isEmpty()) {
                vo.setOrgCd("000000");
            }

            // 3. 기존의 단건 삭제 Mapper 호출 (루프 돔)
            memrStlmPtMapper.deleteMemrStlmPt(vo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApproveMemrStlmPtList(List<MemrStlmPtRspVO> list) {
        for (MemrStlmPtRspVO vo : list) {
            // [검증] 이미 지급처리된 건은 스킵하거나 에러 처리 (여기선 스킵)
            if ("Y".equals(vo.getPayPrcgYn())) {
                continue;
            }

            // 승인 처리: payPrcgYn = 'Y', manualPrcgSta = '02'(승인) 등으로 변경
            vo.setPayPrcgYn("Y");

            // 필요하다면 수기처리상태도 '승인(02)'으로 변경
             vo.setManualPrcgSta("02");

            // 업데이트 날짜, 수정자 ID 등 세팅
            // vo.setUpdrId("SYSTEM"); // 로그인 세션 ID 사용 권장

            // 기존 수정 Mapper 호출 (또는 전용 승인 Mapper 생성 가능)
            // 여기서는 기존 updateMemrStlmPt 재활용
            memrStlmPtMapper.updateMemrStlmPt(vo);
        }
    }
}