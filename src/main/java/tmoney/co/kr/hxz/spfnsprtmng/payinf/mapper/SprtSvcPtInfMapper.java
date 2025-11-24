package tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper;

import org.apache.ibatis.annotations.Mapper;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcDtlRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcTypRspVO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@HxzDb
@Mapper
public interface SprtSvcPtInfMapper { // 💡 Mapper 이름 변경
    // =====================
    // 201 지원서비스 (상위)
    // =====================
    long readSprtSvcPtInfListCnt(SprtSvcPtInfReqVO reqVO, String orgCd);
    List<SprtSvcDtlRspVO> readSprtSvcPtInfList(SprtSvcPtInfReqVO reqVO, String orgCd);

    SprtSvcDtlRspVO findSprtSvcPtInf(String tpwSvcId, String orgCd);

    void saveSprtSvcPtInf(SprtSvcPtInfRspVO form);

    // 지원서비스 업데이트
    void updateSprtSvcPtInf(SprtSvcPtInfRspVO form);

    // =====================
    // 202 서비스유형 (하위)
    // =====================
    List<SprtSvcTypRspVO> findSprtSvcTypList(String tpwSvcId);
    SprtSvcTypRspVO findSprtSvcTyp(String tpwSvcTypId, BigDecimal tpwSvcTypSno, String tpwSvcId);

    void saveSprtSvcTyp(SprtSvcTypRspVO typ);
    SprtSvcTypRspVO updateSprtSvcTyp(SprtSvcTypRspVO typ);


    void updateUseYnN(SprtSvcTypRspVO form);

    String generateNewSvcTypId(@NotBlank(message = "서비스 ID는 필수 입력 항목입니다.") @Size(max = 7, message = "서비스 ID는 최대 7자입니다.") String tpwSvcId);

    SprtSvcTypRspVO findSprtSvcTypById(SprtSvcTypRspVO form);
}