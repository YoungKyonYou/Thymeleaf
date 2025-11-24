package tmoney.co.kr.hxz.sprtpolimng.polimnginf.controller;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tmoney.co.kr.hxz.common.page.vo.PageDataVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.service.SprtLmtService;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.InstReqVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.AmtLstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst.NcntLstVO;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.sprtlmt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 지원 한도 관리(지원금 한도 + 건수 한도) 화면 컨트롤러.
 *
 * <p>관련 테이블
 * <ul>
 *     <li>tbhxzd208  : HXZ_지원금한도관리 (금액/건수 한도 이력 관리)</li>
 *     <li>tbhxzm201  : HXZ_교통복지서비스관리 (서비스 정보)</li>
 *     <li>tbhxzm202  : HXZ_교통복지서비스유형관리 (서비스 유형 정보)</li>
 * </ul>
 *
 * <p>주요 기능
 * <ul>
 *     <li>지원 한도 목록(서비스/서비스유형 기준) 조회</li>
 *     <li>금액(분기/월)·건수 한도를 통합한 3in1 모달 신규/수정</li>
 *     <li>기존 한도 존재 여부 및 분기 중복 범위 조회 (프론트 confirm 로직용)</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/sprtpolimng/polimnginf")
public class SprtLmtController {

    private final SprtLmtService sprtLmtService;

    /**
     * 지원 한도 내역 조회 화면.
     *
     * <p>서비스/서비스유형 기준으로 tbhxzd208(지원금한도관리)에 등록된 최신 한도 정보를 페이징 조회하여
     * 목록 화면에 렌더링한다.
     *
     * <pre>
     * - 검색 조건 VO : SprtLmtSrchReqVO
     * - 리스트 VO   : SprtLmtRspVO
     * - 페이징 래퍼 : PageDataVO&lt;SprtLmtRspVO&gt;
     * </pre>
     *
     * @param req   검색 조건 (서비스, 서비스유형, 한도구분 등)
     * @param model 뷰에 전달할 모델
     * @return 지원 한도 목록 화면 템플릿 경로
     */
    @GetMapping(value = "/sprtLmtPt.do")
    public String readSprtLmtPtPaging(
            @ModelAttribute SprtLmtSrchReqVO req,
            Model model
    ) {
        PageDataVO<SprtLmtRspVO> contents = sprtLmtService.readSprtLmtPtPaging(req);

        model.addAttribute("pageData", contents);
        model.addAttribute("req", req);

        return "hxz/sprtpolimng/polimnginf/sprtLmtPt";
    }

    /**
     * 지원 한도 3in1 신규 모달 오픈.
     *
     * <p>특정 서비스/서비스유형에 아직 한도(금액/건수)가 한 번도 등록되지 않은 경우,
     * 빈 상태의 3in1 모달(금액-분기 / 금액-월 / 건수 탭)을 신규 모드로 연다.
     *
     * <pre>
     * - mode  : "new-3in1"
     * - dvsCd : 기본 한도 구분 (01 = 금액)
     * - typCd : 기본 한도 타입 (02 = 분기)
     *
     * - qt    : 분기 한도 입력 리스트 (신규 시 빈 리스트)
     * - mon   : 월 한도 입력 리스트(신규 시 빈 리스트)
     * </pre>
     *
     * @param model 뷰에 전달할 모델
     * @return 지원 한도 모달 프래그먼트 템플릿 경로
     */
    @GetMapping(value = "/sprtLmtDtl/new.do")
    public String newSprtLmt(Model model) {
        // 분기/월은 신규일 때 빈 리스트로
        List<AmtReqVO> qt  = new ArrayList<>();
        List<AmtReqVO> mon = new ArrayList<>();

        model.addAttribute("mode", "new-3in1");
        model.addAttribute("dvsCd", "01");   // 기본 금액
        model.addAttribute("typCd", "02");   // 기본 분기

        model.addAttribute("qt", qt);
        model.addAttribute("mon", mon);
        model.addAttribute("amtQt", new AmtLstVO(qt));
        model.addAttribute("amtMon", new AmtLstVO(mon));

        return "hxz/sprtpolimng/polimnginf/sprtLmtPt :: amt-modal";
    }

    /**
     * 지원 한도 3in1 수정 모달 오픈.
     *
     * <p>특정 서비스/서비스유형에 이미 등록된 한도(금액/건수)를 조회하여
     * 3in1 모달(금액-분기 / 금액-월 / 건수 탭)에 채워 넣고, 수정 모드로 연다.
     *
     * <pre>
     * - mode  : "edit-3in1"
     * - dvsCd : 현재 적용 중인 한도 구분 (01=금액, 02=건수)
     * - typCd : 현재 적용 중인 한도 타입 (01=월, 02=분기/건수)
     *
     * - qt    : 분기 한도 리스트
     * - mon   : 월 한도 리스트
     * - arr   : 건수 한도 리스트
     * </pre>
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스 유형 ID
     * @param model       뷰에 전달할 모델
     * @return 지원 한도 모달 프래그먼트 템플릿 경로
     */
    @GetMapping("/sprtLmtDtl/{tpwSvcId}/{tpwSvcTypId}/edit.do")
    public String openEdit3In1(
            @PathVariable("tpwSvcId") String tpwSvcId,
            @PathVariable("tpwSvcTypId") String tpwSvcTypId,
            Model model
    ) {
        SprtLmtModalDtlVO vo = sprtLmtService.readSprtLmtByTpwSvcTypId(tpwSvcId, tpwSvcTypId);

        model.addAttribute("mode", "edit-3in1");
        model.addAttribute("tpwSvcTypId", tpwSvcTypId);
        model.addAttribute("dvsCd", vo.getDvsCd());
        model.addAttribute("typCd", vo.getTypCd());

        model.addAttribute("qt", vo.getQt());
        model.addAttribute("mon", vo.getMon());
        model.addAttribute("arr", vo.getArr());

        model.addAttribute("amtQt", new AmtLstVO(vo.getQt()));
        model.addAttribute("amtMon",  new AmtLstVO(vo.getMon()));
        model.addAttribute("ncnt", new NcntLstVO(vo.getArr(), ""));

        return "hxz/sprtpolimng/polimnginf/sprtLmtPt :: amt-modal";
    }

    /**
     * 지원 한도(금액/건수) 저장 API.
     *
     * <p>3in1 모달에서 입력한 금액/건수 한도 정보를 서버로 전송하여 저장한다.
     * 한도 구분/타입에 따라 내부에서 아래와 같이 처리한다.
     *
     * <ul>
     *     <li>금액(분기/월) 한도 : 동일 기간 내 기존 버전 use_yn='N' 처리 후 신규 버전 insert</li>
     *     <li>건수 한도        : 동일 서비스/유형의 기존 건수 한도 전체 use_yn='N' 후 신규 버전 insert</li>
     * </ul>
     *
     * <p>실제 이력 관리 및 use_yn 업데이트 로직은 {@link SprtLmtService#insertSprtLmtAmt(InstReqVO)} 에서 처리한다.
     *
     * @param req 3in1 모달에서 넘어온 요청 VO (서비스/서비스유형, 한도 구분/타입, 금액/건수 리스트 등)
     * @return 저장 성공 여부를 나타내는 JSON 응답 ({@code {"ok": true}})
     */
    @PostMapping("/sprtLmt/edit.do")
    @ResponseBody
    public ResponseEntity<?> saveSprtLmt(@RequestBody InstReqVO req) {
        sprtLmtService.insertSprtLmtAmt(req);

        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        return ResponseEntity.ok(body);
    }

    /**
     * 지원 한도 존재 여부 및 분기 중복 범위 조회 API.
     *
     * <p>프론트에서 신규/수정 모달을 띄우기 전에 호출하여,
     * 해당 서비스/서비스유형에 기존 한도가 있는지, 있다면 어떤 형태인지(금액/건수, 월/분기)를 확인한다.
     *
     * <p>반환 VO {@link SprtLmtExistResVO} 내용 예시:
     * <ul>
     *     <li>{@code exists}       : 해당 서비스/유형에 활성(use_yn='Y') 한도가 하나라도 존재하는지 여부
     *                               (금액/건수 구분 없이 전체 기준)</li>
     *     <li>{@code qtRanges}     : 기존 분기 한도 범위 리스트 (분기 중복 여부 체크용)</li>
     *     <li>{@code svcLmtDvsCd}  : 서비스 단위 한도 구분 (01=금액, 02=건수) - 단일 유형인 경우 설정</li>
     *     <li>{@code svcLmtTypCd}  : 서비스 단위 한도 타입 (01=월, 02=분기/건수) - 단일 유형인 경우 설정</li>
     *     <li>{@code multiKinds}   : 한도 유형이 둘 이상인 경우 true (프론트에서 안내 문구/처리 분기용)</li>
     * </ul>
     *
     * <p>프론트에서는 이 값을 활용하여:
     * <ul>
     *     <li>기존 한도 존재 시, 신규 등록 전에 "기존 한도를 종료하고 새로 등록할지" confirm 알림 표시</li>
     *     <li>분기 한도인 경우, {@code qtRanges}를 참고하여 중복 기간 여부를 검사</li>
     * </ul>
     *
     * @param tpwSvcId    서비스 ID
     * @param tpwSvcTypId 서비스 유형 ID
     * @return SprtLmtExistResVO를 JSON으로 감싼 응답
     */
    @GetMapping("/sprtLmtDtl/check-exist.do")
    @ResponseBody
    public ResponseEntity<?> checkExistingLimit(
            @RequestParam String tpwSvcId,
            @RequestParam String tpwSvcTypId
    ) {
        SprtLmtExistResVO res = sprtLmtService.checkExist(tpwSvcId, tpwSvcTypId);
        return ResponseEntity.ok(res);
    }
}
