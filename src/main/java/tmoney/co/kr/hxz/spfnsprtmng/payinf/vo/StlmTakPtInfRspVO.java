package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 마감확정내역 응답 VO (조회 + 신규등록 검증용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StlmTakPtInfRspVO {

    /**
     * 실행구분 (PERD: 정기, SIM: 시뮬레이션)
     * 디폴트: PERD
     */
    @NotBlank(message = "실행구분은 필수입니다.")
    @Pattern(regexp = "^(PERD|SIM)$", message = "실행구분은 PERD 또는 SIM만 가능합니다.")
    private String exeDiv = "PERD";

    /** 기관명 */
    @Size(max = 100, message = "기관명은 100자리 이하이어야 합니다.")
    private String orgNm;

    /** 서비스명 */
    @Size(max = 500, message = "서비스명은 500자리 이하이어야 합니다.")
    private String tpwSvcNm;

    /** 서비스유형명 */
    @Size(max = 100, message = "서비스유형명은 100자리 이하이어야 합니다.")
    private String tpwSvcTypNm;

    /** 신청시작일자 (YYYYMMDD) */
    @Size(max = 8, message = "신청일자는 8자리 이하이어야 합니다.")
    @Pattern(regexp = "^[0-9]{8}$", message = "신청일자는 8자리 숫자(YYYYMMDD) 형식이어야 합니다.")
    private String aplSttDt;

    /** 신청종료일자 (YYYYMMDD) */
    @Size(max = 8, message = "신청일자는 8자리 이하이어야 합니다.")
    @Pattern(regexp = "^[0-9]{8}$", message = "신청일자는 8자리 숫자(YYYYMMDD) 형식이어야 합니다.")
    private String aplEndDt;

    /** 신청일자 (YYYYMMDD) */
    @Size(max = 8, message = "신청일자는 8자리 이하이어야 합니다.")
    @Pattern(regexp = "^[0-9]{8}$", message = "신청일자는 8자리 숫자(YYYYMMDD) 형식이어야 합니다.")
    private String aplDt;

    /** 거래시작일자 (YYYYMMDD) */
    @Size(max = 8, message = "거래시작일자는 8자리 이하이어야 합니다.")
    @Pattern(regexp = "^[0-9]{8}$", message = "거래시작일자는 8자리 숫자(YYYYMMDD) 형식이어야 합니다.")
    private String tpwTrdSttDt;

    /** 거래종료일자 (YYYYMMDD) */
    @Size(max = 8, message = "거래종료일자는 8자리 이하이어야 합니다.")
    @Pattern(regexp = "^[0-9]{8}$", message = "거래종료일자는 8자리 숫자(YYYYMMDD) 형식이어야 합니다.")
    private String tpwTrdEndDt;

    /** 정산일자 (YYYYMMDD) */
    @Size(max = 8, message = "정산일자는 8자리 이하이어야 합니다.")
    @Pattern(regexp = "^[0-9]{8}$", message = "정산일자는 8자리 숫자(YYYYMMDD) 형식이어야 합니다.")
    private String stlmDt;

    /** 확정일자 (YYYYMMDD) */
    @Size(max = 8, message = "확정일자는 8자리 이하이어야 합니다.")
    @Pattern(regexp = "^[0-9]{8}$", message = "확정일자는 8자리 숫자(YYYYMMDD) 형식이어야 합니다.")
    private String fixDt;

    /** 마감확정여부 (Y/N)
     *  ^[YN]$ → Y 또는 N만 허용
     */
    @Pattern(regexp = "^[YN]$", message = "마감확정여부는 Y 또는 N이어야 합니다.")
    private String closCfmYn;

    /** 정산확정여부 (Y/N)
     *  ^[YN]$ → Y 또는 N만 허용
     */
    @Pattern(regexp = "^[YN]$", message = "정산확정여부는 Y 또는 N이어야 합니다.")
    private String stlmFixYn;

    /** 회계처리완료여부 (Y/N)
     *  ^[YN]$ → Y 또는 N만 허용
     */
    @Pattern(regexp = "^[YN]$", message = "회계처리여부는 Y 또는 N이어야 합니다.")
    private String acngPrcgFnYn;

    /** 회계처리완료여부 (Y/N)
     *  ^[YN]$ → Y 또는 N만 허용
     */
    @Pattern(regexp = "^[YN]$", message = "회계처리여부는 Y 또는 N이어야 합니다.")
    private String prcgFnYn;

    /** 서비스ID */
    @Size(max = 7, message = "서비스ID는 7자리 이하이어야 합니다.")
    private String tpwSvcId;

    /** 서비스유형ID */
    @Size(max = 10, message = "서비스유형ID는 10자리 이하이어야 합니다.")
    private String tpwSvcTypId;

    /** 서비스유형일련번호 (numeric(10)) */
    @PositiveOrZero(message = "서비스유형일련번호는 0 이상이어야 합니다.")
    private BigDecimal tpwSvcTypSno;




}
