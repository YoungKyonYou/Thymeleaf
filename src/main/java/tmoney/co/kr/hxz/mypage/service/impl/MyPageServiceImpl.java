package tmoney.co.kr.hxz.mypage.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.PasswordEncoder;
import tmoney.co.kr.hxz.mypage.mapper.MyPageMapper;
import tmoney.co.kr.hxz.mypage.service.MyPageService;
import tmoney.co.kr.hxz.mypage.vo.MngrVO;
import tmoney.co.kr.hxz.mypage.vo.MyPageVO;
import tmoney.co.kr.hxz.mypage.vo.PwdChangeVO;
import tmoney.co.kr.hxz.mypage.vo.UpdateMngrVO;

@RequiredArgsConstructor
@Service
public class MyPageServiceImpl implements MyPageService {
    private final MyPageMapper myPageMapper;
    private final PasswordEncoder passwordEncoder;
    /**
     * 관리자 마이페이지용 정보를 조회한다.
     *
     * @param mngrId 관리자 ID
     * @return 관리자 VO (없으면 null)
     */
    @Override
    public MngrVO readMngrMyPage(String mngrId) {
        return myPageMapper.readMngrMyPage(mngrId);
    }

    /**
     * 관리자 마이페이지 기본 정보를 수정한다.
     *
     * @param param 수정 파라미터
     * @return 수정된 행 수
     */
    @Override
    public int updateMngrMyPage(UpdateMngrVO param) {
        return myPageMapper.updateMngrMyPage(param);
    }

    /**
     * 관리자 암호화 비밀번호를 조회한다.
     *
     * @param mngrId 관리자 ID
     * @return 암호화된 비밀번호 (없으면 null)
     */
    @Override
    public String readMngrPwd(String mngrId) {
        return myPageMapper.readMngrPwd(mngrId);
    }

    /**
     * 관리자 비밀번호를 변경한다.
     *
     * @param mngrId          관리자 ID
     * @param encodedPassword 암호화된 새 비밀번호
     * @param updrId          수정자 ID
     * @return 수정된 행 수
     */
    @Override
    public int updateMngrPwd(String mngrId, String encodedPassword, String updrId) {
        return myPageMapper.updateMngrPwd(mngrId, encodedPassword, updrId);
    }


    // ==========================
    // 비즈니스 메서드 구현부
    // ==========================

    /**
     * 마이페이지 화면에 표시할 관리자 정보를 조회한다.
     *
     * @param mngrId 현재 로그인한 관리자 ID
     * @return 화면 바인딩용 MyPageVO
     */
    @Override
    @Transactional(readOnly = true)
    public MyPageVO getMyPage(String mngrId) {
        MngrVO mngr = readMngrMyPage(mngrId); // 이건 구현체 내 private 메서드 or 인터페이스 override

        if (mngr == null) {
            throw new IllegalStateException("관리자 정보를 찾을 수 없습니다.");
        }

        return MyPageVO.from(mngr);
    }

    /**
     * 마이페이지 기본 정보를 수정한다.
     *
     * @param mngrId   현재 로그인한 관리자 ID
     * @param myPageVO 화면에서 입력된 수정 정보
     */
    @Override
    @Transactional
    public void updateMyPage(String mngrId, MyPageVO myPageVO) {
        UpdateMngrVO param = UpdateMngrVO.from(mngrId, myPageVO);

        int updated = updateMngrMyPage(param); // ← Mapper 래핑 메서드
        if (updated != 1) {
            throw new IllegalStateException("마이페이지 정보 수정에 실패했습니다.");
        }
    }

    /**
     * 마이페이지에서 비밀번호를 변경한다.
     *
     * @param mngrId  현재 로그인한 관리자 ID
     * @param request 비밀번호 변경 요청 값(현재/새 비밀번호)
     */
    @Override
    @Transactional
    public void changePassword(String mngrId, PwdChangeVO request) {
        String currentRaw = request.getCurrentPassword();
        String newRaw = request.getNewPassword();

      /*  String currentEncoded = readMngrPwd(mngrId);
        if (currentEncoded == null ||
                !passwordEncoder.matches(currentRaw, currentEncoded)) {
            // 컨트롤러에서 메시지 매핑할 수 있도록 코드형 메시지 사용
            throw new IllegalArgumentException("CURRENT_PASSWORD_NOT_MATCH");
        }

        // 동일 비밀번호 재사용 방지
        if (passwordEncoder.matches(newRaw, currentEncoded)) {
            throw new IllegalArgumentException("SAME_AS_OLD_PASSWORD");
        }

        String encoded = passwordEncoder.encode(newRaw);
        int updated = updateMngrPwd(mngrId, encoded, mngrId);
        if (updated != 1) {
            throw new IllegalStateException("비밀번호 변경 처리 중 오류");
        }*/
    }
}