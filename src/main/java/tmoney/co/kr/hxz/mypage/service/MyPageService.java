package tmoney.co.kr.hxz.mypage.service;

import tmoney.co.kr.hxz.mypage.vo.MngrVO;
import tmoney.co.kr.hxz.mypage.vo.MyPageVO;
import tmoney.co.kr.hxz.mypage.vo.PwdChangeVO;
import tmoney.co.kr.hxz.mypage.vo.UpdateMngrVO;

public interface MyPageService {
    /**
     * 관리자 마이페이지용 정보를 조회한다.
     *
     * @param mngrId 관리자 ID
     * @return 관리자 VO (없으면 null)
     */
    MngrVO readMngrMyPage(String mngrId);

    /**
     * 관리자 마이페이지 기본 정보를 수정한다.
     *
     * @param param 수정 파라미터
     * @return 수정된 행 수
     */
    int updateMngrMyPage(UpdateMngrVO param);

    /**
     * 관리자 암호화 비밀번호를 조회한다.
     *
     * @param mngrId 관리자 ID
     * @return 암호화된 비밀번호 (없으면 null)
     */
    String readMngrPwd(String mngrId);

    /**
     * 관리자 비밀번호를 변경한다.
     *
     * @param mngrId          관리자 ID
     * @param encodedPassword 암호화된 새 비밀번호
     * @param updrId          수정자 ID
     * @return 수정된 행 수
     */
    int updateMngrPwd(String mngrId, String encodedPassword, String updrId);

    // ==========================
    // 화면/비즈니스 메서드
    // ==========================

    /**
     * 마이페이지 화면에 표시할 관리자 정보를 조회한다.
     *
     * @param mngrId 현재 로그인한 관리자 ID
     * @return 화면 바인딩용 MyPageVO
     */
    MyPageVO getMyPage(String mngrId);

    /**
     * 마이페이지 기본 정보를 수정한다.
     *
     * @param mngrId   현재 로그인한 관리자 ID (수정 대상 및 updr_id)
     * @param myPageVO 화면에서 입력된 수정 정보
     */
    void updateMyPage(String mngrId, MyPageVO myPageVO);

    /**
     * 마이페이지에서 비밀번호를 변경한다.
     *
     * @param mngrId  현재 로그인한 관리자 ID
     * @param request 비밀번호 변경 요청 값(현재/새 비밀번호)
     */
    void changePassword(String mngrId, PwdChangeVO request);
}
