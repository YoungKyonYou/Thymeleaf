package tmoney.co.kr.hxz.sprtpolimng.polimnginf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tmoney.co.kr.config.HxzDb;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.QuarterRangeVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtKindVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtRspVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.SprtLmtSrchReqVO;

import java.util.List;

@HxzDb
@Mapper
public interface SprtLmtMapper {

    /**
     * 지원 한도 내역 조회
     *
     * @param req 지원 한도 검색 조건
     * @return 지원 한도 내역 리스트
     */
    List<SprtLmtRspVO> readSprtLmtPtList(
            @Param("req") SprtLmtSrchReqVO req
    );

    /**
     * 지원 한도 내역 카운트 조회
     *
     * @param req 지원 한도 검색 조건
     * @return 검색 조건에 해당하는 전체 건수
     */
    long readSprtLmtPtListCnt(@Param("req") SprtLmtSrchReqVO req);

    /**
     * 서비스/유형별 지원 한도 상세 조회
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @param useYn       사용 여부(Y/N)
     * @return 조건에 해당하는 지원 한도 상세 리스트
     */
    List<SprtLmtRspVO> readSprtLmtDtlByTpwSvc(
            @Param("tpwSvcId") String tpwSvcId,
            @Param("tpwSvcTypId") String tpwSvcTypId,
            @Param("useYn") String useYn
    );

    /**
     * 지원 한도 내역 생성
     *
     * @param req 생성할 지원 한도 내역 리스트
     */
    void insertSprtLmt(List<SprtLmtReqVO> req);

    /**
     * 건수 한도 전체 미사용 처리
     * (서비스/서비스유형 기준으로 tpw_lmt_dvs_cd = '02' 한도 전체 use_yn = 'N' 처리)
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     */
    void updateSprtLmtUseYnAllForCount(
            @Param("tpwSvcId") String tpwSvcId,
            @Param("tpwSvcTypId") String tpwSvcTypId
    );

    /**
     * 동일 서비스에 대해 현재 사용중인 한도유형 목록 조회
     *
     * @param tpwSvcId 서비스 ID
     * @return 서비스별 사용중인 한도유형 목록
     */
    List<SprtLmtKindVO> readSvcLmtKinds(@Param("tpwSvcId") String tpwSvcId);

    /**
     * 분기 기준 한도 시작/종료년월 범위 조회
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return 분기 기준 한도 시작/종료년월 정보 리스트
     */
    List<QuarterRangeVO> readQuarterRanges(
            @Param("tpwSvcId") String tpwSvcId,
            @Param("tpwSvcTypId") String tpwSvcTypId
    );

    /**
     * 지원금한도관리번호 시퀀스 다건 조회
     *
     * @param count 생성할 관리번호 개수
     * @return 새로 발급된 관리번호 목록
     */
    List<String> readNextMngNo(@Param("count") int count);

    /**
     * 서비스유형별 거래적용여부 수정
     *
     * @param tpwSvcTypId 서비스유형 ID
     * @param adptYn      거래적용여부(Y/N)
     */
    void updateTrdNcntLtnAdptYn(
            @Param("tpwSvcTypId") String tpwSvcTypId,
            @Param("adptYn") String adptYn
    );

    /**
     * 관리번호/이전 일련번호 기준 기존 한도 미사용 처리
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @param tpwLmtDvsCd 한도구분 코드
     * @param prevSno     이전 한도 일련번호
     * @param mngNos      미사용 처리 대상 관리번호 목록
     */
    void updateSprtLmtUseYnByMngNo(
            @Param("tpwSvcId") String tpwSvcId,
            @Param("tpwSvcTypId") String tpwSvcTypId,
            @Param("tpwLmtDvsCd") String tpwLmtDvsCd,
            @Param("prevSno") String prevSno,
            @Param("mngNos") List<String> mngNos
    );

    /**
     * 서비스/유형별 활성 한도 카운트 조회
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스유형 ID
     * @return use_yn = 'Y' 인 한도의 개수
     */
    Integer readSprtLmtCntBySvcTyp(
            @Param("tpwSvcId") String tpwSvcId,
            @Param("tpwSvcTypId") String tpwSvcTypId
    );
}
