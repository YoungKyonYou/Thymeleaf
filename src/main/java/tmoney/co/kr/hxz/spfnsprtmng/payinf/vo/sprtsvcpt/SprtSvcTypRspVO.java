package tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.sprtsvcpt;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * ============================================
 * SprtSvcTypRspVO
 * - TBHXZM202 (202 서비스 유형 단건 VO)
 * ============================================
 */
@Data
public class SprtSvcTypRspVO {

    // -------------------------------
    // PK (NOT NULL)
    // -------------------------------
    @NotBlank(message = "서비스 ID는 필수 입력 항목입니다.")
    @Size(max = 7, message = "서비스 ID는 최대 7자입니다.")
    private String tpwSvcId;                // VARCHAR(7) NOT NULL

    @NotBlank(message = "서비스 유형 ID는 필수 입력 항목입니다.")
    @Size(max = 10, message = "서비스 유형 ID는 최대 10자입니다.")
    private String tpwSvcTypId;             // VARCHAR(10) NOT NULL

    @NotNull(message = "서비스 유형 일련번호는 필수 입력 항목입니다.")
    @Digits(integer = 10, fraction = 0, message = "서비스유형순번은 최대 10자리 숫자만 가능합니다.")
    private BigDecimal tpwSvcTypSno;              // NUMERIC(10) NOT NULL


    // -------------------------------
    // 일반 정보
    // -------------------------------
    @Size(max = 100, message = "서비스 유형명은 최대 100자입니다.")
    private String tpwSvcTypNm;             // VARCHAR(100)

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "시작일자는 yyyy-MM-dd 형식이어야 합니다.")
    private String tpwSvcTypSttDt;          // VARCHAR(8)

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "종료일자는 yyyy-MM-dd 형식이어야 합니다.")
    private String tpwSvcTypEndDt ="9999-12-31";          // VARCHAR(8)

    @Size(max = 2000, message = "서비스 유형 내용은 최대 2000자입니다.")
    private String tpwSvcTypCtt;            // VARCHAR(2000)

    /** HXZ 회원유형코드 (HXZM00007 코드 테이블 참조) */
    @Size(max = 3, message = "회원유형코드는 최대 3자입니다.")
    private String tpwMbrsTypCd;            // VARCHAR(3)

    /*
      유지 코드
      비고: HXZ_교통정산관리
      000:기타
      100:버스
      200: 도시철도
      300: 택시
      101: 도시형버스
      102:일반좌석버스
     */
    /** HXZ_교통정산관리 코드 (HXZM00002 코드 테이블 참조) */
    @Size(max = 10, message = "지원교통수단코드는 최대 10자입니다.")
    @NotBlank(message = "지원교통수단코드는 필수 입력 항목입니다.")
    private String tpwMntnCd;               // VARCHAR(10)

    /**
     * 정산 주기 구분 코드 (드롭다운)
     * 01: 월정산 (기본값)
     * 02: 분기
     */
    @Size(max = 2, message = "정산주기구분코드는 최대 2자입니다.")
    private String tpwStlmCycDvsCd;         // VARCHAR(2)

    /**
     * 정산 분류 코드 (드롭다운)
     * 01: 정액 (기본값)
     * 02: 정률
     */
    @NotBlank(message = "정산분류코드는 필수 입력 항목입니다.")
    @Size(max = 2, message = "정산분류코드는 최대 2자입니다.")
    private String tpwStlmCtgCd = "01";      // VARCHAR(2) NOT NULL

    /**
     * 정산 분류 적용 값
     * 비고: 지급금액에 따른 적용 금액 또는 할인율 값
     */
    @Digits(integer = 15, fraction = 0, message = "정산분류값은 최대 15자리 숫자여야 합니다.")
    private BigDecimal stlmCtgAdptVal = BigDecimal.ZERO;      // NUMERIC(15)


    // -------------------------------
    // Y/N 코드 필드 모음
    // -------------------------------
    /** 교통 거래 요청 여부 (Y/N) (기본값 N) */
    @Pattern(regexp = "[YN]?", message = "교통거래요청 값은 Y 또는 N이어야 합니다.")
    private String trnsTrdReqYn = "N";       // VARCHAR(1) NOT NULL

    /** 원장 거래 요청 여부 (Y/N) (기본값 N) */
    @Pattern(regexp = "[YN]?", message = "원장거래요청 값은 Y 또는 N이어야 합니다.")
    private String ldgrTrdReqYn = "N";       // VARCHAR(1) NOT NULL

    /** 택시 거래 요청 여부 (Y/N) (기본값 N) */
    @Pattern(regexp = "[YN]?", message = "택시거래요청 값은 Y 또는 N이어야 합니다.")
    private String taxiTrdReqYn = "N";       // VARCHAR(1) NOT NULL

    /** 지역 거래 요청 여부 (Y/N) (기본값 N) */
    @Pattern(regexp = "[YN]?", message = "지역거래요청 값은 Y 또는 N이어야 합니다.")
    private String areaTrdReqYn = "N";       // VARCHAR(1) NOT NULL


    /**
     * 정산 실행 단계 구분 코드 (드롭다운)
     * 01: 지원대상 (기본값)
     * 02: 거래내역
     * 03: 정산내역
     * 04: 지급실행
     */
    @Size(max = 1, message = "정산실행단계 코드는 1자입니다.")
    @NotBlank(message = "정산실행단계 코드는 필수 입력 항목입니다.")
    private String tpwStlmActDvsCd = "01";   // VARCHAR(1) NOT NULL

    /**
     * 거주지 인증 주기 코드
     * 비고: 거주지인증주기 분기기반기관 (코드 테이블 확인 필요)
     * 월 01, 분기02, 반기03 cmn_grp_cd_id :0003
     */
    @Size(max = 2, message = "거주지인증주기 코드는 최대 2자입니다.")
    private String tpwRsdcAuthCycCd;         // VARCHAR(2)

    /**
     * 이월 구분 코드
     * 비고: 없음, 분기내, 연환 이월 (코드 테이블 확인 필요)
     */
    @Size(max = 2, message = "이월구분 코드는 최대 2자입니다.")
    private String tpwCrovDvsCd;             // VARCHAR(2)

    /** 지원 중복 여부 (Y/N) (기본값 N) */
    @Pattern(regexp = "[YN]?", message = "지원중복여부 값은 Y 또는 N이어야 합니다.")
    private String sprtDplcYn = "N";         // VARCHAR(1) NOT NULL

    /** 거래 기관 코드 (교통거래정보 요청 기관코드) */
    @Size(max = 7, message = "거래기관코드는 최대 7자입니다.")
    private String tpwTrdOrgCd;              // VARCHAR(7)

    /**
     * 교통 거래 다인승 제외 여부 (Y/N)
     * 기본값: Y
     */
    @Pattern(regexp = "[YN]?", message = "다인승제외여부 값은 Y 또는 N이어야 합니다.")
    private String trnsTrdMlprExYn = "Y";    // VARCHAR(1) NOT NULL
    /** 자동 신청 여부 (Y/N) (기본값 N) */
    @Pattern(regexp = "[YN]?", message = "자동신청여부 값은 Y 또는 N이어야 합니다.")
    private String autAplYn = "N";           // VARCHAR(1) NOT NULL

    /** 증빙 서류 여부 (Y/N) (기본값 N) */
    @Pattern(regexp = "[YN]?", message = "증빙서류여부 값은 Y 또는 N이어야 합니다.")
    private String evdnYn = "N";             // VARCHAR(1) NOT NULL


    /** 외국인 지원 여부 (Y/N) (기본값 N) */
    @Pattern(regexp = "[YN]?", message = "외국인지원여부 값은 Y 또는 N이어야 합니다.")
    private String frgnSprtYn = "N";         // VARCHAR(1) NOT NULL

    /**
     * 거래 건수 제한 적용 여부 (Y/N)
     * N: 거래건수 제한 미적용 (기본값)
     * Y: 교통거래횟수 제한이 있는 유형의 경우 Y로 선택
     */
    @Pattern(regexp = "[YN]?", message = "거래건수제한여부 값은 Y 또는 N이어야 합니다.")
    private String trdNcntLtnAdptYn = "N";   // VARCHAR(1) NOT NULL

    /**
     * 사용 여부 (Y/N)
     * 기본값: Y
     */
    @NotBlank(message = "사용여부는 필수이며 Y 또는 N만 가능합니다.")
    @Pattern(regexp = "^[YN]$", message = "사용여부는 Y 또는 N만 가능합니다.")
    private String useYn = "Y";              // VARCHAR(1) NOT NULL


    // -------------------------------
    // 등록/수정 정보
    // -------------------------------
    @Size(max = 20, message = "등록자 ID는 최대 20자입니다.")
    @NotBlank(message = "등록자 ID는 필수 입력 항목입니다.")
    private String rgsrId;                   // VARCHAR(20) NOT NULL

    @Pattern(regexp = "\\d{14}", message = "등록일시는 YYYYMMDDHHMMSS 14자리여야 합니다.")
    private String rgtDtm;

    @Size(max = 20, message = "수정자 ID는 최대 20자입니다.")
    @NotBlank(message = "수정자 ID는 필수 입력 항목입니다.")
    private String updrId;                   // VARCHAR(20) NOT NULL

    @Pattern(regexp = "\\d{14}", message = "수정일시는 YYYYMMDDHHMMSS 14자리여야 합니다.")
    private String updDtm;
}
