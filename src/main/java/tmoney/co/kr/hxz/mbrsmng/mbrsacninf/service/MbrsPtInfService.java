package tmoney.co.kr.hxz.mbrsmng.mbrsacninf.service;

import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfReqVO;
import tmoney.co.kr.hxz.mbrsmng.mbrsacninf.vo.MbrsPtInfRspVO;

import javax.validation.Valid;
import java.util.List;

/**
 * 회원정보내역 서비스
 */
public interface MbrsPtInfService {

    /**
     * 회원정보내역 페이징 조회
     */
    @Transactional(readOnly = true)
    PageDataVO<MbrsPtInfRspVO> readMbrsPtInfPaging(@Valid MbrsPtInfReqVO req, String orgCd);

    /**
     * 회원정보내역 리스트 조회
     */
    @Transactional(readOnly = true)
    List<MbrsPtInfRspVO> readMbrsPtInfList(MbrsPtInfReqVO req, String orgCd);

    /**
     * 회원정보내역 총 건수 조회
     */
    @Transactional(readOnly = true)
    long readMbrsPtInfListCnt(MbrsPtInfReqVO req, String orgCd);


}
