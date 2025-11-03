
(function (global) {
    'use strict'

    var MODAL_ID = 'modal-common';
    var MODAL_BOUND = false;
    var MODAL_PREV_ACTIVE = null;

    let lastActive = null;
    let lastScrollTop = 0;

    /**
     * closeModal(elOrSelector)
     * @param {Element|string} elOrSelector - 닫을 모달의 루트 .modal 요소 또는 CSS 셀렉터
     */
    function closeModal(elOrSelector) {
        if (!elOrSelector) return;

        // 인자로 Element가 오든 문자열이 오든 처리
        const modal = (
            typeof elOrSelector === 'string'
                ? document.querySelector(elOrSelector)
                : elOrSelector
        );

        if (!modal) return;

        // aria-hidden="true"
        modal.setAttribute('aria-hidden', 'true');

        // body-lock 제거
        document.body.classList.remove('body-lock');

        // 포커스 복원
        if (lastActive && typeof lastActive.focus === 'function') {
            lastActive.focus();
        }

        // 스크롤 복원
        if (typeof lastScrollTop === 'number') {
            window.scrollTo({ top: lastScrollTop });
        }
    }

    // function initDatePicker() {
    //     // 중복 초기화 방지
    //     window.__JQ_PERIOD_FIXED__ = window.__JQ_PERIOD_FIXED__ || false;
    //     if (window.__JQ_PERIOD_FIXED__) return;
    //     window.__JQ_PERIOD_FIXED__ = true;

    //     $("#sttDt, #endDt").datepicker({
    //         dateFormat: "yy-mm-dd",
    //         changeMonth: true,
    //         changeYear: true,
    //         showOn: "focus"
    //     });
    //     var sttBtn = document.getElementById("sttD").nextElementSibling;
    //     var endBtn = document.getElementById("endD").nextElementSibling;
    //     if (sttBtn) sttBtn.addEventListener("click", function () { $("#sttD").datepicker("show"); });
    //     if (endBtn) endBtn.addEventListener("click", function () { $("#endD").datepicker("show"); });
    // }
    // initDatePicker();



    /**
     * 엑셀 출력 API 호출
     *
     * @param {HTMLFormElement} searchForm 검색 폼
     * @param {HTMLButtonElement} exportBtn 엑셀 출력 버튼 객체
     * @param {string} sortValue 정렬 (sort 컬럼명, 없으면 defaultSortColumn 사용)
     * @param {string} dirValue 정렬 방향, 없으면 asc 사용
     * @param {string} defaultSortColumn 디폴트 sort 컬럼명
     * @param {object} payload (payload, requestBody에 전할 payload ex) payload = { orgcd: '', mngrid: '', dnksn: '' }
     * @returns {void}
     */
    async function bindExcelExport(searchForm, exportBtn, sortValue, dirValue, defaultSortColumn, payload) {
        // exportBtn에 dataset.provider 속성이 정의되어 있다고 가정
        if (exportBtn) {
            const provider = exportBtn.dataset.provider;

            if (!provider) { // provider가 지정되지 않았으면
                alert('\'provider\'가 지정되지 않았습니다.');
                return;
            }

            // searchForm에서 입력된 값을 수집하여 URLSearchParams 객체로 변환
            const params = new URLSearchParams(collectFromForm(searchForm));
            const applied = collectFromForm(searchForm); // searchForm에서 값 수집

            if (!applied) { // 수집된 값이 없으면
                alert('searchForm이 지정되지 않았습니다.'); // 오류 메시지 (이 메시지는 잘못된 것 같습니다. searchForm은 이미 전달됨)
                return;
            }

            // applied 객체의 모든 키-값 쌍에 대해
            Object.entries(applied).forEach(([k, v]) => {
                // 값이 null이 아니고, 문자열로 변환 후 공백 제거한 값이 빈 문자열이 아니면
                if (v != null && String(v).trim() !== '') {
                    params.set(k, v); // URLSearchParams에 설정
                }
            });

            // 정렬 관련 파라미터 설정
            params.set('sort', sortValue || defaultSortColumn); // sortValue가 없으면 defaultSortColumn 사용
            params.set('dir', dirValue || 'asc'); // dirValue가 없으면 'asc' 사용

            // Excel 내보내기 요청
            const res = await sendExcel(
                `/export/xlsx?${params.toString()}`, // 요청 URL (파라미터 포함)
                {
                    method: "POST", // POST 메서드 사용
                    data: payload    // 페이로드 데이터 전송
                }
            );

            // 응답 헤더에서 'Content-Disposition' 가져오기
            const cd = res.headers.get('Content-Disposition') || '';
            // 파일 이름 추출 정규식: UTF-8 인코딩 파일명 또는 일반 파일명
            const match = cd.match(/filename\*=UTF-8''([\w%\-\.]+)''|filename="([^"]*)"/i);
            // 파일 이름 디코딩 (match[1]이 있으면 UTF-8 디코딩, 아니면 match[2] 사용)
            const filename = match ? decodeURIComponent(match[1]) || match[2] : 'export.xlsx';

            // Blob 데이터를 이용하여 파일 다운로드
            const blob = await res.blob(); // 응답 본문을 Blob 형태로 가져오기
            const url = URL.createObjectURL(blob); // Blob URL 생성
            const a = document.createElement('a'); // 가상의 <a> 태그 생성
            a.href = url; // <a> 태그의 href에 Blob URL 설정
            a.download = filename; // 다운로드될 파일 이름 설정
            document.body.appendChild(a); // <a> 태그를 body에 추가 (클릭하기 위해)
            a.click(); // <a> 태그 클릭 (다운로드 트리거)
            a.remove(); // <a> 태그 제거
            URL.revokeObjectURL(url); // Blob URL 해제 (메모리 누수 방지)
        }
    }


    // 안전한 HTTP 요청을 보내는 비동기 함수 (send 함수를 래핑하여 에러 처리 강화)
    // url: 요청할 URL
    // method: HTTP 메서드 (기본값: 'POST')
    // data: 요청 본문 데이터 (기본값: null)
    // signal: AbortSignal (선택적)
    // headers: 추가 헤더 (선택적)
    // expect: 응답 형식 기대값 (기본값: 'json')
    // clientErrorMsg: 클라이언트 에러 메시지 (기본값: '요청에 실패했습니다.')
    // otherErrorMsg: 기타 에러 메시지 (기본값: '오류가 발생했습니다.')
    async function sendSafe(url, {method = 'POST', data = null, signal, headers, expect = 'json', clientErrorMsg = '요청에 실패했습니다.', otherErrorMsg = '오류가 발생했습니다.'}={}) {
        // send 함수 호출 결과를 out 변수에 저장
        try {
            const out = await send(url, method, data, headers, signal, expect);

            // 성공 시 결과를 그대로 반환 (ok: true, status: 200, payload: out 등)
            return { ok: true, data: out };
        } catch (e) {
            // AbortError인 경우 (요청 취소) 에러 무시하고 null 반환
            if (e.name === 'AbortError') {
                return null;
            }

            // 400, 401, 422 등의 클라이언트 에러 상태 코드인 경우
            if (e?.status >= 400 && e.status < 500) {
                // 모달 에러 메시지 생성: payload.message가 있으면 사용, 아니면 clientErrorMsg
                const msg = (e.payload && e.payload.message) ? e.payload.message : clientErrorMsg;

                modalShow({
                    title: '경고',
                    message: msg,
                    buttons: ['close']
                });

                // 에러 정보 반환
                return { ok: false, status: e.status, error: e };
            }

            // 기타 에러인 경우
            console.error(e);

            // 에러 모달 표시 (title: '오류', message: otherErrorMsg, buttons: ['닫기'])
            modalShow({
                title: '오류',
                message: otherErrorMsg,
                buttons: ['close']
            });

            // 에러 정보 반환
            return { ok: false, status: e.status, error: e };
        }
    }







    // (참고) 열 때는 이렇게 저장해주세요.
    function openModal(elOrSelector) {
        const modal =
            (typeof elOrSelector === 'string')
                ? document.querySelector(elOrSelector)
                : elOrSelector;
        if (!modal) return;

        // 현재 포커스된 요소 저장 (닫을 때 복원용)
        lastActive = document.activeElement;

        // 현재 스크롤 위치 저장 (닫을 때 복원용)
        lastScrollTop = window.pageYOffset || document.documentElement.scrollTop || 0;

        // body 스크롤 방지
        document.body.classList.add('body-lock');

        // 모달 표시
        modal.setAttribute('aria-hidden', 'false');

        // 필요하면 첫 포커스 가능한 요소로 이동
        const firstFocusable = modal.querySelector(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        if (firstFocusable) firstFocusable.focus();
    }

    function syncEmptyRow(tbodyOrSelector, opts = {}) {
        const {
            message = "조회 결과가 없습니다.",
            colspan,
            rowSelector = "tr:not([data-empty-row='true'])",
        } = opts;

        const tbody = typeof tbodyOrSelector === "string"
            ? document.querySelector(tbodyOrSelector)
            : tbodyOrSelector;
        if (!tbody) return;

        // 현재 데이터 행 개수 (placeholder 제외)
        const dataRowCount = tbody.querySelectorAll(rowSelector).length;

        // 기존 placeholder 찾기
        let emptyRow = tbody.querySelector("tr[data-empty-row='true']");

        if (dataRowCount === 0) {
            if (!emptyRow) {
                // colspan 자동 계산 (우선순위: thead → 첫 데이터행 → 첫 tr의 td/th 개수)
                const table = tbody.closest("table");
                let span = colspan;
                if (!span && table) {
                    const theadCells = table.tHead?.rows?.[0]?.cells?.length || 0;
                    if (theadCells > 0) span = theadCells;
                }
                if (!span) {
                    const firstTr = tbody.querySelector("tr");
                    span = firstTr?.cells?.length || 1;
                }

                // 빈 행 생성
                emptyRow = document.createElement("tr");
                emptyRow.setAttribute("data-empty-row", "true");

                const td = document.createElement("td");
                td.colSpan = span;
                td.className = "no-data"; // 필요 시 스타일 클래스
                td.textContent = message;

                emptyRow.appendChild(td);
                tbody.appendChild(emptyRow);
            } else {
                // 이미 있으면 문구만 최신화
                const td = emptyRow.cells[0];
                if (td && td.textContent !== message) td.textContent = message;
            }
        } else {
            // 데이터가 있으면 placeholder 제거
            if (emptyRow) emptyRow.remove();
        }
    }




    /**
     * 문자열 정규화
     * - 공백/줄바꿈을 하나로 합치고, &nbsp;를 제거하고, 소문자로 통일한다.
     * - 헤더 텍스트 비교 시 대/소문자 차이 등을 줄이기 위함.
     *
     * @param {string} text 원본 문자열
     * @returns {string} 정규화된 문자열
     */
    function norm(text) {
        return (text || "")
            .replace(/\s+/g, " ")   // 연속된 공백을 하나로
            .replace(/\u00A0/g, " ") // nbsp(공백문자) 제거
            .trim()                  // 앞뒤 공백 제거
            .toLowerCase();          // 소문자로 변환
    }

    /**
     * thead 헤더 텍스트 → DTO 키 매핑
     * @param {HTMLTableElement} table
     * @param {{ headerMap: Object<string,string>, requiredKeys: string[] }} cfg
     * @returns {Object<number,string>} 인덱스→키 매핑 객체
     */
    function buildHeaderKeyMap(table, { headerMap, requiredKeys }) {
        if (!headerMap || !Object.keys(headerMap).length) {
            throw new Error("headerMap이 필요합니다. 예) {'관리자 id':'adminId','관리자명':'adminName','역할':'roleName'}");
        }

        if (!Array.isArray(requiredKeys) || !requiredKeys.length) {
            throw new Error("requiredKeys가 필요합니다. 예) ['adminId','adminName','roleName']");
        }

        // thead 내의 모든 th 요소 가져오기
        const ths = table.querySelectorAll("thead th");
        if (!ths.length) throw new Error("테이블 헤더가 없습니다.");

        // 정규화 함수 (소문자 + 공백정리)
        const norm = s => (s || "").replace(/\s+/g, " ").replace(/\u00A0/g, " ").trim().toLowerCase();

        // th 인덱스별 DTO 키 매핑
        const indexToKey = {};
        ths.forEach((th, idx) => {
            const key = headerMap[norm(th.textContent)];
            if (key) indexToKey[idx] = key;
        });

        // 필수 키 누락 확인
        const present = new Set(Object.values(indexToKey));
        const missing = requiredKeys.filter(k => !present.has(k));
        if (missing.length) throw new Error("필수 컬럼 매핑 부족: " + missing.join(", "));

        return indexToKey;
    }




    /**
     * <tr> → DTO 변환
     * @param {HTMLTableRowElement} tr
     * @param {Object<number,string>} indexToKey
     * @returns {Object<string,string>}
     */
    function rowToObject(tr, indexToKey) {
        const obj = {};
        tr.querySelectorAll("td").forEach((td, i) => {
            const key = indexToKey[i];
            if (key) obj[key] = (td.textContent || "").trim();
        });
        return obj;
    }

    /**
     * 섹션 행 수집 (div[data-section] 기준)
     * - filter: 'selected' | 'unselected' | 'all' (기본 'all'로 요청하신 대로 변경)
     *
     * @param {Element|string} modalBodyOrSelector  .modal-body 엘리먼트 또는 셀렉터
     * @param {string} sectionKey                   data-section 값 ('list' 등)
     * @param {{ headerMap?: Object, requiredKeys?: string[], filter?: ('selected'|'unselected'|'all') }} param2
     * @returns {Array<Object<string,string>>}
     */
    function collectSectionRows(
        modalBodyOrSelector,
        sectionKey,
        { headerMap = {}, requiredKeys = [], filter = 'all' } = {}
    ) {
        const root = (typeof modalBodyOrSelector === "string")
            ? document.querySelector(modalBodyOrSelector)
            : modalBodyOrSelector;
        if (!root) throw new Error("modalBody를 찾을 수 없습니다.");

        const sectionRoot = root.querySelector('[data-section="' + sectionKey + '"]');
        if (!sectionRoot) throw new Error("섹션을 찾을 수 없습니다: " + sectionKey);

        const table = sectionRoot.querySelector("table");
        if (!table) throw new Error("섹션 테이블을 찾을 수 없습니다: " + sectionKey);

        const indexToKey = buildHeaderKeyMap(table, { headerMap, requiredKeys });

        // 필터 적용
        let rowSelector = "tbody tr";
        if (filter === "selected") rowSelector = "tbody tr.is-selected";
        else if (filter === "unselected") rowSelector = "tbody tr:not(.is-selected)";

        const rows = table.querySelectorAll(rowSelector);
        const out = [];
        rows.forEach(tr => out.push(rowToObject(tr, indexToKey)));
        return out;
    }

    /**
     * tbody의 체크 상태로 헤더(전체선택) 체크박스를 상태 동기화
     * - 모두 체크: checked=true, indeterminate=false
     * - 일부 체크: checked=false, indeterminate=true
     * - 모두 해제: checked=false, indeterminate=false
     *
     * @param {HTMLTableSectionElement} tbody
     * @returns {void}
     */
    function syncHeaderCheckBox(tbody) {
        const table = tbody.closest('table');
        const headCb = table?.tHead?.querySelector('input[type="checkbox"]')
            || table?.querySelector('thead input[type="checkbox"]');
        if (!headCb) return;

        const cbs = tbody.querySelectorAll('input[type="checkbox"]');
        if (!cbs.length) {
            headCb.checked = false;
            headCb.indeterminate = false;
            return;
        }

        let checked = 0;
        cbs.forEach(cb => {
            if (cb.checked) checked++;
        });

        if (checked === 0) {
            headCb.checked = false;
            headCb.indeterminate = false;
        } else if (checked === cbs.length) {
            headCb.checked = true;
            headCb.indeterminate = false;
        } else {
            headCb.checked = false;
            headCb.indeterminate = true;
        }
    }

    /**
     * 두 섹션(예: list / added)의 결과를 서버 DTO 구조로 합친다.
     *
     * @param {Array<Object<string,string>>} listRows  좌측 섹션에서 수집한 DTO 배열
     * @param {Array<Object<string,string>>} addedRows 우측 섹션에서 수집한 DTO 배열
     * @param {{list?: string, added?: string}} keys   키 이름 매핑(기본 { list:'list', added:'added' })
     * @returns {{ [key: string]: Array<Object<string,string>> }} 병합된 페이로드 (예: { list:[...], added:[...] })
     */
    function mergeAs(listRows, addedRows, keys) {
        const listKey = (keys && keys.list) ? keys.list : "list";
        const addedKey = (keys && keys.added) ? keys.added : "added";
        return {
            [listKey]: Array.isArray(listRows) ? listRows : [],
            [addedKey]: Array.isArray(addedRows) ? addedRows : []
        };
    }

    // ** check box param 수집 end ** //

    /**
     * 섹션(table/thead/tbody/헤더체크박스) 요소를 얻기
     * @param {Element|string} root .modal-body 엘리먼트 또는 셀렉터
     * @param {string} sectionKey data-section 값 ('list' | 'added' 등)
     * @returns {{table:HTMLTableElement, thead:HTMLTableSectionElement|null, tbody:HTMLTableSectionElement, header:HTMLInputElement|null}}
     * @throws {Error} 섹션/테이블/tbody 미존재 시
     */
    function getSectionParts(root, sectionKey) {
        const modalBody = (typeof root === 'string') ? document.querySelector(root) : root;
        if (!modalBody) throw new Error('modal-body를 찾을 수 없습니다.');

        const section = modalBody.querySelector(`div[data-section="${sectionKey}"]`);
        if (!section) throw new Error(`섹션을 찾을 수 없습니다: ${sectionKey}`);

        const table = section.querySelector('table');
        if (!table) throw new Error(`섹션 테이블을 찾을 수 없습니다: ${sectionKey}`);

        const thead = table.tHead || table.querySelector('thead');
        const tbody = table.tBodies?.[0] || table.querySelector('tbody');
        if (!tbody) throw new Error(`섹션 tbody를 찾을 수 없습니다: ${sectionKey}`);

        const header = thead ? thead.querySelector('input[type="checkbox"]') : null;
        return { table, thead, tbody, header };
    }

    /**
     * 선택된(tr.is-selected) 행을 다른 섹션으로 이동
     * - 이동 중 is-selected 제거
     * - 옵션: 개별 체크박스 해제, 헤더 선택 해제, 헤더 상태 동기화
     *
     * @param {Element|string} modalBodyOrSelector .modal-body 엘리먼트 또는 셀렉터
     * @param {string} fromKey 출발 섹션 data-section 값
     * @param {string} toKey 도착 섹션 data-section 값
     * @param {Object} [opts]
     * @param {boolean} [opts.resetCheckbox=true] 이동 행의 체크박스도 해제
     * @param {'from'|'to'|'both'|boolean} [opts.clearSelectAll='both'] 헤더 전체선택 강제 해제
     * @param {boolean} [opts.syncHeader=true] 이동 후 헤더 상태 재계산
     * @returns {number} 이동한 행 개수
     */
    function moveSelectedRows(
        modalBodyOrSelector,
        fromKey,
        toKey,
        { resetCheckbox = true, clearSelectAll = 'both', syncHeader = true } = {}
    ) {
        const { tbody: fromTbody, header: fromHeader } = getSectionParts(modalBodyOrSelector, fromKey);
        const { tbody: toTbody, header: toHeader } = getSectionParts(modalBodyOrSelector, toKey);

        const selected = Array.from(fromTbody.querySelectorAll('tr.is-selected'));
        if (!selected.length) return 0;

        selected.forEach(tr => {
            tr.classList.remove('is-selected');
            if (resetCheckbox) {
                tr.querySelectorAll('input[type="checkbox"]').forEach(cb => cb.checked = false);
            }
            toTbody.appendChild(tr);
        });

        // 헤더 전체선택 강제 해제
        if (clearSelectAll === true || clearSelectAll === 'both') {
            if (fromHeader) { fromHeader.checked = false; fromHeader.indeterminate = false; }
            if (toHeader) { toHeader.checked = false; toHeader.indeterminate = false; }
        } else if (clearSelectAll === 'from') {
            if (fromHeader) { fromHeader.checked = false; fromHeader.indeterminate = false; }
        } else if (clearSelectAll === 'to') {
            if (toHeader) { toHeader.checked = false; toHeader.indeterminate = false; }
        }

        // 헤더 상태 동기화 (선택된 행이 변경되었으므로)
        if (syncHeader) {
            syncHeaderCheckbox(fromTbody);
            syncHeaderCheckbox(toTbody);
        }

        return selected.length;
    }

    function getModal() {
        var m = document.getElementById(MODAL_ID);
        if (!m) throw new Error('#' + MODAL_ID + ' not found in layout');
        return m;
    }

    function modalBindOnce() {
        if (MODAL_BOUND) return;

        // ESC로 닫기
        document.addEventListener('keydown', function (e) {
            var m = getModal();
            if (m.getAttribute('aria-hidden') === 'true') return; // 모달이 숨겨져 있으면 아무것도 하지 않음
            if (e.key === 'Escape') modalHide(); // ESC 키를 누르면 모달 숨기기
        });

        // data-dismiss(오버레이/닫기/취소 등) 클릭 시 닫기
        document.addEventListener('click', function (e) {
            var m = getModal();
            if (m.getAttribute('aria-hidden') === 'true') return; // 모달이 숨겨져 있으면 아무것도 하지 않음
            var t = e.target.closest('[data-dismiss]'); // 클릭된 요소 또는 가장 가까운 조상 중 data-dismiss 속성을 가진 요소 찾기
            if (!t) return; // data-dismiss 요소가 없으면 아무것도 하지 않음
            e.preventDefault(); // 기본 이벤트 방지 (예: 링크 클릭 시 페이지 이동)
            modalHide(); // 모달 숨기기
        });

        MODAL_BOUND = true; // 모달 이벤트 바인딩이 완료되었음을 표시
    }

    // 내부: 버튼 핸들러 추적/해제
    function rememberHandler(modal, el, type, fn) {
        modal._handlers = modal._handlers || []; // 모달에 핸들러 목록이 없으면 새로 생성
        modal._handlers.push({ el: el, type: type, fn: fn }); // 핸들러 정보 (요소, 이벤트 타입, 함수)를 목록에 추가
    }

    function cleanupButtonHandlers(modal) {
        if (!modal._handlers) return; // 등록된 핸들러가 없으면 아무것도 하지 않음

        for (var i = 0; i < modal._handlers.length; i++) {
            var h = modal._handlers[i];
            // 요소가 존재하고 removeEventListener 메서드를 가지고 있으면 이벤트 리스너 제거
            if (h.el && h.el.removeEventListener) h.el.removeEventListener(h.type, h.fn);
        }
        modal._handlers = []; // 핸들러 목록 초기화
    }


    /*
     * 개체로만 받는 API
     * @param {Object} param0
     * @param {string} param0.message 본문 메시지(필수)
     * @param {string} param0.title 타이틀(없으면 '알림')
     * @param {string} [param0.buttons='close'] 버튼 구성 ('close'이면 버튼이 ok-close일 때만 유효)
     * @param {Function} [param0.onOk] 확인 클릭 시 콜백 (버튼이 ok-close일 때만 유효)
     * @param {Function} [param0.onClose] 닫힘 (ESC/오버레이/취소/닫기) 포함 콜백
     * @param {string} [param0.okText='확인'] 확인 라벨 ('확인')
     * @param {string} [param0.closeText='닫기'] 닫기/취소 라벨 ('닫기')
     */
    function modalShow({ message, title = '알림', buttons = 'close', onOk, onClose, okText = '확인', closeText = '닫기' }) {
        if (message == null || message === '') return;

        var m = getModal();
        modalBindOnce();

        // 타이틀/본문
        var titleEl = m.querySelector('#modal-title-basic');
        var bodyEl = m.querySelector('.modal-body');
        if (titleEl) titleEl.textContent = title;
        if (bodyEl) bodyEl.textContent = String(message);

        // 버튼 요소
        var btnCancel = m.querySelector('.modal-footer .btn-dark-line'); // 취소 (회색)
        var btnOk = m.querySelector('.modal-footer .btn-blue');       // 확인 (파란)

        // 이전 버튼 정리
        cleanupButtonHandlers(m);

        // 버튼 구성
        if (buttons === 'close') {
            // 닫기 버튼만 있을 때
            if (btnCancel) {
                btnCancel.style.display = 'none'; // 취소 버튼 숨기기
            }
            if (btnOk) {
                btnOk.textContent = closeText; // 확인 버튼 텍스트를 닫기 텍스트로 변경
                btnOk.setAttribute('data-dismiss', ''); // data-dismiss 속성 추가 (모달 닫기 기능)
                btnOk.style.display = ''; // 확인 버튼 표시
            }
        } else { // 'ok-close' 버튼 구성일 때
            if (btnCancel) {
                btnCancel.textContent = closeText; // 취소 버튼 텍스트를 닫기 텍스트로 변경
                btnCancel.setAttribute('data-dismiss', ''); // data-dismiss 속성 추가
                btnCancel.style.display = ''; // 취소 버튼 표시
            }
            if (btnOk) {
                btnOk.textContent = okText; // 확인 버튼 텍스트를 확인 텍스트로 변경
                btnOk.removeAttribute('data-dismiss'); // data-dismiss 속성 제거 (닫기 기능 없음)
                btnOk.style.display = ''; // 확인 버튼 표시
            }
        }
        m.setAttribute('aria-hidden', 'false'); // 모달이 현재 보이는 상태임을 접근성 도구에 알림
        m.classList.add('is-open');            // CSS에서 display 처리 (모달을 화면에 표시)
        document.body.classList.add('modal-open'); // body에 클래스를 추가하여 스크롤 비활성화 등 처리

        // 포커스
        MODAL_PREV_ACTIVE = document.activeElement; // 모달이 열리기 전 활성화되어 있던 요소를 저장
        var container = m.querySelector('.modal-container'); // 모달 컨테이너 요소 찾기
        (container || m).focus(); // 모달 컨테이너에 포커스 설정 (없으면 모달 자체에 포커스)

        // 확인 버튼 핸들러
        if (buttons === 'ok-close' && btnOk) { // 버튼 구성이 'ok-close'이고 확인 버튼이 있으면
            var okHandler = function () {
                try {
                    onOk && onOk(); // onOk 콜백 함수가 존재하면 실행
                } finally {
                    modalHide(); // onOk 실행 후 모달 숨기기 (오류 발생 여부와 상관없이)
                }
            };
            btnOk.addEventListener('click', okHandler, { once: true }); // 확인 버튼 클릭 시 okHandler 한 번만 실행
            rememberHandler(m, btnOk, 'click', okHandler); // 핸들러를 추적하여 나중에 제거할 수 있도록 저장
        }

        // 닫힘 콜백 등록 (ESC/오버레이/닫기/취소 포함)
        m._onClose = (typeof onClose === 'function') ? onClose : null; // onClose 콜백 함수가 함수 타입이면 등록, 아니면 null
        // 이 닫는 중괄호는 이전 `modalShow` 함수의 끝을 나타냅니다.

        // TODO: 여기에 이벤트 리스너 추가 로직이 필요해 보입니다.
        // 현재 코드는 버튼만 구성하고 이벤트는 연결하지 않습니다.
    }// 표시 + 접근성
    function modalHide() {
        var m = getModal(); // 모달 요소 가져오기

        m.setAttribute('aria-hidden', 'true'); // 모달이 숨겨진 상태임을 접근성 도구에 알림
        m.classList.remove('is-open');        // 모달을 화면에서 숨기기 위한 CSS 클래스 제거
        document.body.classList.remove('modal-open'); // body에서 클래스를 제거하여 스크롤 활성화 등 처리

        // 버튼 핸들러 해제
        cleanupButtonHandlers(m); // 모달과 관련된 모든 이벤트 핸들러 제거

        // 닫힘 콜백
        if (typeof m._onClose === 'function') { // 닫힘 콜백 함수가 등록되어 있으면
            try { m._onClose(); } // 콜백 함수 실행
            finally { m._onClose = null; } // 콜백 실행 후 콜백 참조를 제거하여 한 번만 실행되도록 함
        }

        // 포커스 복귀
        // 모달이 열리기 전 활성화되어 있던 요소가 존재하고 focus 메서드를 가지고 있으면
        if (MODAL_PREV_ACTIVE && typeof MODAL_PREV_ACTIVE.focus === 'function') {
            MODAL_PREV_ACTIVE.focus(); // 이전 활성화 요소로 포커스 복귀
        }
        MODAL_PREV_ACTIVE = null; // 이전 활성화 요소 참조 초기화
    }





    /**
     * 요청을 위한 baseUrl + queryParams를 붙이는 함수
     * @param {string} [base] 기본 요청 url
     * @param {Query Param 정보} params
     * @returns queryParams가 합쳐진 url 반환
     */
    const buildUrl = (base, params = {}) => {
        const u = new URL(base, window.location.origin); // base URL과 현재 도메인을 이용하여 URL 객체 생성
        Object.entries(params).forEach(([k, v]) => { // params 객체의 각 키-값 쌍에 대해
            // 값이 '??'이 아니면 (유효한 값이면) 쿼리 파라미터로 설정
            if (v !== '??') {
                u.searchParams.set(k, v);
            }
        });
        // 'Date'라는 이름의 쿼리 파라미터를 현재 시간으로 설정 (캐시 방지 목적)
        u.searchParams.set('_', Date.now());
        return u.toString(); // 완성된 URL 문자열 반환
    };

    /**
     * 문자열 상태로는 단순 텍스트이지만
     * 기존 페이지에 삽입하거나 교체할 수 있게 html 형태로 변환
     * @param {html 형태의 String} html
     * @returns string 형태를 html 형태로 변환
     */
    const parseHtml = (html) => new DOMParser().parseFromString(html, 'text/html'); // HTML 문자열을 DOM Document 객체로 파싱
    /**
        * 기존 태그를 새로운 데이터가 들어간 태그로 교체
        * @param {html} html
        * @param {교체할 태그를 querySelector로 가져온 값} selector
        * @returns
        */
    const swap = (html, selector) => {
        if (!html) return; // html이 없으면 아무것도 하지 않음

        const doc = parseHtml(html); // HTML 문자열을 Document 객체로 파싱
        const next = doc.querySelector(selector); // 파싱된 문서에서 교체할 새 요소 찾기
        const curr = document.querySelector(selector); // 현재 문서에서 교체될 기존 요소 찾기

        // 새 요소와 기존 요소가 모두 존재하면 기존 요소를 새 요소로 교체
        if (next && curr) curr.replaceWith(next);
    };



    /**
     * url로 요청해서 받은 html을 반환
     * @param {string} url 요청 url (queryParams이 다 들어간 url이어야 함) url
     * @param {추가 옵션을 위한 파라미터} opt
     * @returns url로 요청해서 받은 html을 반환
     */
    const fetchHtml = async (url, opt = {}) => {
        const res = await fetch(url, {
            headers: { 'Accept': 'text/html, application/json;q=0.9' }, // HTML을 우선적으로, JSON도 허용
            cache: 'no-store', // 캐시 사용 안 함
            ...opt // 추가 옵션 병합
        });

        const ct = res.headers.get('content-type') || ''; // Content-Type 헤더 가져오기
        let bodyText = await res.text(); // 응답 본문을 텍스트로 읽기

        if (!res.ok) { // 응답 상태 코드가 200번대가 아니면
            let payload = null;
            if (ct.includes('application/json')) { // Content-Type이 JSON이면
                try { payload = JSON.parse(bodyText); } catch (e) { /* JSON 파싱 실패 무시 */ }
            }
            // 오류 메시지 구성 (payload.message가 있으면 사용, 없으면 HTTP 상태 메시지 사용)
            const message = (payload && payload.message) ? payload.message : `HTTP ${res.status}`;
            const err = new Error(message); // 새로운 Error 객체 생성
            err.name = 'FetchHtmlError'; // 오류 이름 설정
            err.status = res.status; // HTTP 상태 코드 저장
            err.body = bodyText; // 응답 본문 저장
            err.payload = payload; // 파싱된 JSON 페이로드 저장
            err.contentType = ct; // Content-Type 저장
            throw err; // 오류 발생
        }
        return bodyText; // 성공하면 응답 본문 텍스트 반환
    };




    /**
     * 현재 활성 페이지 링그가 없으면 0 반환
     * @returns 현재 활성 페이지 링크가 없으면 0 반환
     */
    const activePage = () => {
        // data-page 기반 공통 페이지에서는 의미가 없지만, 하위호환으로 유지
        const a = document.querySelector('#grid-pager a.grid-pager[aria-current="page"]'); // 현재 활성화된 페이지 링크 요소 찾기
        if (!a) return 0; // 링크가 없으면 0 반환

        const u = new URL(a.getAttribute('href') || '', window.location.origin); // 링크의 href 속성을 이용하여 URL 객체 생성
        // 'page' 쿼리 파라미터 값을 정수로 파싱하여 반환, 없거나 파싱 실패 시 0 반환
        return parseInt(u.searchParams.get('page') || '0', 10) || 0;
    };

    /**
     * 폼에서 현재 입력된 값을 읽음
     * @param {form 객체} selectorForm
     * @returns 폼에 입력된 값을 읽어서 Map 형태 반환
     */
    function collectFromForm(selectorForm) {
        const params = {}; // 결과를 저장할 객체
        if (selectorForm) {
            const fd = new FormData(selectorForm); // 폼 요소를 기반으로 FormData 객체 생성
            for (const [k, v] of fd.entries()) { // FormData의 모든 엔트리(키-값 쌍)를 반복
                // 값이 null이 아니고, 문자열로 변환 후 공백 제거한 값이 빈 문자열이 아니면 params에 추가
                if (v != null && String(v).trim() !== '') params[k] = v;
            }
        }
        return params; // 수집된 파라미터 객체 반환
    }

    function collectAsJson(form) {
        const fd = new FormData(form); // 폼 요소를 기반으로 FormData 객체 생성
        const payload = { list: [] }; // 결과를 저장할 초기 페이로드 객체 (list 배열 포함)

        // name이 list[0].field 형태일 때 파싱
        const re = /^list\[(\d+)\].(.+)$/; // 'list[숫자].필드명' 패턴을 위한 정규 표현식
        for (const [name, value] of fd.entries()) { // FormData의 모든 엔트리를 반복
            const m = name.match(re); // 이름이 정규 표현식 패턴과 일치하는지 확인
            if (!m) continue; // 일치하지 않으면 건너뛰기
            // TODO: 필요하다면 다른 name도 처리 로직 추가

            const idx = Number(m[1]); // 인덱스 추출 (문자열을 숫자로 변환)
            const field = m[2]; // 필드명 추출

            if (!payload.list[idx]) { // 해당 인덱스의 리스트 항목이 없으면 빈 객체로 초기화
                payload.list[idx] = {};
            }
            payload.list[idx][field] = value; // 해당 인덱스의 객체에 필드와 값을 할당
        }
        return payload; // JSON 형태로 구성된 페이로드 객체 반환
    }





    // 이전 이미지에서 `colle

    let listAbort; // 리스트 요청 중단을 위한 변수 (Aborted Request 처리)

    // 폼에서 현재 입력된 값을 수집
    let applied = collectFromForm();

    /**
     * @param {기본 요청 url} base
     * @param {요청에 필요한 추가 옵션} overrides
     * @param {pagination할 때 size} selectSize
     * @param {pagination할 때 정렬 기준} inputSort
     * @param {pagination할 때 정렬 방향} inputDir
     * @param {디폴트 기본 정렬명 또는 정렬 칼럼} defaultSortColumn
     * @returns pagination search를 하기 위해 api에 파라미터로 넘길 params 반환
     */
    const withSortAndSize = (base, overrides = {}, selectSize, inputSort, inputDir, defaultSortColumn) => ({
        ...base, // 기본 URL 파라미터 (spread operator로 복사)
        size: parseInt(selectSize?.value || '10', 10), // selectSize 요소의 값을 숫자로 파싱, 없으면 기본값 '10' 사용
        sort: inputSort?.value || defaultSortColumn, // inputSort 요소의 값 사용, 없으면 defaultSortColumn 사용
        dir: inputDir?.value || 'asc', // inputDir 요소의 값 사용, 없으면 기본값 'asc' (오름차순) 사용
        page: 0, // 페이지 번호는 기본적으로 0으로 설정
        ...overrides // 전달된 overrides를 병합 (기존 값들을 덮어쓸 수 있음)
    });

    /**
     * @param {기본 요청 url} base
     * @param {요청에 필요한 추가 옵션} overrides
     * @param {pagination할 때 size} selectSize
     * @param {pagination할 때 정렬 기준} inputSort
     * @param {pagination할 때 정렬 방향} inputDir
     * @param {pagination 요청을 보내기 위한 파라미터 집합 - collectFromForm 함수로 리턴받은 값을 넘기면 됨} applied
     * @returns API 요청하기 위한 파라미터 반환
     */
    const state = ({ overrides = {}, selectSize, inputSort, inputDir, defaultSortColumn, applied } = {}) => ({
        // applied 객체에 overrides, selectSize 등의 pagination 관련 정보를 병합하여 반환
        // 이 함수는 withSortAndSize를 호출하여 최종 상태 객체를 구성합니다.
        ...withSortAndSize(applied, overrides, selectSize, inputSort, inputDir, defaultSortColumn),
    });
    /**
     * * 현재 페이지를 다시 로드하는 비동기 함수
     *
     * @param {현재 페이지} page
     * @param {디폴트 sort 컬럼} defaultSortColumn
     * @param {요청된 API 응답을 적용하기 위한 교체할 태그 목록} swapTargets ['#grid-tbody', '#grid-pager']
     * @param {기본 요청 url} baseUrl
     * @param {pagination (페이지네이션) size} selectSize
     * @param {pagination 대상 컬럼} inputSort
     * @param {sort 방향. 일반적으로 asc 사용} inputDir
     * @param {pagination 값을 내기 위해 collectFromForm 함수로 리턴받은 값을 넘기면 됨} applied
     * @returns swapTargets에 해당하는 태그들을 요청한 API 결과값으로 적용
     */
    async function reloadList({ page, defaultSortColumn, swapTargets = ['#grid-tbody', '#grid-pager'], baseUrl, selectSize, inputSort, inputDir, applied }) {
        listAbort?.abort(); // 이전 요청이 있으면 중단
        listAbort = new AbortController(); // 새로운 AbortController 생성
        const signal = listAbort.signal; // 중단 신호 가져오기

        // 현재 페이지와 pagination 관련 파라미터를 포함한 상태 객체 생성
        const params = state({
            overrides: { page: typeof page === 'number' ? page : activePage() }, // 페이지가 숫자면 해당 페이지, 아니면 현재 활성 페이지 사용
            selectSize, inputSort, inputDir, defaultSortColumn, applied
        });

        try {
            // buildUrl로 URL을 생성하고 fetchHtml로 해당 URL의 HTML을 비동기로 가져옴
            const html = await fetchHtml(buildUrl(baseUrl, params), { signal }); // 중단 신호 전달
            for (const sel of swapTargets) { // swapTargets 배열의 각 셀렉터에 대해
                swap(html, sel); // 가져온 HTML에서 해당 셀렉터의 요소를 찾아 현재 문서의 요소와 교체
            }

            bindPagination(baseUrl, {
                page: 0,
                size: parseInt(selectSize?.value || '10', 10),
                dir: inputDir?.value || 'asc',
                sort: inputSort?.value || defaultSortColumn
            }); // 페이지네이션 바인딩 (함수 구현 필요)

            if (selectSize) selectSize.value = params.size;
            if (inputDir) inputDir.value = params.dir;
            if (inputSort) inputSort.value = params.sort;

        } catch (e) {
            if (e.name === "AbortError") return; // 요청이 중단되었으면 아무것도 하지 않음

            // HTTP 상태 코드가 400, 422, 409인 경우 (일반적인 오류 응답)
           if (e?.status >= 400 && e.status < 500) {
                // payload.message가 있으면 해당 메시지를 사용하고, 없으면 기본 메시지
                const msg = e.payload.message || '요청을 처리할 수 없습니다.';
                Common.modalShow({
                    message: msg,
                    title: '알림',
                    buttons: 'close'
                });
                return;
            }
            // 그 외의 모든 오류에 대해 일반적인 오류 메시지 출력
            alert('목록을 불러오지 못했습니다.');
            console.error(e);
        }
    }





    /**
     * @param {데이터} data: Files | FileList | File[]
     * @param {확장자 허용 목록 (예: 'jpg', 'png', 'pdf')} extensions: * 없으면, 대소문자 무시
     * @param {검증 실패 시 null} form: FormData | null
     *
     * @서비스(@RequestPart):
     * @RequestPart("data") JSON
     * @RequestPart("file") MultipartFile (단일)
     *
     * toMultiPart에 데이터, extensions = []를 넘겨줍니다.
     *
     * @param {data} data multipart 데이터를 구성할 파일 또는 파일 목록
     * @param {string[]} extensions 허용되는 파일 확장자 목록 (선택 사항)
     * @returns {FormData|null} 유효한 FormData 객체 또는 검증 실패 시 null
     */
    const toMultiPart = (data, extensions = []) => {
        // --- 상수 ---
        const MAX_SIZE = 20 * 1024 * 1024; // 20MB
        const DISALLOW_EMPTY = true; // 빈 파일 방지

        // --- 유틸 ---
        // 허용되는 확장자를 소문자로 변환하고 정규식에 사용할 수 있도록 이스케이프 처리
        const normExts = (extensions || []).map(e => String(e).replace(/(\.)/g, '\\.').toLowerCase());
        const allowedLabel = normExts.length ? '.' + normExts.join(', .') : '';

        // files가 유효한 FileList 또는 File[]인지 확인하는 함수
        function isInstanceofFiles(files) {
            // files가 FileList의 인스턴스이거나 배열이고 모든 요소가 File의 인스턴스인지 확인
            return files instanceof FileList || (Array.isArray(files) && files.every(f => f instanceof File));
        }

        // files에서 유효한 단일 파일을 반환하는 함수
        function pickOneFile(files) {
            if (!files) return null; // files가 없으면 null 반환
            if (files instanceof File) return files; // files가 단일 File 객체이면 바로 반환

            if (isInstanceofFiles(files)) {
                if (files.length === 0 && DISALLOW_EMPTY) return '__TOO_MANY__'; // 빈 파일이고 허용되지 않으면 특정 문자열 반환
                return files.item(0); // 첫 번째 파일 반환 (FileList의 경우 item(0) 사용)
            }

            const onlyFiles = files.filter(f => f instanceof File); // File 객체만 필터링
            if (onlyFiles.length === 0 && DISALLOW_EMPTY) return '__TOO_MANY__'; // 필터링 후 빈 파일이고 허용되지 않으면 특정 문자열 반환
            return onlyFiles[0]; // 필터링된 첫 번째 파일 반환

            return null; // 그 외의 경우 null 반환
        }

        // 메시지를 모달로 보여주는 함수
        function show(msg) {
            Common.modalShow({ message: msg, buttons: 'close' });
        }

        // --- 입력값 정규화 ---
        const src = data || []; // data가 없으면 빈 배열로 초기화
        const fileSel = pickOneFile(src); // data에서 단일 파일 선택 (또는 에러 문자열)

        if (fileSel === '__TOO_MANY__') {
            show(`파일은 1개만 업로드할 수 없습니다.`);
            return null;
        }

        if (!fileSel) { // 선택된 파일이 없으면
            show(`업로드할 파일이 없습니다.`);
            return null;
        }

        // TODO: 여기에 실제 FormData 구성 및 파일 유효성 검사 로직이 추가되어야 합니다.
        // 현재 코드는 파일 선택 및 초기 검증까지만 수행합니다.
        // 예: 파일 크기, 확장자 검증 후 FormData에 추가하는 로직

        // --- 검증 ---
        // 파일 이름 (없으면 'unnamed'로 기본값 설정)
        const name = fileSel.name || 'unnamed';
        // 파일 크기 (숫자 타입이 아니면 0으로 설정)
        const size = typeof fileSel.size === 'number' ? fileSel.size : 0;
        // 파일 확장자 (이름에 '.'이 있으면 마지막 점 이후 부분을 소문자로 변환, 없으면 빈 문자열)
        const ext = name.includes('.') ? name.split('.').pop().toLowerCase() : '';

        // 파일이 비어 있고 비어 있는 파일이 허용되지 않는 경우
        if (DISALLOW_EMPTY && size === 0) {
            show(`파일 "${name}"은(는) 0바이트로 업로드할 수 없습니다.`);
            return null;
        }

        // 파일 크기가 최대 허용 크기(MAX_SIZE)를 초과하는 경우
        if (size > MAX_SIZE) {
            // fmtBytes 함수는 정의되지 않았지만, 파일 크기를 보기 좋게 포맷하는 함수로 가정
            show(`파일 "${name}"의 용량이 20MB를 초과했습니다. (현재: ${fmtBytes(size)})`);
            return null;
        }

        // 허용되는 확장자 목록이 있고, 현재 파일의 확장자가 목록에 포함되지 않는 경우
        if (normExts.length && !normExts.includes(ext)) {
            show(`허용되지 않은 파일 형식입니다.\n허용 확장자: ${allowedLabel}\n파일: ${name}`);
            return null;
        }

        // --- FormData 구성 ---
        // src 객체에서 files 속성을 제외한 모든 속성을 meta 객체로 복사
        const meta = { ...src };
        delete meta.files; // files 속성 제거

        const fd = new FormData(); // 새로운 FormData 객체 생성
        // 'data' 필드에 meta 객체를 JSON 문자열로 변환하여 Blob 형태로 추가
        fd.append('data', new Blob([JSON.stringify(meta)], { type: 'application/json' }));
        // 'file' 필드에 선택된 파일(fileSel)과 파일 이름(name)을 추가
        fd.append('file', fileSel, name);

        return fd; // 구성된 FormData 객체 반환
        ; // 이 닫는 중괄호는 `toMultiPart` 함수의 끝을 나타냅니다.
    }


    // 바이트를 사람이 읽기 쉬운 문자열로 변환
    function fmtBytes(bytes) {
        // bytes가 유한한 숫자가 아니면 (NaN, Infinity 등) 그대로 문자열로 변환하여 반환
        if (!Number.isFinite(bytes)) return String(bytes);

        // 단위 배열: 바이트(B), 킬로바이트(KB), 메가바이트(MB), 기가바이트(GB), 테라바이트(TB)
        const u = ['B', 'KB', 'MB', 'GB', 'TB'];
        let i = 0, n = bytes; // i는 단위 인덱스, n은 현재 바이트 값

        // n이 1024 이상이고, 단위 배열의 마지막 인덱스(TB)에 도달하기 전까지 반복
        while (n >= 1024 && i < u.length - 1) {
            n /= 1024; // n을 1024로 나누고 (단위 변경)
            i++; // 단위 인덱스 증가
        }

        // 최종 포맷된 문자열 반환
        // n이 100 이상이면 소수점 없이, 10 이상 100 미만이면 소수점 한 자리, 10 미만이면 소수점 두 자리로 고정
        return `${n.toFixed(n >= 100 ? 0 : n >= 10 ? 1 : 2)} ${u[i]}`;
    }









    // Excel 데이터를 전송하는 비동기 함수
    async function sendExcel(url, { method = 'POST', data = null, headers = {}, signal, expect = 'json' } = {}) {
        const init = {
            method, // HTTP 메서드 (기본값 'POST')
            // 가능한 application/json으로 주고 text/plain 그리고 그 외 아무거나
            headers: {
                'Accept': 'application/json, text/plain;q=0.9', // JSON을 선호하지만 text/plain도 허용
                ...headers // 추가 헤더 병합
            },
            cache: 'no-store', // 캐시 사용 안 함
            credentials: 'same-origin', // 동일 출처 자격 증명 사용 (쿠키 등)
            signal // AbortController의 signal 전달 (요청 취소용)
        };

        if (data !== null) { // 데이터가 있는 경우
            if (data instanceof FormData) { // 데이터가 FormData 인스턴스인 경우
                init.body = data; // body에 FormData 직접 할당 (Content-Type은 브라우저가 자동으로 multipart/form-data로 설정)
            } else if (data instanceof URLSearchParams) { // 데이터가 URLSearchParams 인스턴스인 경우
                init.headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8'; // Content-Type 설정
                init.body = data.toString(); // body에 URLSearchParams 문자열 할당
            } else if (init.headers['Content-Type'] === 'application/x-www-form-urlencoded;charset=UTF-8') {
                // Content-Type이 x-www-form-urlencoded로 명시되어 있고 data가 URLSearchParams가 아닌 경우
                init.body = new URLSearchParams(data.toString()); // 데이터를 URLSearchParams로 변환하여 body에 할당
            } else { // 그 외의 경우 (기본적으로 JSON으로 처리)
                init.headers['Content-Type'] = 'application/json;charset=UTF-8'; // Content-Type 설정
                init.body = JSON.stringify(data); // 데이터를 JSON 문자열로 변환하여 body에 할당
            }
        }

        // show(); // TODO: 로딩 스피너 등을 표시하는 함수 호출 (현재 주석 처리)

        const res = await fetch(url, init); // fetch API를 사용하여 요청 전송
        return res; // 응답 객체 반환
    }






    // HTTP 요청을 보내는 비동기 함수
    // url: 요청할 URL
    // method: HTTP 메서드 (기본값: 'POST')
    // data: 요청 본문 데이터 (기본값: null)
    // headers: 추가 헤더 (기본값: {} )
    // signal: AbortSignal (선택적)
    // expect: 응답 형식 기대값 (기본값: 'json')
    async function send(url, method = 'POST', data = null, headers = {}, signal, expect = 'json') {
        // 기본 헤더 설정: JSON 형식으로 Accept와 Content-Type 지정
        // 기존 헤더와 병합
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json; charset=utf-8',
            ...headers
        };

        // Fetch 초기화 객체 설정
        // 캐시: no-store (캐싱하지 않음)
        // signal: AbortController 신호 (취소 가능)
        let init = {
            method,
            headers,
            cache: 'no-store',
            credentials: 'same-origin',
            signal: signal || undefined  // signal이 제공되면 사용, 아니면 생략
        };

        if (data != null) {
            if (data instanceof FormData) {
                init.body = data;
            } else if (data instanceof URLSearchParams) {
                init.headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
                init.body = data.toString();
            } else if (headers['Content-Type'] === 'application/x-www-form-urlencoded;charset=UTF-8') {
                body = new URLSearchParams(data).toString();
            } else {
                init.headers['Content-Type'] = 'application/json;charset=UTF-8';
                init.body = JSON.stringify(data);
            }
        }

        // show:
        const res = await fetch(url, init);
        const ct = res.headers.get('content-type') || '';
        const text = await res.text();

        if (res.ok) {
            let payload = null;
            if (ct.includes('application/json')) {
                try {
                    payload = JSON.parse(text);
                } catch (e) {
                }
            }
        } else {
            const err = new Error((payload && payload.message) ? payload.message : `HTTP ${res.status}`);
            err.name = 'FetchJsonError';
            err.status = res.status;
            err.payload = payload;
            err.body = text;
            err.contentType = ct;
            throw err;
        }

        if (expect === "text" || ct.includes("application/json")) return text;
        try {
            return JSON.parse(text);
        } catch {
            return text;
        }
    }
    // 전역 상태 및 기본값 정의

    const DEFAULTS = {
        asideSelector: '#gnb1',  // aside 선택자 (링크가 있는 사이드바)
        containerSelector: '#contents .container',  // 콘텐츠 컨테이너 선택자
    };

    const MiniSPAState = {
        bound: false,
        opts: { ...DEFAULTS },
        onClick: null,
        onPopstate: null,
    };


    function linkDepth(a) {
        if (a.closest('ul.depth3')) return 3;
        if (a.closest('ul.depth2')) return 2;
        return 1;
    }

    function showLoading() {
        let el = document.getElementById('mini-sp-loading');
        if (!el) {
            el = document.createElement('div');
            el.id = 'mini-sp-loading';
            Object.assign(el.style, {
                position: 'fixed',
                right: '16px',
                bottom: '16px',
                padding: '8px 12px',
                borderRadius: '8px',
                background: '#fff',
                border: '1px solid #ddd',
                boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                zIndex: 9999
            });
            el.textContent = '로딩 중...';
            document.body.appendChild(el);
        }
        el.style.display = 'block';
    }

    function hideLoading() {
        const el = document.getElementById('mini-sp-loading');
        if (el) el.style.display = 'none';
    }
    /* 콘텐츠를 비동기로 불러와 컨테이너에 삽입
    * @param {string} url - 불러올 URL
    * @param {string} html - HTML 문자열 (swapFromHtml에서 사용)
    */

    function toggleMenu(buttonEl) {
        const li = buttonEl.closest('li.has-sub');
        if (!li) return;
        const subBox = li.querySelector(':scope > .depth-box');
        const opened = li.classList.toggle('is-open');
        buttonEl.setAttribute('aria-expanded', opened ? 'true' : 'false');
        if (subBox) subBox.setAttribute('aria-hidden', opened ? 'false' : 'true');
    }

    function highlightActiveAside(currentUrl) {
        const ASIDE_SELECTOR = MiniSPAState.opts.asideSelector || '#gnb1';
        const links = Array.from(document.querySelectorAll(`${ASIDE_SELECTOR} a.swap-link[href]`));
        links.forEach(a => a.classList.remove('is-active'));

        const cur = new URL(currentUrl || location.href, location.href);
        const curKey = cur.pathname + cur.search;

        const candidates = links.map(a => {
            try {
                const u = new URL(a.href, location.href);
                if (u.origin !== location.origin) return null;
                const key = u.pathname + u.search;
                return { a, key, len: key.length };
            } catch { return null; }
        }).filter(Boolean);

        if (candidates.length === 0) return;

        // 정확 매칭 우선, 없으면 가장 긴 prefix
        let best = candidates.find(c => c.key === curKey);
        if (!best) {
            const pref = candidates.filter(c => curKey.startsWith(c.key));
            if (pref.length) best = pref.sort((a, b) => b.len - a.len)[0];
        }
        if (!best) best = candidates.sort((a, b) => b.len - a.len)[0];

        best.a.classList.add('is-active');

        // 상위 메뉴 열기 (li.has-sub > button + .depth-box)
        const li = best.a.closest('li.has-sub');
        const btn = li ? li.querySelector(':scope > button[aria-haspopup="true"]') : null;
        const box = li ? li.querySelector(':scope > .depth-box') : null;
        if (li) li.classList.add('is-open');
        if (btn) btn.setAttribute('aria-expanded', 'true');
        if (box) box.setAttribute('aria-hidden', 'false');
    }


    async function fetchContent(url) {
        try {
            const response = await fetch(url);
            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            const newContent = doc.querySelector(MiniSPAState.opts.containerSelector);
            if (newContent) {
                document.querySelector(MiniSPAState.opts.containerSelector).innerHTML = newContent.innerHTML;
            }
            return html;  // swapFromHtml에서 재사용 위해 반환
        } catch (error) {
            console.error('콘텐츠 로드 실패:', error);
            throw error;
        }
    }

    /**
     * HTML 문자열로부터 컨테이너 콘텐츠 교체
     * @param {string} html - 불러온 HTML
     * @param {string} url - 현재 URL
     */
   async function swapFromHtml(html, url, push) {
     const doc = new DOMParser().parseFromString(html, 'text/html');
     const next = doc.querySelector(MiniSPAState.opts.containerSelector)
         || doc.querySelector("#contents .container")
         || null;
     const cur = document.querySelector(MiniSPAState.opts.containerSelector);

     // 컨테이너 못 찾으면 폴백
     if (!(cur && next)) {
       hideLoading();
       location.href = url;
       return;
     }

     // 1) 교체 전에 next 안의 inline script를 미리 뽑아둠
     const inlineScripts = Array.from(next.querySelectorAll('script:not([src])')).map(s => ({
       type: s.getAttribute('type') || '',
       noModule: s.hasAttribute('noModule'),
       code: s.textContent || ''
     }));

     // 2) 컨테이너 교체
     cur.innerHTML = next.innerHTML;

     // 3) 인라인 스크립트 재실행
     for (const s of inlineScripts) {
       const el = document.createElement('script');
       if (s.type) el.type = s.type;
       if (s.noModule) el.noModule = true;
       el.textContent = s.code;
       cur.appendChild(el);
     }

     window.scrollTo({ top: 0, left: 0, behavior: 'auto' });

     // AOS/기타 위젯 재바인딩 (이미지/폰트 로딩 고려)
     if (window.Common?.Reinit?.run) {
       // 컨테이너 노드 기준으로만 스캔하면 성능에 유리
       await window.Common.Reinit.run(cur);
     }
     // =========================

     // 4) 타이틀 변경
     const title = doc.querySelector('title')?.textContent?.trim();
     if (title) document.title = title;

     // 5) 히스토리
     if (push) history.pushState({}, '', url);

     // 6) 네비 하이라이트 갱신
     highlightActiveAside(url);

     // (Reinit는 위에서 이미 호출함)
     hideLoading();
   }












    /**
     * 현재 URL에 맞는 aside 링크 하이라이트
     * @param {string} url - 하이라이트할 URL
     */


    /**
     * 네비게이션 및 스왑: 콘텐츠 로드 + 히스토리 업데이트 + 하이라이트
     * @param {string} url - 이동할 URL
     * @param {boolean} push - pushState 사용 여부 (true: push, false: replace)
     */
    async function navigateAndSwap(url, push) {
        showLoading();
        let res;
        try {
            res = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
        } catch (e) {
            hideLoading();
            alert('네트워크 오류가 발생했습니다.');
            throw e;
        }
        if (!res.ok) {
            hideLoading();
            location.href = url; // 풀 브라우저 새로고침
            return;
        }
        const html = await res.text();
        await swapFromHtml(html, url, push);
    }

    // 기존 handleClick 전체 교체
    function handleClick(e) {
        // 1) 서브 열기/닫기(버튼)
        const menuBtn = e.target.closest('#gnb1 li.has-sub > button[aria-haspopup="true"]');
        if (menuBtn) {
            e.preventDefault();
            toggleMenu(menuBtn);
            return;
        }

        // 2) SPA 링크 처리 (.swap-link)
        const a = e.target.closest('a.swap-link[href]');
        if (!a) return;

        // 새창/수정키는 그대로
        if (a.getAttribute('target') === '_blank' || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) return;

        // 외부 링크는 패스
        try {
            const u = new URL(a.href, location.href);
            if (u.origin !== location.origin) return;
        } catch { return; }

        // # 같은 더미도 패스
        if (!a.href || a.getAttribute('href').trim().endsWith('#')) return;

        e.preventDefault();
        navigateAndSwap(a.href, true).catch(console.error);
    }
    /**
     * popstate 핸들러: 브라우저 뒤로/앞 가기 시 SPA 전환
     * @param {Event} e - popstate 이벤트
     */
    function handlePopstate(e) {
        const url = location.href;
        navigateAndSwap(url, false);
    }

    const MiniSPA = Object.freeze({
        /**
         * SPA(1). aside 링크 + popstate 처리 + aside 표시
         * @param [{asideSelector}: string, containerSelector?: string] opts
         */
        init(opts = {}) {
            if (MiniSPAState.bound) return;

            MiniSPAState.opts = { ...DEFAULTS, ...opts };
            MiniSPAState.onClick = handleClick;
            MiniSPAState.onPopstate = handlePopstate;

            document.addEventListener('click', MiniSPAState.onClick);
            window.addEventListener('popstate', MiniSPAState.onPopstate);

            // 현재 URL 체크 표시
            highlightActiveAside(location.href);

            MiniSPAState.bound = true;
        },

        /**
         * 파괴: 이벤트 리스너 제거
         */
        destroy() {
            if (!MiniSPAState.bound) return;  // 바인딩되지 않은 경우 스킵 (조건 수정)

            document.removeEventListener('click', MiniSPAState.onClick);
            window.removeEventListener('popstate', MiniSPAState.onPopstate);

            MiniSPAState.bound = false;
        },

        /**
         * GET으로 변경. container만 교체 (pushState-결 true)
         */
        go(url, push = true) {
            return navigateAndSwap(url, push);
        },

        /**
         * 동일 URL 변경 처리 (현재페이지 새로고침)
         */
        reload() {
            return navigateAndSwap(location.href, /*push*/ false);
        },

        /**
         * 현재 URL 기준 표시
         */
        highlight(url = location.href) {
            highlightActiveAside(url);
        },

        /**
         * replaceState 통해 URL 변경 (현재페이지 새로고침)
         */
        goReplace(url) {
            return navigateAndSwap(url, /*push*/ false);
        },

        /**
         * a 태그 클릭 SPA 변경 처리
         */
        goFromAnchor(event, anchor, { push = true } = {}) {
            if (event) event.preventDefault();
            const url = anchor.href;
            return navigateAndSwap(url, push);
        },

        /**
         * GET 방식으로 쿼리 문자열 처리 (버그 수정: &&로 변경, 조건 반전)
         */
        goGET(url, params = {}, { push = true } = {}) {
            const u = new URL(url, location.href);
            Object.entries(params).forEach(([k, v]) => {
                if (v !== undefined && v !== null) u.searchParams.set(k, v);  // 수정: 조건 반전
            });
            return navigateAndSwap(u.toString(), push);
        },

        /**
         * POST 요청 HTML container만 교체 (body 처리 보완: JSON.stringify 가정)
         */
        async goPOST(url, body, { headers = {}, push = true } = {}) {
            showLoading();
            let res;
            try {
                const postBody = typeof body === 'object' ? JSON.stringify(body) : body;
                const postHeaders = {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Content-Type': 'application/json',  // JSON body 가정; 필요시 제거
                    ...headers
                };
                res = await fetch(url, {
                    method: 'POST',
                    headers: postHeaders,
                    body: postBody
                });
            } catch (e) {
                hideLoading();
                alert('네트워크 오류가 발생했습니다.');
                throw e;
            }
            if (res.ok) {
                hideLoading();
                // SPA 유지 위해 push/replace 사용 (수정: 풀 리로드 대신)
                return navigateAndSwap(url, push);
            }
            const html = await res.text();
            await swapFromHtml(html, url, push);
        }
    });
    const Reinit = {
      async run(root = document) {
        // 1) 폰트/이미지 로딩 대기 (레이아웃 확정 후 AOS 계산 정확히)
        async function imagesReady(scope) {
          const imgs = Array.from(scope.querySelectorAll('img'));
          if (!imgs.length) return;
          await Promise.allSettled(imgs.map(img => {
            if (img.complete && img.naturalWidth > 0) return Promise.resolve();
            return new Promise(res => {
              img.addEventListener('load', res, { once: true });
              img.addEventListener('error', res, { once: true });
            });
          }));
        }
        try {
          if (document.fonts?.ready) await document.fonts.ready;
        } catch {}

        await imagesReady(root);

        // 2) AOS 재초기화/리프레시
        if (window.AOS) {
          // 최초 1회만 init, 이후에는 refreshHard
          if (!window.__aosInited) {
            AOS.init({ duration: 600, once: true });
            window.__aosInited = true;
          } else {
            AOS.refreshHard(); // 새 data-aos 요소까지 재스캔
          }
        }
      }
    };

    // 사용 예시
    // MiniSPA.init({ asideSelector: '#myAside', containerSelector: '#content' });
    // MiniSPA.go('/new-page');
    // 나중에: MiniSPA.destroy();

    // 글로벌 common 객체 정의: 자주 사용되는 함수들을 Object.freeze로 동결하여 불변성 보장
    // 이 객체는 Excel 내보내기, 재로딩, 안전한 요청, 스왑, 모달 등 다양한 유틸리티 함수 포함
    global.Common = Object.freeze({
        // Excel 파일 내보내기 함수 (bindExcelExport)
        collectFromForm,

        bindExcelExport,

        // 재로딩 함수 (reloadList)
        reloadList,

        // 안전한 요청 함수 (sendSafe)
        sendSafe,

        // 스왑 함수 (swap)
        swap,

        // 모달 표시 함수 (modalShow)
        modalShow,

        // 다중 파트 처리 함수 (multipart)
        toMultiPart,

        // 섹션 행 수집 함수 (collectSectionRows)
        collectSectionRows,

        // 병합 처리 함수 (merge)
        mergeAs,

        // 선택된 행 이동 함수 (moveSelectedRows)
        moveSelectedRows,

        // 헤더 체크 동기화 함수 (syncHeaderCheckBox)
        syncHeaderCheckBox,

        // HTML 가져오기 함수 (fetchHtml)
        fetchHtml,

        // 빈 행 동기화 함수 (syncEmptyRow)
        syncEmptyRow,

        // 모달 닫기 함수 (closeModal)
        closeModal,
        openModal,

        // JSON 수집 함수 (collectAsJson)
        collectAsJson,
        MiniSPA,
        Reinit
    })
})(window);