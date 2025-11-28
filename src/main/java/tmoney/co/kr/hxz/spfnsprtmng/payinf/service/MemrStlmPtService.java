package tmoney.co.kr.hxz.spfnsprtmng.payinf.service;

import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtReqVO;
import tmoney.co.kr.hxz.spfnsprtmng.payinf.vo.MemrStlmPtRspVO;

import javax.validation.Valid;
import java.util.List;

public interface MemrStlmPtService {

    PageDataVO<MemrStlmPtRspVO> readMemrStlmPtPaging(@Valid MemrStlmPtReqVO req, String orgCd);

    List<MemrStlmPtRspVO> readMemrStlmPtList(MemrStlmPtReqVO req, String orgCd);

    @Transactional(readOnly = true)
    long readMemrStlmPtListCnt(MemrStlmPtReqVO req, String orgCd);
    
    
    // 등록
    void saveMemrStlmPt(@Valid MemrStlmPtRspVO form);
    
    // 수정
    void updateMemrStlmPt(@Valid MemrStlmPtRspVO form);
    
    // 삭제(승인코드 요청인 것만 삭제 가능)
    void deleteMemrStlmPt(@Valid MemrStlmPtRspVO form);
}