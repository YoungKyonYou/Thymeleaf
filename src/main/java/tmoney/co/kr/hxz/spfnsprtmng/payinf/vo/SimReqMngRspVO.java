package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo;

import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 시뮬레이션 마감확정내역 검색 VO
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimReqMngRspVO {

    private String orgCd;

    /** tpw_svc_id (교통복지서비스ID) */
    @NotNull(message = "tpw_svc_id(교통복지서비스ID)는 필수값입니다.")
    @Size(max = 7, message = "tpw_svc_id는 7자리 이하입니다.")
    private String tpwSvcId;

    /** tpw_svc_typ_id (교통복지서비스유형ID) */
    @NotNull(message = "tpw_svc_typ_id(교통복지서비스유형ID)는 필수값입니다.")
    @Size(max = 10, message = "tpw_svc_typ_id는 10자리 이하입니다.")
    private String tpwSvcTypId;

    /** tpw_svc_typ_sno (교통복지서비스유형일련번호) */
    @NotNull(message = "tpw_svc_typ_sno(서비스유형일련번호)는 필수값입니다.")
    @PositiveOrZero(message = "tpw_svc_typ_sno는 음수일 수 없습니다.")
    private Integer tpwSvcTypSno;

    /** apl_dt (신청일자, yyyyMMdd) */
    @NotNull(message = "apl_dt(신청일자)는 필수값입니다.")
    @Size(max = 8, message = "apl_dt는 yyyyMMdd 형식(8자리)이어야 합니다.")
    private String aplDt;

    /** mbrs_id (회원ID) */
    @NotNull(message = "mbrs_id(회원ID)는 필수값입니다.")
    @Size(max = 20, message = "mbrs_id는 20자리 이하입니다.")
    private String mbrsId;

    /** mbrs_nm (회원명) */
    private String mbrsNm;

    /** apl_card_sno (카드일련번호) */
    @PositiveOrZero(message = "apl_card_sno는 음수일 수 없습니다.")
    private Integer aplCardSno;

    /** card_no (카드번호) */
    @Size(max = 100, message = "card_no는 100자리 이하입니다.")
    private String cardNo;

    /** card_stt_dt (카드시작일자, yyyyMMdd) */
    @Size(max = 8, message = "card_stt_dt는 yyyyMMdd 형식(8자리)이어야 합니다.")
    private String cardSttDt;

    /** card_end_dt (카드종료일자, yyyyMMdd) */
    @Size(max = 8, message = "card_end_dt는 yyyyMMdd 형식(8자리)이어야 합니다.")
    private String cardEndDt;

    /** tpw_svc_nm (서비스명) - 조회 조인 결과로 채워짐 */
    @Size(max = 500, message = "tpw_svc_nm은 500자 이하입니다.")
    private String tpwSvcNm;

    /** tpw_svc_typ_nm (서비스유형명) - 조회 조인 결과로 채워짐 */
    @Size(max = 100, message = "tpw_svc_typ_nm은 100자 이하입니다.")
    private String tpwSvcTypNm;

    /** prcg_fn_yn (처리완료여부, Y/N) */
    @NotNull(message = "prcg_fn_yn(처리완료여부)은 필수값입니다.")
    @Pattern(regexp = "^[YN]$", message = "prcg_fn_yn은 'Y' 또는 'N'만 가능합니다.")
    private String prcgFnYn;

    /** message (추가 응답 메시지 필드) */
    @Size(max = 1000, message = "message는 1000자 이하입니다.")
    private String message;
    
    /** 요청일자*/
    private String takReqDt;


    private String tpwTrdSttDt;
    private String tpwTrdEndDt;


    // [수정용] 변경 전 원본 데이터 (WHERE 절 탐색용)
    private String origCardNo;       // 기존 카드번호
    private String origTpwSvcId;     // 기존 서비스ID
    private String origTpwSvcTypId;  // 기존 서비스유형ID
    private String origTpwSvcTypSno; // 기존 서비스유형일련번호
    private String origAplDt; // 기존 신청일자
    private String origMbrsId; // 기존 회원id

}
