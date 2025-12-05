package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data

public class SprtSvcPtInfRspVO {

    private String orgCd;          // 기관코드
    private String tpwOrgNm;          // 기관명
    private String tpwSvcId;       // 서비스ID
    private String tpwSvcNm;       // 서비스명
    private String tpwSvcCtt;      // 서비스내용
    private String tpwSvcSttDt;    // 서비스시작일자
    private String tpwSvcEndDt;    // 서비스종료일자
    private String useYn;          // 사용여부
    private String rgsrId;         // 등록자ID
    private String rgtDtm;         // 등록일시
    private String updrId;         // 수정자ID
    private String updDtm;         // 수정일시

    private String krnChecYn;         // 주민등록번호체크여부
    private String acngTrdpNo;         // 회계 거래처번호
    private String bnkTrnCtt;         // 은행 계좌/거래
    private String tpwSvcScrnPathNm; //화면경로

    private List<SprtSvcTypRspVO> svcTypList;



    //  sprt.orgCd as orgCd,
    //     sprt.tpwSvcCtt as tpwSvcCtt,        <!-- 지원서비스내용 -->
    //     sprt.tpwSvcSttDt as tpwSvcSttDt,    <!-- 서비스시작일자 -->
    //     sprt.tpwSvcEndDt as tpwSvcEndDt,    <!-- 서비스종료일자 -->
    //     sprt.tpwSvcTypNm as tpwSvcTypNm,      <!-- 서비스유형 -->
    //     sprt.tpwSvcId as tpwSvcId,
    //     sprt.tpwOrgNm as tpwOrgNm
}
