package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Getter
@Setter
public class SimPtInfReqVO {

    @Size(max = 7, message = "기관코드는 7 이하의 ㅣㄹ이어야 합니다.")
    private String orgCd; //  기관코드

    @Size(max = 8, message = "신청기간 시작일자는 8자리 이하의 길이어야 합니다.")
    private String sttDt;              // 신청기간 시작일자 (YYYYMMDD)

    @Size(max = 8, message = "신청기간 종료일자는 8자리 이하의 길이어야 합니다.")
    private String endDt;              // 신청기간 종료일자 (YYYYMMDD)

    @Size(max = 100, message = "카드번호는 100자리 이하의 길이어야 합니다.")
    private String cardNo;             // 카드번호

    @Size(max = 500, message = "서비스명은 500자리 이하의 길이어야 합니다.")
    private String svcNm;              // 서비스명

    @Size(max = 100, message = "서비스유형은 100자리 이하의 길이어야 합니다.")
    private String svcTypNm;           // 서비스유형명

    @Size(max = 7, message = "서비스ID는 7자리 이하의 길이어야 합니다.")
    private String tpwSvcId;           // 서비스ID

    @Size(max = 10, message = "서비스유형ID는 10자리 이하의 길이어야 합니다.")
    private String tpwSvcTypId;        // 서비스유형ID

    @Size(max = 10, message = "서비스유형일련번호는 10자리 이하의 길이어야 합니다.")
    private String tpwSvcTypSno;       // 서비스유형일련번호

    @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.")
    private int page = 0;


    @Positive(message =  "페이지 값은 0보다 커야 합니다.")
    private int size = 10;

    @Size(max = 10, message = "정랼값의 문자열 길이는  500자리 이하의 길이어야 합니다.")
    private String sort = "mbrs_id";
    private String dir = "asc";

    public SimPtInfReqVO(String orgCd, @Size(max = 8, message = "신청기간 시작일자는 8자리 이하의 길이어야 합니다.") String sttDt, @Size(max = 8, message = "신청기간 종료일자는 8자리 이하의 길이어야 합니다.") String endDt, @Size(max = 100, message = "카드번호는 100자리 이하의 길이어야 합니다.") String cardNo, @Size(max = 500, message = "서비스명은 500자리 이하의 길이어야 합니다.") String svcNm, @Size(max = 100, message = "서비스유형은 100자리 이하의 길이어야 합니다.") String svcTypNm, @Size(max = 7, message = "서비스ID는 7자리 이하의 길이어야 합니다.") String tpwSvcId, @Size(max = 10, message = "서비스유형ID는 10자리 이하의 길이어야 합니다.") String tpwSvcTypId, @Size(max = 10, message = "서비스유형일련번호는 10자리 이하의 길이어야 합니다.") String tpwSvcTypSno, int offset, @PositiveOrZero(message = "페이지 값은 음수가 될 수 없습니다.") int page, @Positive(message =  "페이지 값은 0보다 커야 합니다.") int size, @Size(max = 10, message = "정랼값의 문자열 길이는  500자리 이하의 길이어야 합니다.") String sort, String dir) {
    }

    public void updateDefaultDate(String startDate, String endDate) {
        this.sttDt = startDate;
        this.endDt = endDate;
    }
}
