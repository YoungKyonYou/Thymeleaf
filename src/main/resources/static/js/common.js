(function (global) {
    'use strict';

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

    // function initDatePicker() {
    //     // 중복 초기화 방지
    //     window.__JQ_PERIOD_FIXED__ = window.__JQ_PERIOD_FIXED__ || false;
    //     if (window.__JQ_PERIOD_FIXED__) return;
    //     window.__JQ_PERIOD_FIXED__ = true;
    //
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
     * 엑셀 Import 공통 바인딩
     *
     * @param {HTMLInputElement} fileInput  엑셀 업로드 <input type="file">
     * @param {string} provider             서버측 ImportProvider name (ex: "user")
     * @param {function(Object[]):void} onSuccessRows   rows 콜백 (그리드 갱신용)
     * @param {function(Object[]):void} onErrors        에러 콜백 (에러표시용)
     * @param {Object} extraParams          추가 파라미터 (필요시)
     */
    async function bindExcelImport(fileInput, provider, onSuccessRows, onErrors, extraParams) {
        if (!fileInput) return;

        // 한 번이라도 성공적으로 업로드 했는지 여부
        let hasUploadedOnce = false;

        // 실제 업로드 수행 로직을 함수로 분리
        async function doUpload(file) {
            const form = new FormData();
            form.append('provider', provider);
            form.append('file', file);

            if (extraParams) {
                Object.entries(extraParams).forEach(([k, v]) => {
                    form.append(k, v);
                });
            }

            const res = await sendSafe('/import/xlsx', {
                method: 'POST',
                data: form,
                multipart: true
            });

            // Abort 된 경우(null)나, 에러(ok=false)는 여기서 그냥 종료
            if (!res || !res.ok) {
                // sendSafe 안에서 이미 모달을 띄웠으므로 추가 처리 없이 리턴
                fileInput.value = '';   // 그래도 다시 선택 가능하게 리셋
                return;
            }

            // sendSafe OK → res.data 에 JSON 이 들어있다고 가정
            const json = res.data;
            // json = { rows: [...], errors: [...], totalRows, successRows, errorRows }

            if (onSuccessRows && Array.isArray(json.rows)) {
                onSuccessRows(json.rows);
            }

            if (onErrors && Array.isArray(json.errors)) {
                onErrors(json.errors);
            }

            // 기본 로그/알림
            if (json.errorRows && json.errorRows > 0) {
                console.warn('Import errors: ', json.errors);
                modalShow({
                    title: '알림',
                    message: `총 ${json.totalRows}건 중 ${json.successRows}건 성공, ${json.errorRows}건 실패했습니다.`
                });
            }

            // 여기까지 왔으면 적어도 한 번은 업로드 성공으로 취급
            hasUploadedOnce = true;

            // 같은 파일을 다시 선택해도 change 이벤트가 발생하도록 value 리셋
            fileInput.value = '';
        }

        fileInput.addEventListener('change', function () {
            const file = fileInput.files && fileInput.files[0];
            if (!file) return;

            // 아직 한 번도 업로드 안 했으면 바로 업로드
            if (!hasUploadedOnce) {
                doUpload(file).catch(console.error);
                return;
            }

            // 이미 업로드한 적이 있으면 모달로 한 번 더 확인
            modalShow({
                title: '확인',
                message: '이미 데이터를 한 번 업로드했습니다.\n기존 내용을 지우고 다시 업로드하시겠습니까?',
                buttons: 'ok-close',
                okText: '다시 업로드',
                closeText: '취소',
                onOk: function () {
                    // 확인 눌렀을 때 다시 업로드
                    doUpload(file).catch(console.error);
                },
                onClose: function () {
                    // 취소한 경우도 파일 선택 상태는 비워 줘야
                    // 같은 파일을 다시 선택해도 change가 뜬다
                    fileInput.value = '';
                }
            });
        });
    }

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
        if (!exportBtn) return;

        const provider = exportBtn.dataset.provider;
        if (!provider) {
            alert("'provider'가 지정되지 않았습니다.");
            return;
        }

        // searchForm에서 입력된 값을 수집하여 URLSearchParams 객체로 변환
        const params = new URLSearchParams(collectFromForm(searchForm));
        const applied = collectFromForm(searchForm);

        if (!applied) {
            alert('searchForm이 지정되지 않았습니다.');
            return;
        }

        // applied 객체의 모든 키-값 쌍에 대해
        Object.entries(applied).forEach(([k, v]) => {
            if (v != null && String(v).trim() !== '') {
                params.set(k, v);
            }
        });

        // 정렬 관련 파라미터 설정
        params.set('sort', sortValue || defaultSortColumn);
        params.set('dir', dirValue || 'asc');

        // Excel 내보내기 요청
        const res = await sendExcel(
            `/export/xlsx?${params.toString()}`,
            {
                method: 'POST',
                data: payload
            }
        );

        // 응답 헤더에서 'Content-Disposition' 가져오기
        const cd = res.headers.get('Content-Disposition') || '';

        // 파일 이름 추출 정규식: UTF-8 인코딩 파일명 또는 일반 파일명
        const match = cd.match(/filename\*=UTF-8''([^;]+)|filename="([^"]*)"/i);

        // 파일 이름 디코딩
        const filename = match ? decodeURIComponent(match[1] || match[2]) : 'export.xlsx';

        // Blob 데이터를 이용하여 파일 다운로드
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();

        URL.revokeObjectURL(url);
    }

    /**
     * 안전한 HTTP 요청을 보내는 비동기 함수 (send 함수를 래핑하여 에러 처리 강화)
     *
     * url: 요청할 URL
     * method: HTTP 메서드 (기본값: 'POST')
     * data: 요청 본문 데이터 (기본값: null)
     * signal: AbortSignal (선택적)
     * headers: 추가 헤더 (선택적)
     * expect: 응답 형식 기대값 (기본값: 'json')
     * clientErrorMsg: 클라이언트 에러 메시지 (기본값: '요청에 실패했습니다.')
     * otherErrorMsg: 기타 에러 메시지 (기본값: '오류가 발생했습니다.')
     */
    async function sendSafe(
        url,
        {
            method = 'POST',
            data = null,
            signal,
            headers,
            expect = 'json',
            clientErrorMsg = '요청에 실패했습니다.',
            otherErrorMsg = '오류가 발생했습니다.',
            multipart = false
        } = {}
    ) {
        try {
            const out = await send(url, method, data, headers, signal, expect, multipart);
            return { ok: true, data: out };
        } catch (e) {
            // AbortError인 경우 (요청 취소)
            if (e.name === 'AbortError') {
                return null;
            }

            // 400 ~ 499 클라이언트 에러
            if (e?.status >= 400 && e.status < 500) {
                const msg = (e.payload && e.payload.message) ? e.payload.message : clientErrorMsg;

                modalShow({
                    title: '알림',
                    message: msg,
                    buttons: 'close'
                });

                return { ok: false, status: e.status, error: e };
            }

            // 기타 에러
            console.error(e);

            modalShow({
                title: '오류',
                message: otherErrorMsg,
                buttons: 'close'
            });

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

    /**
     * tbody가 비었으면 "조회 결과 없음" 한 줄 보여주는 util
     */
    function syncEmptyRow(tbodyOrSelector, opts = {}) {
        const {
            message = '조회 결과가 없습니다.',
            colspan,
            rowSelector = "tr:not([data-empty-row='true'])"
        } = opts;

        const tbody = typeof tbodyOrSelector === 'string'
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
                const table = tbody.closest('table');
                let span = colspan;
                if (!span && table) {
                    const theadCells = table.tHead?.rows?.[0]?.cells?.length || 0;
                    if (theadCells > 0) span = theadCells;
                }
                if (!span) {
                    const firstTr = tbody.querySelector('tr');
                    span = firstTr?.cells?.length || 1;
                }

                // 빈 행 생성
                emptyRow = document.createElement('tr');
                emptyRow.setAttribute('data-empty-row', 'true');

                const td = document.createElement('td');
                td.colSpan = span;
                td.className = 'no-data';
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
        return (text || '')
            .replace(/\s+/g, ' ')       // 연속된 공백을 하나로
            .replace(/\u00A0/g, ' ')    // nbsp 제거
            .trim()
            .toLowerCase();
    }

    /**
     * thead 헤더 텍스트 → DTO 키 매핑
     * @param {HTMLTableElement} table
     * @param {{ headerMap: Object<string,string>, requiredKeys: string[] }} cfg
     * @returns {Object<number,string>} 인덱스→키 매핑 객체
     */
    function buildHeaderKeyMap(table, { headerMap, requiredKeys }) {
        if (!headerMap || !Object.keys(headerMap).length) {
            throw new Error(
                "headerMap이 필요합니다. 예) {'관리자 id':'adminId','관리자명':'adminName','역할':'roleName'}"
            );
        }
        if (!Array.isArray(requiredKeys) || !requiredKeys.length) {
            throw new Error(
                "requiredKeys가 필요합니다. 예) ['adminId','adminName','roleName']"
            );
        }

        const ths = table.querySelectorAll('thead th');
        if (!ths.length) throw new Error('테이블 헤더가 없습니다.');

        const indexToKey = {};
        ths.forEach((th, idx) => {
            const key = headerMap[norm(th.textContent)];
            if (key) indexToKey[idx] = key;
        });

        const present = new Set(Object.values(indexToKey));
        const missing = requiredKeys.filter(k => !present.has(k));
        if (missing.length) {
            throw new Error('필수 컬럼 매핑 부족: ' + missing.join(', '));
        }

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
        tr.querySelectorAll('td').forEach((td, i) => {
            const key = indexToKey[i];
            if (key) obj[key] = (td.textContent || '').trim();
        });
        return obj;
    }

    /**
     * 일반 table 태그에서 thead 헤더 텍스트 기준으로
     * tbody 데이터를 DTO 배열 + payload 로 만들어 준다.
     *
     * @param {HTMLTableElement|string} tableOrSelector table 요소 또는 CSS 셀렉터
     * @param {{
     *   headerMap: Object<string,string>,
     *   requiredKeys?: string[],
     *   wrapperKey?: string,
     *   skipEmptyRow?: boolean
     * }} opts
     * @returns {{ [key: string]: Array<Object<string,string>> }}
     */
    function collectTablePayload(tableOrSelector, opts = {}) {
        const {
            headerMap,
            requiredKeys = [],
            wrapperKey = 'list',
            skipEmptyRow = true
        } = opts;

        const table = (typeof tableOrSelector === 'string')
            ? document.querySelector(tableOrSelector)
            : tableOrSelector;

        if (!table) {
            throw new Error('collectTablePayload: 테이블을 찾을 수 없습니다.');
        }

        const indexToKey = buildHeaderKeyMap(table, { headerMap, requiredKeys });
        const rows = table.querySelectorAll('tbody tr');
        const list = [];

        rows.forEach(tr => {
            // "조회 결과 없음" placeholder 등은 건너뛰기
            if (tr.matches('[data-empty-row="true"]')) return;

            const obj = rowToObject(tr, indexToKey);

            if (skipEmptyRow) {
                const hasValue = Object.values(obj).some(
                    v => v != null && String(v).trim() !== ''
                );
                if (!hasValue) return;
            }
            list.push(obj);
        });

        return { [wrapperKey]: list };
    }

    /**
     * 섹션 행 수집 (div[data-section] 기준)
     * - filter: 'selected' | 'unselected' | 'all'
     *
     * @param {Element|string} modalBodyOrSelector .modal-body 엘리먼트 또는 셀렉터
     * @param {string} sectionKey data-section 값 ('list' 등)
     * @param {{
     *   headerMap?: Object,
     *   requiredKeys?: string[],
     *   filter?: ('selected'|'unselected'|'all')
     * }} param2
     * @returns {Array<Object<string,string>>}
     */
    function collectSectionRows(
        modalBodyOrSelector,
        sectionKey,
        { headerMap = {}, requiredKeys = [], filter = 'all' } = {}
    ) {
        const root = (typeof modalBodyOrSelector === 'string')
            ? document.querySelector(modalBodyOrSelector)
            : modalBodyOrSelector;

        if (!root) throw new Error('modalBody를 찾을 수 없습니다.');

        const sectionRoot = root.querySelector(`[data-section="${sectionKey}"]`);
        if (!sectionRoot) throw new Error('섹션을 찾을 수 없습니다: ' + sectionKey);

        const table = sectionRoot.querySelector('table');
        if (!table) throw new Error('섹션 테이블을 찾을 수 없습니다: ' + sectionKey);

        const indexToKey = buildHeaderKeyMap(table, { headerMap, requiredKeys });

        let rowSelector = 'tbody tr';
        if (filter === 'selected') rowSelector = 'tbody tr.is-selected';
        else if (filter === 'unselected') rowSelector = 'tbody tr:not(.is-selected)';

        const rows = table.querySelectorAll(rowSelector);
        const out = [];
        rows.forEach(tr => out.push(rowToObject(tr, indexToKey)));
        return out;
    }

    /**
     * tbody의 체크 상태로 헤더(전체선택) 체크박스를 상태 동기화
     *
     * @param {HTMLTableSectionElement} tbody
     * @returns {void}
     */
    function syncHeaderCheckBox(tbody) {
        const table = tbody.closest('table');
        const headCb =
            table?.tHead?.querySelector('input[type="checkbox"]') ||
            table?.querySelector('thead input[type="checkbox"]');
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
     * @param {Array<Object<string,string>>} listRows
     * @param {Array<Object<string,string>>} addedRows
     * @param {{list?: string, added?: string}} keys
     * @returns {{ [key: string]: Array<Object<string,string>> }}
     */
    function mergeAs(listRows, addedRows, keys) {
        const listKey = (keys && keys.list) ? keys.list : 'list';
        const addedKey = (keys && keys.added) ? keys.added : 'added';

        return {
            [listKey]: Array.isArray(listRows) ? listRows : [],
            [addedKey]: Array.isArray(addedRows) ? addedRows : []
        };
    }

    /**
     * 섹션(table/thead/tbody/헤더체크박스) 요소를 얻기
     * @param {Element|string} root .modal-body 엘리먼트 또는 셀렉터
     * @param {string} sectionKey data-section 값 ('list' | 'added' 등)
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
     *
     * @param {Element|string} modalBodyOrSelector
     * @param {string} fromKey
     * @param {string} toKey
     * @param {Object} [opts]
     * @param {boolean} [opts.resetCheckbox=true]
     * @param {'from'|'to'|'both'|boolean} [opts.clearSelectAll='both']
     * @param {boolean} [opts.syncHeader=true]
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
                tr.querySelectorAll('input[type="checkbox"]').forEach(cb => {
                    cb.checked = false;
                });
            }
            toTbody.appendChild(tr);
        });

        // 헤더 전체선택 강제 해제
        if (clearSelectAll === true || clearSelectAll === 'both') {
            if (fromHeader) {
                fromHeader.checked = false;
                fromHeader.indeterminate = false;
            }
            if (toHeader) {
                toHeader.checked = false;
                toHeader.indeterminate = false;
            }
        } else if (clearSelectAll === 'from') {
            if (fromHeader) {
                fromHeader.checked = false;
                fromHeader.indeterminate = false;
            }
        } else if (clearSelectAll === 'to') {
            if (toHeader) {
                toHeader.checked = false;
                toHeader.indeterminate = false;
            }
        }

        // 헤더 상태 동기화
        if (syncHeader) {
            syncHeaderCheckBox(fromTbody);
            syncHeaderCheckBox(toTbody);
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
            if (m.getAttribute('aria-hidden') === 'true') return;
            if (e.key === 'Escape') modalHide();
        });

        // data-dismiss 클릭 시 닫기
        document.addEventListener('click', function (e) {
            var m = getModal();
            if (m.getAttribute('aria-hidden') === 'true') return;
            var t = e.target.closest('[data-dismiss]');
            if (!t) return;
            e.preventDefault();
            modalHide();
        });

        MODAL_BOUND = true;
    }

    // 내부: 버튼 핸들러 추적/해제
    function rememberHandler(modal, el, type, fn) {
        modal._handlers = modal._handlers || [];
        modal._handlers.push({ el: el, type: type, fn: fn });
    }

    function cleanupButtonHandlers(modal) {
        if (!modal._handlers) return;

        for (var i = 0; i < modal._handlers.length; i++) {
            var h = modal._handlers[i];
            if (h.el && h.el.removeEventListener) {
                h.el.removeEventListener(h.type, h.fn);
            }
        }
        modal._handlers = [];
    }

    /*
     * 개체로만 받는 API
     * @param {Object} param0
     * @param {string} param0.message 본문 메시지(필수)
     * @param {string} param0.title 타이틀(없으면 '알림')
     * @param {string} [param0.buttons='close'] 버튼 구성 ('close' | 'ok-close')
     * @param {Function} [param0.onOk] 확인 클릭 시 콜백 (버튼이 ok-close일 때만 유효)
     * @param {Function} [param0.onClose] 닫힘 (ESC/오버레이/취소/닫기) 포함 콜백
     * @param {string} [param0.okText='확인']
     * @param {string} [param0.closeText='닫기']
     */
    function modalShow({
        message,
        title = '알림',
        buttons = 'close',
        onOk,
        onClose,
        okText = '확인',
        closeText = '닫기'
    }) {
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
        var btnOk = m.querySelector('.modal-footer .btn-blue');          // 확인 (파란)

        // 이전 버튼 정리
        cleanupButtonHandlers(m);

        // 버튼 구성
        if (buttons === 'close') {
            if (btnCancel) {
                btnCancel.style.display = 'none';
            }
            if (btnOk) {
                btnOk.textContent = closeText;
                btnOk.setAttribute('data-dismiss', '');
                btnOk.style.display = '';
            }
        } else {
            if (btnCancel) {
                btnCancel.textContent = closeText;
                btnCancel.setAttribute('data-dismiss', '');
                btnCancel.style.display = '';
            }
            if (btnOk) {
                btnOk.textContent = okText;
                btnOk.removeAttribute('data-dismiss');
                btnOk.style.display = '';
            }
        }

        m.setAttribute('aria-hidden', 'false');
        m.classList.add('is-open');
        document.body.classList.add('modal-open');

        // 포커스
        MODAL_PREV_ACTIVE = document.activeElement;
        var container = m.querySelector('.modal-container');
        (container || m).focus();

        // 확인 버튼 핸들러
        if (buttons === 'ok-close' && btnOk) {
            var okHandler = function () {
                try {
                    onOk && onOk();
                } finally {
                    modalHide();
                }
            };
            btnOk.addEventListener('click', okHandler, { once: true });
            rememberHandler(m, btnOk, 'click', okHandler);
        }

        // 닫힘 콜백 등록
        m._onClose = (typeof onClose === 'function') ? onClose : null;
    }

    function modalHide() {
        var m = getModal();

        m.setAttribute('aria-hidden', 'true');
        m.classList.remove('is-open');
        document.body.classList.remove('modal-open');

        // 버튼 핸들러 해제
        cleanupButtonHandlers(m);

        // 닫힘 콜백
        if (typeof m._onClose === 'function') {
            try {
                m._onClose();
            } finally {
                m._onClose = null;
            }
        }

        // 포커스 복귀
        if (MODAL_PREV_ACTIVE && typeof MODAL_PREV_ACTIVE.focus === 'function') {
            MODAL_PREV_ACTIVE.focus();
        }
        MODAL_PREV_ACTIVE = null;
    }

    /**
     * 요청을 위한 baseUrl + queryParams를 붙이는 함수
     */
    const buildUrl = (base, params = {}) => {
        const u = new URL(base, window.location.origin);
        Object.entries(params).forEach(([k, v]) => {
            if (v !== '??') {
                u.searchParams.set(k, v);
            }
        });
        // 캐시 방지용
        u.searchParams.set('_', Date.now());
        return u.toString();
    };

    /**
     * 문자열 상태의 html → Document
     */
    const parseHtml = (html) => new DOMParser().parseFromString(html, 'text/html');

    /**
     * 기존 태그를 새로운 데이터가 들어간 태그로 교체
     */
    const swap = (html, selector) => {
        if (!html) return;
        const doc = parseHtml(html);
        const next = doc.querySelector(selector);
        const curr = document.querySelector(selector);
        if (next && curr) curr.replaceWith(next);
    };

    /**
     * url로 요청해서 받은 html을 반환
     */
    const fetchHtml = async (url, opt = {}) => {
        const res = await fetch(url, {
            headers: { 'Accept': 'text/html, application/json;q=0.9' },
            cache: 'no-store',
            ...opt
        });

        const ct = res.headers.get('content-type') || '';
        let bodyText = await res.text();

        if (!res.ok) {
            let payload = null;
            if (ct.includes('application/json')) {
                try { payload = JSON.parse(bodyText); } catch (e) { /* ignore */ }
            }
            const message = (payload && payload.message)
                ? payload.message
                : `HTTP ${res.status}`;

            const err = new Error(message);
            err.name = 'FetchHtmlError';
            err.status = res.status;
            err.body = bodyText;
            err.payload = payload;
            err.contentType = ct;
            throw err;
        }
        return bodyText;
    };

    /**
     * 현재 활성 페이지 링크가 없으면 0 반환
     */
    const activePage = () => {
        const a = document.querySelector('#grid-pager a.grid-pager[aria-current="page"]');
        if (!a) return 0;

        const u = new URL(a.getAttribute('href') || '', window.location.origin);
        return parseInt(u.searchParams.get('page') || '0', 10) || 0;
    };

    /**
     * 폼에서 현재 입력된 값을 읽음
     */
    function collectFromForm(selectorForm) {
        const params = {};
        if (selectorForm) {
            const fd = new FormData(selectorForm);
            for (const [k, v] of fd.entries()) {
                if (v != null && String(v).trim() !== '') {
                    params[k] = v;
                }
            }
        }
        return params;
    }

    /**
     * name이 list[0].field 형태인 FormData를 JSON으로 변환
     */
    function collectAsJson(form) {
        const fd = new FormData(form);
        const payload = { list: [] };

        const re = /^list\[(\d+)\]\.(.+)$/;
        for (const [name, value] of fd.entries()) {
            const m = name.match(re);
            if (!m) continue;

            const idx = Number(m[1]);
            const field = m[2];

            if (!payload.list[idx]) {
                payload.list[idx] = {};
            }
            payload.list[idx][field] = value;
        }
        return payload;
    }

    let listAbort;
    let applied = collectFromForm();

    /**
     * withSortAndSize
     */
    const withSortAndSize = (
        base,
        overrides = {},
        selectSize,
        inputSort,
        inputDir,
        defaultSortColumn
    ) => ({
        ...base,
        size: parseInt(selectSize?.value || '10', 10),
        sort: inputSort?.value || defaultSortColumn,
        dir: inputDir?.value || 'asc',
        page: 0,
        ...overrides
    });

    /**
     * state
     */
    const state = ({
        overrides = {},
        selectSize,
        inputSort,
        inputDir,
        defaultSortColumn,
        applied
    } = {}) => ({
        ...withSortAndSize(applied, overrides, selectSize, inputSort, inputDir, defaultSortColumn)
    });

    /**
     * 목록 재조회
     */
    async function reloadList({
        page,
        defaultSortColumn,
        swapTargets = ['#grid-tbody', '#grid-pager'],
        baseUrl,
        selectSize,
        inputSort,
        inputDir,
        applied
    }) {
        listAbort?.abort();
        listAbort = new AbortController();
        const signal = listAbort.signal;

        const params = state({
            overrides: { page: typeof page === 'number' ? page : activePage() },
            selectSize,
            inputSort,
            inputDir,
            defaultSortColumn,
            applied
        });

        try {
            const html = await fetchHtml(buildUrl(baseUrl, params), { signal });

            for (const sel of swapTargets) {
                swap(html, sel);
            }

            if (typeof bindPagination === 'function') {
                bindPagination(baseUrl, {
                    page: 0,
                    size: parseInt(selectSize?.value || '10', 10),
                    dir: inputDir?.value || 'asc',
                    sort: inputSort?.value || defaultSortColumn
                });
            }

            if (selectSize) selectSize.value = params.size;
            if (inputDir) inputDir.value = params.dir;
            if (inputSort) inputSort.value = params.sort;
        } catch (e) {
            if (e.name === 'AbortError') return;

            if (e?.status >= 400 && e.status < 500) {
                const msg = e.payload?.message || '요청을 처리할 수 없습니다.';
                modalShow({
                    message: msg,
                    title: '알림',
                    buttons: 'close'
                });
                return;
            }

            alert('목록을 불러오지 못했습니다.');
            console.error(e);
        }
    }

    /**
     * multipart FormData 생성
     *
     * @param {any} data Files | FileList | File[] | { files: ... , ... }
     * @param {string[]} extensions 허용 확장자
     */
    const toMultiPart = (data, extensions = []) => {
        const MAX_SIZE = 20 * 1024 * 1024; // 20MB
        const DISALLOW_EMPTY = true;

        const normExts = (extensions || [])
            .map(e => String(e).replace(/(\.)/g, '\\.').toLowerCase());
        const allowedLabel = normExts.length ? '.' + normExts.join(', .') : '';

        function isInstanceofFiles(files) {
            return files instanceof FileList ||
                (Array.isArray(files) && files.every(f => f instanceof File));
        }

        function pickOneFile(files) {
            if (!files) return null;
            if (files instanceof File) return files;

            if (isInstanceofFiles(files)) {
                if (files.length === 0 && DISALLOW_EMPTY) return '__TOO_MANY__';
                return files.item ? files.item(0) : files[0];
            }

            const onlyFiles = files.filter(f => f instanceof File);
            if (onlyFiles.length === 0 && DISALLOW_EMPTY) return '__TOO_MANY__';
            return onlyFiles[0];
        }

        function show(msg) {
            modalShow({ message: msg, buttons: 'close' });
        }

        const src = data || {};
        const fileSel = pickOneFile(src.files || src);

        if (fileSel === '__TOO_MANY__') {
            show('파일은 1개만 업로드할 수 없습니다.');
            return null;
        }

        if (!fileSel) {
            show('업로드할 파일이 없습니다.');
            return null;
        }

        const name = fileSel.name || 'unnamed';
        const size = typeof fileSel.size === 'number' ? fileSel.size : 0;
        const ext = name.includes('.') ? name.split('.').pop().toLowerCase() : '';

        if (DISALLOW_EMPTY && size === 0) {
            show(`파일 "${name}"은(는) 0바이트로 업로드할 수 없습니다.`);
            return null;
        }

        if (size > MAX_SIZE) {
            show(`파일 "${name}"의 용량이 20MB를 초과했습니다. (현재: ${fmtBytes(size)})`);
            return null;
        }

        if (normExts.length && !normExts.includes(ext)) {
            show(`허용되지 않은 파일 형식입니다.\n허용 확장자: ${allowedLabel}\n파일: ${name}`);
            return null;
        }

        const meta = { ...src };
        delete meta.files;

        const fd = new FormData();
        fd.append('data', new Blob([JSON.stringify(meta)], { type: 'application/json' }));
        fd.append('file', fileSel, name);

        return fd;
    };

    // 바이트를 사람이 읽기 쉬운 문자열로 변환
    function fmtBytes(bytes) {
        if (!Number.isFinite(bytes)) return String(bytes);

        const u = ['B', 'KB', 'MB', 'GB', 'TB'];
        let i = 0;
        let n = bytes;

        while (n >= 1024 && i < u.length - 1) {
            n /= 1024;
            i++;
        }

        return `${n.toFixed(n >= 100 ? 0 : n >= 10 ? 1 : 2)} ${u[i]}`;
    }

    // Excel 데이터를 전송하는 비동기 함수
    async function sendExcel(
        url,
        { method = 'POST', data = null, headers = {}, signal, expect = 'json' } = {}
    ) {
        const init = {
            method,
            headers: {
                'Accept': 'application/json, text/plain;q=0.9',
                ...headers
            },
            cache: 'no-store',
            credentials: 'same-origin',
            signal
        };

        if (data !== null) {
            if (data instanceof FormData) {
                init.body = data;
            } else if (data instanceof URLSearchParams) {
                init.headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
                init.body = data.toString();
            } else if (init.headers['Content-Type'] === 'application/x-www-form-urlencoded;charset=UTF-8') {
                init.body = new URLSearchParams(data.toString());
            } else {
                init.headers['Content-Type'] = 'application/json;charset=UTF-8';
                init.body = JSON.stringify(data);
            }
        }

        const res = await fetch(url, init);
        return res;
    }

    /**
     * HTTP 요청 공통 함수
     *
     * @param {string} url
     * @param {string} method
     * @param {any} data
     * @param {Object} headers
     * @param {AbortSignal} signal
     * @param {'json'|'text'} expect
     * @param {boolean} multipart
     */
    async function send(
        url,
        method = 'POST',
        data = null,
        headers = {},
        signal,
        expect = 'json',
        multipart = false
    ) {
        headers = {
            'Accept': 'application/json',
            ...headers
        };

        let init = {
            method,
            headers,
            cache: 'no-store',
            credentials: 'same-origin',
            signal: signal || undefined
        };

        if (data != null) {
            if (multipart) {
                if (!(data instanceof FormData)) {
                    throw new Error('multipart=true 인 경우 data는 FormData 이어야 합니다.');
                }
                delete init.headers['Content-Type'];
                init.body = data;

            } else if (data instanceof FormData) {
                delete init.headers['Content-Type'];
                init.body = data;

            } else if (data instanceof URLSearchParams) {
                init.headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
                init.body = data.toString();

            } else if (headers['Content-Type'] === 'application/x-www-form-urlencoded;charset=UTF-8') {
                init.body = new URLSearchParams(data).toString();

            } else {
                init.headers['Content-Type'] = 'application/json;charset=UTF-8';
                init.body = JSON.stringify(data);
            }
        }

        const res = await fetch(url, init);
        const ct = res.headers.get('content-type') || '';
        const text = await res.text();

        let payload = null;
        if (ct.includes('application/json')) {
            try {
                payload = JSON.parse(text);
            } catch (e) {
                // ignore
            }
        }

        if (!res.ok) {
            const err = new Error(
                (payload && payload.message)
                    ? payload.message
                    : `HTTP ${res.status}`
            );
            err.name = 'FetchJsonError';
            err.status = res.status;
            err.payload = payload;
            err.body = text;
            err.contentType = ct;
            throw err;
        }

        if (expect === 'text') {
            return text;
        }
        if (expect === 'json') {
            return payload != null ? payload : text;
        }
        return payload != null ? payload : text;
    }

    // 글로벌 common 객체 정의
    global.Common = Object.freeze({
        collectFromForm,
        bindExcelExport,
        bindExcelImport,
        reloadList,
        collectTablePayload,
        sendSafe,
        swap,
        modalShow,
        toMultiPart,
        collectSectionRows,
        mergeAs,
        moveSelectedRows,
        syncHeaderCheckBox,
        fetchHtml,
        syncEmptyRow,
        closeModal,
        openModal,
        collectAsJson,
        showLoading,
        hideLoading
    });
})(window);

/**
 * showLoading()
 *
 * hideLoading()
 *
 * toggleMenu() (왼쪽 메뉴 열고 닫는 함수)
 *
 * highlightActiveAside() (현재 URL 기준으로 메뉴 강조하는 함수)
 *
 * HTML 네비게이션/스왑
 * fetchContent(url, options)
 *
 * swapFromHtml(html, { ... })
 *
 * navigateAndSwap(url, options) (pushState + swap 하는 애)
 *
 * 이벤트 바인딩
 * handleClick(event)
 *
 * handlePopstate(event)
 *
 * Common에 노출되던 것
 * global.Common.MiniSPA = { ... }
 *
 * global.Common.Reinit = function(...) { ... }
 *
 * 위 함수들 위에 달려 있던 주석(JSDoc, 사용법 설명) 도 전부
 */
