(function (global) {
    'use strict';

    // =========================
    // 공통 모달 기본 설정
    // =========================
    var MODAL_ID = 'modal-common';
    var MODAL_BOUND = false;
    var MODAL_PREV_ACTIVE = null;

    let lastActive = null;
    let lastScrollTop = 0;

   // 🔹 전역 로딩 카운터
       let loadingCount = 0;

       // =========================
       // 글로벌 로딩 오버레이 (DOM 보장)
       // =========================
       function ensureLoadingDom() {
           let el = document.getElementById('mini-sp-loading');

           // 레이아웃(commonHead)을 안 쓰는 페이지 대비용 fallback
           if (!el) {
               el = document.createElement('div');
               el.id = 'mini-sp-loading';
               el.setAttribute('aria-hidden', 'true');
               el.innerHTML = [
                   '<div class="global-loading-backdrop"></div>',
                   '<div class="global-loading-card" role="status" aria-label="불러오는 중">',
                   '  <div class="rabbit-wrapper">',
                   '    <div class="rabbit-orbit"></div>',
                   '    <img src="/images/img-rabbit.png" alt="Tmoney 로딩 토끼" class="rabbit-img" />',
                   '    <div class="rabbit-shadow"></div>',
                   '  </div>',
                   '  <div class="global-loading-copy">',
                   '    <p class="global-loading-title">Tmoney CMS</p>',
                   '    <p class="global-loading-text">데이터를 불러오는 중입니다...</p>',
                   '  </div>',
                   '</div>'
               ].join('');
               document.body.appendChild(el);
           }
           return el;
       }

       /**
        * 로딩 스피너 표시 (전체 오버레이)
        */
       function showLoading() {
           loadingCount += 1;
           const el = ensureLoadingDom();
           el.style.display = 'flex';
           el.setAttribute('aria-hidden', 'false');
       }

       /**
        * 로딩 스피너 숨김 (카운터 기반)
        */
       function hideLoading() {
           if (loadingCount > 0) {
               loadingCount -= 1;
           }
           if (loadingCount <= 0) {
               loadingCount = 0;
               const el = document.getElementById('mini-sp-loading');
               if (el) {
                   el.style.display = 'none';
                   el.setAttribute('aria-hidden', 'true');
               }
           }
       }
    /**
     * closeModal(elOrSelector)
     * @param {Element|string} elOrSelector - 닫을 모달의 루트 .modal 요소 또는 CSS 셀렉터
     */
    function closeModal(elOrSelector) {
        if (!elOrSelector) return;

        const modal = (
            typeof elOrSelector === 'string'
                ? document.querySelector(elOrSelector)
                : elOrSelector
        );
        if (!modal) return;

        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('body-lock');

        if (lastActive && typeof lastActive.focus === 'function') {
            lastActive.focus();
        }

        if (typeof lastScrollTop === 'number') {
            window.scrollTo({ top: lastScrollTop });
        }
    }

    /**
     * (참고) 열 때는 이렇게 저장해주세요.
     */
    function openModal(elOrSelector) {
        const modal =
            (typeof elOrSelector === 'string')
                ? document.querySelector(elOrSelector)
                : elOrSelector;
        if (!modal) return;

        lastActive = document.activeElement;
        lastScrollTop = window.pageYOffset || document.documentElement.scrollTop || 0;

        document.body.classList.add('body-lock');
        modal.setAttribute('aria-hidden', 'false');

        const firstFocusable = modal.querySelector(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        if (firstFocusable) firstFocusable.focus();
    }

    // =========================
    // fetch 전역 패치 (모든 fetch 에 로딩 연결)
    // =========================
    (function patchGlobalFetch(globalObj) {
        if (!globalObj.fetch) return;
        if (globalObj.__FETCH_WITH_LOADING__) return;
        globalObj.__FETCH_WITH_LOADING__ = true;

        const originalFetch = globalObj.fetch.bind(globalObj);

        globalObj.fetch = async function (...args) {
            showLoading();
            try {
                const res = await originalFetch(...args);
                return res;
            } finally {
                hideLoading();
            }
        };
    })(global);

    // =========================
    // 엑셀 Import 공통
    // =========================

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

        let hasUploadedOnce = false;

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

            if (!res || !res.ok) {
                fileInput.value = '';
                return;
            }

            const json = res.data;

            if (onSuccessRows && Array.isArray(json.rows)) {
                onSuccessRows(json.rows);
            }

            if (onErrors && Array.isArray(json.errors)) {
                onErrors(json.errors);
            }

            if (json.errorRows && json.errorRows > 0) {
                console.warn('Import errors: ', json.errors);
                modalShow({
                    title: '알림',
                    message: `총 ${json.totalRows}건 중 ${json.successRows}건 성공, ${json.errorRows}건 실패했습니다.`
                });
            }

            hasUploadedOnce = true;
            fileInput.value = '';
        }

        fileInput.addEventListener('change', function () {
            const file = fileInput.files && fileInput.files[0];
            if (!file) return;

            if (!hasUploadedOnce) {
                doUpload(file).catch(console.error);
                return;
            }

            modalShow({
                title: '확인',
                message: '이미 데이터를 한 번 업로드했습니다.\n기존 내용을 지우고 다시 업로드하시겠습니까?',
                buttons: 'ok-close',
                okText: '다시 업로드',
                closeText: '취소',
                onOk: function () {
                    doUpload(file).catch(console.error);
                },
                onClose: function () {
                    fileInput.value = '';
                }
            });
        });
    }

    /**
     * 엑셀 출력 API 호출
     *
     * @param {HTMLFormElement}  searchForm 검색 폼
     * @param {HTMLButtonElement} exportBtn 엑셀 출력 버튼 객체
     * @param {string} sortValue 정렬 (sort 컬럼명, 없으면 defaultSortColumn 사용)
     * @param {string} dirValue 정렬 방향, 없으면 asc 사용
     * @param {string} defaultSortColumn 디폴트 sort 컬럼명
     * @param {object} payload (requestBody에 전할 payload ex) payload = { orgcd: '', mngrid: '' }
     */
    async function bindExcelExport(searchForm, exportBtn, sortValue, dirValue, defaultSortColumn, payload) {
        if (!exportBtn) return;

        const provider = exportBtn.dataset.provider;
        if (!provider) {
            alert("'provider'가 지정되지 않았습니다.");
            return;
        }

        const applied = collectFromForm(searchForm);
        if (!applied) {
            alert('searchForm이 지정되지 않았습니다.');
            return;
        }

        const params = new URLSearchParams();

        Object.entries(applied).forEach(([k, v]) => {
            if (v != null && String(v).trim() !== '') {
                params.set(k, v);
            }
        });

        params.set('sort', sortValue || defaultSortColumn);
        params.set('dir', dirValue || 'asc');

        const res = await sendExcel(
            `/export/xlsx?${params.toString()}`,
            {
                method: 'POST',
                data: payload
            }
        );

        const cd = res.headers.get('Content-Disposition') || '';
        const match = cd.match(/filename\*=UTF-8''([^;]+)|filename="([^"]*)"/i);
        const filename = match ? decodeURIComponent(match[1] || match[2]) : 'export.xlsx';

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

    // =========================
    // HTTP 공통 - send / sendSafe
    // =========================

    /**
     * 안전한 HTTP 요청을 보내는 비동기 함수
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
            if (e.name === 'AbortError') {
                return null;
            }

            if (e?.status >= 400 && e.status < 500) {
                const msg = (e.payload && e.payload.message) ? e.payload.message : clientErrorMsg;

                modalShow({
                    title: '알림',
                    message: msg,
                    buttons: 'close'
                });

                return { ok: false, status: e.status, error: e };
            }

            console.error(e);

            modalShow({
                title: '오류',
                message: otherErrorMsg,
                buttons: 'close'
            });

            return { ok: false, status: e.status, error: e };
        }
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
        const finalHeaders = {
            'Accept': 'application/json',
            ...headers
        };

        const init = {
            method,
            headers: finalHeaders,
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

            } else if (finalHeaders['Content-Type'] === 'application/x-www-form-urlencoded;charset=UTF-8') {
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

    // =========================
    // 공통 모달 (#modal-common) 제어
    // =========================

    function getModal() {
        var m = document.getElementById(MODAL_ID);
        if (!m) throw new Error('#' + MODAL_ID + ' not found in layout');
        return m;
    }

    function modalBindOnce() {
        if (MODAL_BOUND) return;

        document.addEventListener('keydown', function (e) {
            var m = getModal();
            if (m.getAttribute('aria-hidden') === 'true') return;
            if (e.key === 'Escape') modalHide();
        });

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
     * @param {Function} [param0.onOk] 확인 클릭 시 콜백
     * @param {Function} [param0.onClose] 닫힐 때 콜백
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

        var titleEl = m.querySelector('#modal-title-basic');
        var bodyEl = m.querySelector('.modal-body');
        if (titleEl) titleEl.textContent = title;
        if (bodyEl) bodyEl.textContent = String(message);

        var btnCancel = m.querySelector('.modal-footer .btn-dark-line');
        var btnOk = m.querySelector('.modal-footer .btn-blue');

        cleanupButtonHandlers(m);

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

        MODAL_PREV_ACTIVE = document.activeElement;
        var container = m.querySelector('.modal-container');
        (container || m).focus();

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

        m._onClose = (typeof onClose === 'function') ? onClose : null;
    }

    function modalHide() {
        var m = getModal();

        m.setAttribute('aria-hidden', 'true');
        m.classList.remove('is-open');
        document.body.classList.remove('modal-open');

        cleanupButtonHandlers(m);

        if (typeof m._onClose === 'function') {
            try {
                m._onClose();
            } finally {
                m._onClose = null;
            }
        }

        if (MODAL_PREV_ACTIVE && typeof MODAL_PREV_ACTIVE.focus === 'function') {
            MODAL_PREV_ACTIVE.focus();
        }
        MODAL_PREV_ACTIVE = null;
    }

    // =========================
    // HTML/테이블/리스트 유틸
    // =========================

    const buildUrl = (base, params = {}) => {
        const u = new URL(base, window.location.origin);
        Object.entries(params).forEach(([k, v]) => {
            if (v !== '??') {
                u.searchParams.set(k, v);
            }
        });
        u.searchParams.set('_', Date.now());
        return u.toString();
    };

    const parseHtml = (html) => new DOMParser().parseFromString(html, 'text/html');

    const swap = (html, selector) => {
        if (!html) return;
        const doc = parseHtml(html);
        const next = doc.querySelector(selector);
        const curr = document.querySelector(selector);
        if (next && curr) curr.replaceWith(next);
    };

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

    const activePage = () => {
        const a = document.querySelector('#grid-pager a.grid-pager[aria-current="page"]');
        if (!a) return 0;

        const u = new URL(a.getAttribute('href') || '', window.location.origin);
        return parseInt(u.searchParams.get('page') || '0', 10) || 0;
    };

    /**
     * 폼에서 현재 입력된 값을 읽음
     * (form element 또는 selector 문자열 모두 지원)
     */
    function collectFromForm(formOrSelector) {
        const params = {};
        let form = null;

        if (!formOrSelector) return params;

        if (formOrSelector instanceof HTMLFormElement) {
            form = formOrSelector;
        } else if (typeof formOrSelector === 'string') {
            form = document.querySelector(formOrSelector);
        }

        if (!form) return params;

        const fd = new FormData(form);
        for (const [k, v] of fd.entries()) {
            if (v != null && String(v).trim() !== '') {
                params[k] = v;
            }
        }
        return params;
    }

    /**
     * name이 list[0].field 형태인 FormData를 JSON으로 변환
     */
    function collectAsJson(formOrScope) {
        const root = formOrScope instanceof Element
            ? formOrScope
            : (document.querySelector(formOrScope) || document);

        const fd = new FormData(root instanceof HTMLFormElement ? root : root.querySelector('form') || root);
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

    /**
     * tbody가 비었으면 "조회 결과 없음" 한 줄
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

        const dataRowCount = tbody.querySelectorAll(rowSelector).length;
        let emptyRow = tbody.querySelector("tr[data-empty-row='true']");

        if (dataRowCount === 0) {
            if (!emptyRow) {
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

                emptyRow = document.createElement('tr');
                emptyRow.setAttribute('data-empty-row', 'true');

                const td = document.createElement('td');
                td.colSpan = span;
                td.className = 'no-data';
                td.textContent = message;

                emptyRow.appendChild(td);
                tbody.appendChild(emptyRow);
            } else {
                const td = emptyRow.cells[0];
                if (td && td.textContent !== message) td.textContent = message;
            }
        } else {
            if (emptyRow) emptyRow.remove();
        }
    }

    function norm(text) {
        return (text || '')
            .replace(/\s+/g, ' ')
            .replace(/\u00A0/g, ' ')
            .trim()
            .toLowerCase();
    }

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

    function rowToObject(tr, indexToKey) {
        const obj = {};
        tr.querySelectorAll('td').forEach((td, i) => {
            const key = indexToKey[i];
            if (key) obj[key] = (td.textContent || '').trim();
        });
        return obj;
    }

    /**
     * 일반 table → { wrapperKey: [ DTO... ] }
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

    function mergeAs(listRows, addedRows, keys) {
        const listKey = (keys && keys.list) ? keys.list : 'list';
        const addedKey = (keys && keys.added) ? keys.added : 'added';

        return {
            [listKey]: Array.isArray(listRows) ? listRows : [],
            [addedKey]: Array.isArray(addedRows) ? addedRows : []
        };
    }

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

        if (syncHeader) {
            syncHeaderCheckBox(fromTbody);
            syncHeaderCheckBox(toTbody);
        }

        return selected.length;
    }

    // =========================
    // 목록 재조회 / 페이징
    // =========================

    let listAbort;

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

    const state = ({
        overrides = {},
        selectSize,
        inputSort,
        inputDir,
        defaultSortColumn,
        applied
    } = {}) => ({
        ...withSortAndSize(applied || {}, overrides, selectSize, inputSort, inputDir, defaultSortColumn)
    });

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



            if (selectSize) selectSize.value = params.size;
            if (inputDir)  inputDir.value  = params.dir;
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

    // =========================
    // 파일 업로드(FormData) 유틸
    // =========================

    const toMultiPart = (data, extensions = []) => {
        const MAX_SIZE = 20 * 1024 * 1024; // 20MB
        const DISALLOW_EMPTY = true;

        const normExts = (extensions || [])
            .map(e => String(e).replace(/\./g, '').toLowerCase());
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

            const onlyFiles = (Array.isArray(files) ? files : []).filter(f => f instanceof File);
            if (onlyFiles.length === 0 && DISALLOW_EMPTY) return '__TOO_MANY__';
            return onlyFiles[0];
        }

        function show(msg) {
            modalShow({ message: msg, buttons: 'close' });
        }

        const src = data || {};
        const fileSel = pickOneFile(src.files || src);

        if (fileSel === '__TOO_MANY__') {
            show('파일은 1개만 업로드할 수 있습니다.');
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

    async function sendExcel(
        url,
        { method = 'POST', data = null, headers = {}, signal } = {}
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
                init.body = new URLSearchParams(data).toString();
            } else {
                init.headers['Content-Type'] = 'application/json;charset=UTF-8';
                init.body = JSON.stringify(data);
            }
        }

        const res = await fetch(url, init);
        return res;
    }

    // =========================
    // 네비게이션/submit 시 로딩 오버레이
    // =========================

    function bindPageLoadingOnNav() {
        // a 태그 클릭
        document.addEventListener('click', function (e) {
            const link = e.target.closest('a[href]');
            if (!link) return;

            // 이미 다른 핸들러에서 막힌 경우 패스
            if (e.defaultPrevented) return;

            // 왼쪽 클릭만
            if (e.button !== 0) return;

            // 새 탭/새 창 열기(Ctrl/Meta/Shift/Alt)면 패스
            if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;

            const href = link.getAttribute('href');
            if (!href) return;

            if (href.startsWith('#') || href.startsWith('javascript:')) return;

            const url = new URL(link.href, window.location.origin);
            if (url.origin !== window.location.origin) return;

            showLoading();
        });

        // form submit 에도 로딩
        document.addEventListener('submit', function (e) {
            const form = e.target;
            if (!(form instanceof HTMLFormElement)) return;

            // 이미 AJAX 핸들러에서 e.preventDefault() 한 경우 → 로딩 X
            if (e.defaultPrevented) return;

            // 새 창(target="_blank" 등)은 패스
            if (form.target && form.target !== '_self') return;

            showLoading();
        });
    }

    // DOMContentLoaded 이후 네비게이션/submit 로딩 바인딩
    document.addEventListener('DOMContentLoaded', bindPageLoadingOnNav);

    // =========================
    // 글로벌 Common 객체
    // =========================
    global.Common = Object.freeze({
        // fetch & HTTP
        sendSafe,
        fetchHtml,
        reloadList,

        // 폼/테이블
        collectFromForm,
        collectAsJson,
        collectTablePayload,
        collectSectionRows,
        syncHeaderCheckBox,
        syncEmptyRow,
        mergeAs,
        moveSelectedRows,

        // 모달
        modalShow,
        closeModal,
        openModal,

        // Excel
        bindExcelImport,
        bindExcelExport,

        // 파일 전송
        toMultiPart,

        // HTML 교체
        swap,

        // 로딩
        showLoading,
        hideLoading
    });

})(window);
// =========================
// 초기 페이지 로딩 스피너
// =========================
(function initialPageLoading() {
    //  공통 show/hide 래퍼
    function safeShow() {
        try {
            if (window.Common && typeof window.Common.showLoading === 'function') {
                window.Common.showLoading();
            }
        } catch (e) {
            console.error('[initialPageLoading] show error:', e);
        }
    }

    function safeHide() {
        try {
            if (window.Common && typeof window.Common.hideLoading === 'function') {
                window.Common.hideLoading();
            } else {
                // ✅ 최후 수단: DOM 레벨에서 강제로 감추기
                var el = document.getElementById('mini-sp-loading');
                if (el) {
                    el.style.display = 'none';
                    el.setAttribute('aria-hidden', 'true');
                }
            }
        } catch (e) {
            console.error('[initialPageLoading] hide error:', e);
            var el = document.getElementById('mini-sp-loading');
            if (el) {
                el.style.display = 'none';
                el.setAttribute('aria-hidden', 'true');
            }
        }
    }

    // 페이지 진입 시 로더 켜기
    //   (head 의 기본 CSS 때문에 이미 보이긴 하지만,
    //    여기서 한 번 더 Common.ensureLoadingDom 을 태워서
    //    TMONEY 스타일로 맞춰주는 효과도 있음)
    safeShow();

    let doneCalled = false;
    function done() {
        if (doneCalled) return;
        doneCalled = true;

        window.removeEventListener('load', done);
        safeHide();
    }

    // 이미 모든 리소스가 로드된 상태라면 바로 종료
    if (document.readyState === 'complete') {
        done();
    } else {
        // 그렇지 않다면 CSS/JS/이미지까지 모두 로딩된 뒤 종료
        window.addEventListener('load', done);
    }

    // ⚠ 혹시 load 이벤트를 못 받는 비정상 상황 (JS 에러/중간 abort 등) 대비 타임아웃
    setTimeout(done, 15000); // 15초 지나면 강제로 숨김
})();