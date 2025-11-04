package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class InstReqVO {
    List<AmtReqVO> amtList = new ArrayList<>();
    List<NcntReqVO> ncntList = new ArrayList<>();

    /** 서비스ID */
    @Size(max = 7, message = "서비스ID의 길이는 7보다 작아야 합니다.")
    private String tpwSvcId;

    /** 서비스유형ID */
    @Size(max = 10, message = "서비스유형ID의 길이는 10자 이하여야 합니다.")
    private String tpwSvcTypId;

    /** 교통복지한도구분코드 */
    @Size(max = 2, message = "교통복지한도구분코드의 길이는 2보다 작아야 합니다.")
    private String tpwLmtDvsCd;

    /** 교통복지한도유형코드 */
    @Size(max = 2, message = "교통복지한도유형코드의 길이는 2보다 작아야 합니다.")
    private String tpwLmtTypCd;
}