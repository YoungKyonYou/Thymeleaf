package tmoney.co.kr.hxz.spfnsprtmng.payinf.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper.SprtSvcPtInfMapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.service.SprtSvcPtInfService;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcDtlRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcTypRspVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================
 * SprtSvcPtInfServiceImpl
 * - 지원 서비스(201) / 서비스유형(202) 서비스 구현체
 * ============================================
 */
@Service
@RequiredArgsConstructor
public class SprtSvcPtInfServiceImpl implements SprtSvcPtInfService {

    private final SprtSvcPtInfMapper sprtSvcPtInfMapper;

    // =====================
    // 201 지원서비스 (상위)
    // =====================
    @Override
    @Transactional(readOnly = true)
    public PageDataVO<SprtSvcDtlRspVO> readSprtSvcPtInfList(SprtSvcPtInfReqVO reqVO, String orgCd) {

        int offset = reqVO.getPage() * reqVO.getSize();



        long total = sprtSvcPtInfMapper.readSprtSvcPtInfListCnt(reqVO, orgCd);
        List<SprtSvcDtlRspVO> content = sprtSvcPtInfMapper.readSprtSvcPtInfList(reqVO, orgCd);

//        List<SprtSvcDtlRspVO> distinctContent = content.stream()
//                .distinct()
//                .collect(Collectors.toList());

        
        return new PageDataVO<>(
                content,
                reqVO.getPage(),
                reqVO.getSize(),
                total
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SprtSvcDtlRspVO readSprtSvcPtInf(String tpwSvcId, String orgCd, int page, int size) {

        // Mapper에서 SprtSvcPtInfRspVO로 조회
        SprtSvcDtlRspVO baseInfo = sprtSvcPtInfMapper.readSprtSvcPtInf(tpwSvcId, orgCd, 0, 1000);

        if (baseInfo == null) {
            return new SprtSvcDtlRspVO();
        }

        // BeanUtils로 DtlRspVO로 변환
        SprtSvcDtlRspVO main = new SprtSvcDtlRspVO();
        BeanUtils.copyProperties(baseInfo, main);


        int offset = page * size;




        // TODO: PageDataVo로 해줘야 할듯?
        // 하위 서비스유형 리스트 추가 조회
//        main.setSvcTypList(sprtSvcPtInfMapper.readSprtSvcTypList(tpwSvcId, size, offset));

        List<SprtSvcTypRspVO> subList = sprtSvcPtInfMapper.readSprtSvcTypListPaging(tpwSvcId, size, offset);

        main.setSvcTypList(subList);

        // 변환된 DtlRspVO를 반환
        return main;
    }


    @Override
    @Transactional
    public void saveSprtSvcPtInf(SprtSvcPtInfRspVO form) {
        // 상위 서비스 등록
        sprtSvcPtInfMapper.saveSprtSvcPtInf(form);

        // 하위 서비스유형이 포함되어 있으면 같이 등록
        if (form.getSvcTypList() != null && !form.getSvcTypList().isEmpty()) {
            form.getSvcTypList().forEach(typ -> {
                typ.setTpwSvcId(form.getTpwSvcId());
                sprtSvcPtInfMapper.saveSprtSvcTyp(typ);
            });
        }

    }

    @Override
    @Transactional
    public void updateSprtSvcPtInfByService(SprtSvcPtInfRspVO form) {
        // 상위 서비스 수정
        sprtSvcPtInfMapper.updateSprtSvcPtInf(form);

        // 하위 유형 리스트가 포함되어 있으면 각각 수정
        // if (form.getSvcTypList() != null && !form.getSvcTypList().isEmpty()) {
        //     form.getSvcTypList().forEach(typ -> {
        //         typ.setTpwSvcId(form.getTpwSvcId());
        //         sprtSvcPtInfMapper.updateSprtSvcPt(typ);
        //     });
        // }
    }

    // =====================
    // 202 서비스유형 (하위)
    // =====================
    /** -----------------------------------------
     * 1. 지원유형 리스트 조회
     * ---------------------------------------- */
    @Override
    @Transactional(readOnly = true)
    public List<SprtSvcTypRspVO> readSprtSvcTypList(String tpwSvcId) {
        return sprtSvcPtInfMapper.readSprtSvcTypList(tpwSvcId);
    }

    /** -----------------------------------------
     * 2. 지원유형 단건 상세보기
     * ---------------------------------------- */
    @Override
    @Transactional(readOnly = true)
    public SprtSvcTypRspVO readSprtSvcTyp(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String tpwSvcId) {
        return sprtSvcPtInfMapper.readSprtSvcTyp(tpwSvcTypId, tpwSvcTypSno, tpwSvcId);
    }

    /** -----------------------------------------
     * 3. 신규 등록
     * ---------------------------------------- */
    @Override
    public void saveSprtSvcTyp(SprtSvcTypRspVO form) {
        // 1. tpwSvcTypId 없으면 신규 생성
        if (form.getTpwSvcTypId() == null || form.getTpwSvcTypId().isEmpty()) {
            String newTypId = sprtSvcPtInfMapper.generateNewSvcTypId(form.getTpwSvcId());
            form.setTpwSvcTypId(newTypId);
        }

        // 2. sno 계산 및 insert

        sprtSvcPtInfMapper.saveSprtSvcTyp(form);
    }

    /** -----------------------------------------
     * 수정
     * 1. 기존 데이터 useYn='N' 처리
     * 2. 기존 데이터 조회 (히스토리용)
     * 3. 새로운 VO 생성 + sno 증가
     * 4. 신규 insert
     * ---------------------------------------- */
    @Override
    public void updateSprtSvcTyp(SprtSvcTypRspVO form) {
        // 1. 기존 데이터 useYn 'N'으로 업데이트
        sprtSvcPtInfMapper.updateUseYnN(form);

        // 2. 기존 데이터 조회 (히스토리 생성용)
        SprtSvcTypRspVO existing = sprtSvcPtInfMapper.readSprtSvcTypById(form);

        if (existing == null) {
            throw new RuntimeException("해당 지원서비스유형이 존재하지 않습니다.");
        }

        // 3. 새로 삽입할 VO 생성 (기존 데이터 + 변경 내용 + sno +1)
        SprtSvcTypRspVO newVo = new SprtSvcTypRspVO();

        // null 값 제외하고 덮어쓰기
        copyNonNullProperties(form, existing);  // form의 값이 null이 아닌 경우만 existing에 복사
        BeanUtils.copyProperties(existing, newVo); // 기존 + 덮어쓴 값 복사

        newVo.setTpwSvcTypSno(existing.getTpwSvcTypSno().add(BigDecimal.ONE)); // sno 증가
        newVo.setUseYn("Y"); // 활성화

        // 4. 신규 insert
        sprtSvcPtInfMapper.saveSprtSvcTyp(newVo);
    }

    /**
     * form의 null이 아닌 값만 기존 데이터에 덮어쓰기
     */
    private void copyNonNullProperties(SprtSvcTypRspVO src, SprtSvcTypRspVO target) {
        BeanWrapper srcWrap = new BeanWrapperImpl(src);
        BeanWrapper targetWrap = new BeanWrapperImpl(target);

        for (java.beans.PropertyDescriptor pd : srcWrap.getPropertyDescriptors()) {
            String propName = pd.getName();

            // TODO: 에러 땜빵이니 정상작동하는지 확인필요
            // class는 무시
            if ("class".equals(propName)) {
                continue;
            }

            Object value = srcWrap.getPropertyValue(propName);
            if (value != null) {
                targetWrap.setPropertyValue(propName, value);
            }
        }
    }
    /** -----------------------------------------
     * useYn='N' 처리
     * ---------------------------------------- */
    @Override
    @Transactional
    public void updateUseYnN(SprtSvcTypRspVO form) {
        // Mapper의 updateUseYnN은 단일 VO를 받도록 XML과 매칭되어 있습니다.
        sprtSvcPtInfMapper.updateUseYnN(form);
    }

    @Override
    public long readSprtSvcTypListCnt(String tpwSvcId) {
        return sprtSvcPtInfMapper.readSprtSvcTypListCnt(tpwSvcId);
    }
}