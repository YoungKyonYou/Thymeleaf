package tmoney.co.kr.hxz.spfnsprtmng.payinf.mapper;

import org.springframework.data.repository.query.Param;
import tmoney.co.kr.config.HxzDb;
import org.apache.ibatis.annotations.Mapper;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcDtlRspVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfReqVO; // 💡 변경된 Req VO 경로
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcPtInfRspVO; // 💡 변경된 Rsp VO 경로
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt.SprtSvcTypRspVO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@HxzDb
@Mapper
public interface SprtSvcPtInfMapper { // 💡 Mapper 이름 변경
    // =====================
    // 201 지원서비스 (상위)
    // =====================
    long readSprtSvcPtInfListCnt(@Param("req") SprtSvcPtInfReqVO req,
                                 @Param("orgCd") String orgCd);
    List<SprtSvcDtlRspVO> readSprtSvcPtInfList(@Param("req") SprtSvcPtInfReqVO req,
                                               @Param("orgCd") String orgCd);

    SprtSvcDtlRspVO readSprtSvcPtInf(@Param("tpwSvcId") String tpwSvcId,
                                     @Param("orgCd") String orgCd, int i, int i1);


    void saveSprtSvcPtInf(@Param("form") SprtSvcPtInfRspVO form);

    // 지원서비스 업데이트
    void updateSprtSvcPtInf(@Param("form") SprtSvcPtInfRspVO form);

    // =====================
    // 202 서비스유형 (하위)
    // =====================
    List<SprtSvcTypRspVO> readSprtSvcTypList(@Param("tpwSvcId") String tpwSvcId);
    SprtSvcTypRspVO readSprtSvcTyp(@Param("tpwSvcTypId") String tpwSvcTypId,
                                   @Param("tpwSvcTypSno") BigDecimal tpwSvcTypSno,
                                   @Param("tpwSvcId") String tpwSvcId);


    void saveSprtSvcTyp(@Param("typ") SprtSvcTypRspVO typ);
    SprtSvcTypRspVO updateSprtSvcTyp(@Param("typ") SprtSvcTypRspVO typ);


    void updateUseYnN(@Param("form") SprtSvcTypRspVO form);


    String generateNewSvcTypId(
            @NotBlank(message = "서비스 ID는 필수 입력 항목입니다.")
            @Size(max = 7, message = "서비스 ID는 최대 7자입니다.")
            @Param("tpwSvcId") String tpwSvcId
    );

    SprtSvcTypRspVO readSprtSvcTypById(@Param("form") SprtSvcTypRspVO form);

    long readSprtSvcTypListCnt(@Param("tpwSvcId") String tpwSvcId);

    List<SprtSvcTypRspVO> rreadSprtSvcTypListPaging(@Param("tpwSvcId") String tpwSvcId,
                                                    @Param("size") int size,
                                                    @Param("offset") int offset);

    List<SprtSvcTypRspVO> readSprtSvcTypListPaging(String tpwSvcId, int size, int offset);


    // 페이징용
    List<SprtSvcTypRspVO> readSprtSvcTypListPaging(Map<String, Object> params);


}