/* ========================================================================
 * datepick.js (Vanilla JS)
 * - 타입: data-dp-type="date|month|year"  (기본: date)
 * - date: YYYY-MM-DD / month: MM / year: YYYY
 * - 버튼([data-dp-toggle])으로만 팝업 열기/닫기
 * - date 타입에서 단일/범위 선택 지원(data-dp="range", data-role="start|end", data-dp-group)
 * - 팝업은 body 포털 + position:fixed, flipY/clamp 배치
 * - Alt+↓로 열기, ESC/바깥 클릭으로 닫기
 * - 스크롤해도 닫히지 않도록 KEEP_OPEN_ON_SCROLL 적용
 * - 키보드 내비 지원
 * ======================================================================== */
(function () {
  /* ---------- 옵션 ---------- */
  const KEEP_OPEN_ON_SCROLL = true; // 스크롤해도 닫지 않음(위치만 재배치)

  /* ---------- 유틸 ---------- */
  const pad = (n) => String(n).padStart(2, "0");
  const fmtDate  = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  const fmtMonth = (d) => `${pad(d.getMonth() + 1)}`; // ✅ 월만(mm)
  const fmtYear  = (d) => `${d.getFullYear()}`;

  const parseDate = (s) => {
    const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec((s || "").trim());
    if (!m) return null;
    const d = new Date(+m[1], +m[2] - 1, +m[3]);
    if (d.getFullYear() !== +m[1] || d.getMonth() !== +m[2] - 1 || d.getDate() !== +m[3]) return null;
    return d;
  };
  // ✅ MM만 허용(01~12), 내부 계산용으로 임의년(2000) 부여하되 값에는 쓰지 않음
  const parseMonth = (s) => {
    const m = /^(\d{2})$/.exec((s || "").trim());
    if (!m) return null;
    const mon = +m[1] - 1;
    if (mon < 0 || mon > 11) return null;
    return new Date(2000, mon, 1);
  };
  const parseYear = (s) => {
    const m = /^(\d{4})$/.exec((s || "").trim());
    if (!m) return null;
    const y = +m[1];
    const d = new Date(y, 0, 1);
    if (d.getFullYear() !== y) return null;
    return d;
  };

  const sameDay = (a, b) =>
    !!a && !!b &&
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate();
  const cmp = (a, b) => a.getTime() - b.getTime();
  const today = () => {
    const t = new Date();
    return new Date(t.getFullYear(), t.getMonth(), t.getDate());
  };
  const getType = (input) => {
    const t = (input?.dataset.dpType || 'date').toLowerCase();
    return (t === 'month' || t === 'year') ? t : 'date';
  };

  /* ---------- 전역 상태 ---------- */
  let activeInput = null;
  let activeField = null;
  let suppressOpenByPointer = false;
  let viewYM = (() => { const t = new Date(); return { y: t.getFullYear(), m: t.getMonth() }; })();
  const rangeStore = new Map();

  // 팝업(단일 인스턴스)
  const pop = window.__DP_POP__ || createPopupOnce();
  window.__DP_POP__ = pop;

  // 스크롤 부모 추적
  let scrollParents = [];

  /* ---------- 스크롤 유틸 ---------- */
  function getScrollParents(el) {
    const res = [];
    if (!el) return res;
    let p = el.parentElement;
    const anyScroll = /(auto|scroll|overlay)/;
    const isScrollable = (node) => {
      const cs = getComputedStyle(node);
      const hasScrollProp =
        anyScroll.test(cs.overflow) || anyScroll.test(cs.overflowX) || anyScroll.test(cs.overflowY);
      const hasScrollableSize =
        node.scrollHeight > node.clientHeight || node.scrollWidth > node.clientWidth;
      return hasScrollProp && hasScrollableSize;
    };
    while (p) { if (isScrollable(p)) res.push(p); p = p.parentElement; }
    res.push(document);
    return res;
  }
  function bindScrollParents() {
    cleanupScrollParents();
    scrollParents = getScrollParents(activeField);
    scrollParents.forEach(sp => sp.addEventListener('scroll', onRelayout, { passive: true }));
  }
  function cleanupScrollParents() {
    if (!scrollParents || !scrollParents.length) return;
    scrollParents.forEach(sp => sp.removeEventListener('scroll', onRelayout));
    scrollParents = [];
  }
  function isFieldVisible(field) {
    const r = field.getBoundingClientRect();
    return r.bottom > 0 && r.top < window.innerHeight && r.right > 0 && r.left < window.innerWidth;
  }

  /* ---------- 팝업 생성 ---------- */
  function createPopupOnce() {
    const el = document.createElement("div");
    el.id = "dp-pop";
    el.className = "dp-pop";
    el.setAttribute("role", "dialog");
    el.setAttribute("aria-modal", "true");
    el.setAttribute("aria-label", "선택");
    el.hidden = true;
    el.innerHTML = `
      <div class="dp-head">
        <button class="nav prev" type="button" aria-label="이전">&#x2039;</button>
        <div class="title" aria-live="polite"></div>
        <button class="nav next" type="button" aria-label="다음">&#x203A;</button>
      </div>
      <div class="dp-grid">
        <div class="dp-week"><span>일</span><span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span></div>
        <div class="dp-days" role="rowgroup"></div>
      </div>
    `;
    document.body.appendChild(el);
    el.style.position = "fixed";

    // prev/next: 타입에 따라 동작 분기 (month 타입은 버튼 비활성화/무동작)
    el.querySelector(".prev").addEventListener("click", () => {
      const tp = getType(window.__DP_ACTIVE_INPUT__);
      if (tp === 'date') shiftMonth(-1);
      else if (tp === 'year') shiftYearPage(-12);
      // month: no-op
    });
    el.querySelector(".next").addEventListener("click", () => {
      const tp = getType(window.__DP_ACTIVE_INPUT__);
      if (tp === 'date') shiftMonth(+1);
      else if (tp === 'year') shiftYearPage(+12);
      // month: no-op
    });
    return el;
  }
  function shiftMonth(delta) {
    viewYM.m += delta;
    while (viewYM.m < 0) { viewYM.m += 12; viewYM.y--; }
    while (viewYM.m > 11) { viewYM.m -= 12; viewYM.y++; }
    render();
  }
  function shiftYearPage(deltaYears) { viewYM.y += deltaYears; render(); }

  /* ---------- 입력 마스킹 ---------- */
  (function inputMasking() {
    const SEL = '.text-field.datepicker .dp-input';
    document.querySelectorAll(SEL).forEach(inp => {
      inp.setAttribute('autocomplete', 'off');
      inp.setAttribute('inputmode', 'numeric');
    });

    function onInput(e){
      const el = e.target;
      if (!el.matches(SEL)) return;
      const tp = getType(el);
      let after = el.value;

      if (tp === 'date'){
        const digits = (after||'').replace(/\D/g,'').slice(0,8);
        if (digits.length <= 4) after = digits;
        else if (digits.length <= 6) after = digits.slice(0,4)+'-'+digits.slice(4);
        else after = digits.slice(0,4)+'-'+digits.slice(4,6)+'-'+digits.slice(6);
        el.setAttribute('maxlength','10');
        el.setAttribute('pattern','\\d{4}-\\d{2}-\\d{2}');
      } else if (tp === 'month'){
        const digits = (after||'').replace(/\D/g,'').slice(0,2);
        after = digits;
        el.setAttribute('maxlength','2');     // ✅ MM
        el.setAttribute('pattern','\\d{2}');
      } else { // year
        const digits = (after||'').replace(/\D/g,'').slice(0,4);
        after = digits;
        el.setAttribute('maxlength','4');
        el.setAttribute('pattern','\\d{4}');
      }
      if (el.value !== after){
        el.value = after;
        try { el.setSelectionRange(after.length, after.length); } catch(_) {}
      }
    }

    function onPaste(e){
      const el = e.target;
      if (!el.matches(SEL)) return;
      const tp = getType(el);
      const txt = (e.clipboardData || window.clipboardData).getData('text') || '';
      let after = txt;

      if (tp === 'date'){
        const digits = txt.replace(/\D/g,'').slice(0,8);
        if (digits.length <= 4) after = digits;
        else if (digits.length <= 6) after = digits.slice(0,4)+'-'+digits.slice(4);
        else after = digits.slice(0,4)+'-'+digits.slice(4,6)+'-'+digits.slice(6);
      } else if (tp === 'month'){
        after = txt.replace(/\D/g,'').slice(0,2); // ✅ MM
      } else {
        after = txt.replace(/\D/g,'').slice(0,4);
      }

      e.preventDefault();
      el.value = after;
      try { el.setSelectionRange(after.length, after.length); } catch(_) {}
      el.dispatchEvent(new Event('input', { bubbles: true }));
    }

    function onKeydown(e){
      const el = e.target;
      if (!el.matches(SEL)) return;
      const allowed = ['Backspace','Delete','Tab','Enter','Escape','ArrowLeft','ArrowRight','Home','End'];
      const isNumber = e.key >= '0' && e.key <= '9';
      const isHyphen = e.key === '-';
      const tp = getType(el);
      if (allowed.includes(e.key)) return;
      if (tp === 'date') { if (isNumber || isHyphen) return; }
      else { if (isNumber) return; }
      if ((e.ctrlKey||e.metaKey) && ['a','c','v','x','A','C','V','X'].includes(e.key)) return;
      e.preventDefault();
    }

    document.addEventListener('input', onInput, true);
    document.addEventListener('paste', onPaste, true);
    document.addEventListener('keydown', onKeydown, true);
  })();

  /* ---------- 포인터 캡처 ---------- */
  document.addEventListener("mousedown", (e) => {
    const field = e.target.closest(".text-field.datepicker");
    if (!field) return;
    const isToggle = e.target.closest("[data-dp-toggle]");
    if (!isToggle) suppressOpenByPointer = true;
  }, true);
  ["mouseup","dragend","touchend","pointerup"].forEach(ev =>
    document.addEventListener(ev, () => setTimeout(() => (suppressOpenByPointer = false), 0), true)
  );

  /* ---------- 토글/닫기 ---------- */
  document.addEventListener("click", (e) => {
    const toggle = e.target.closest("[data-dp-toggle]");
    if (toggle) {
      const field = toggle.closest(".text-field.datepicker");
      const input = field?.querySelector(".dp-input");
      if (!input) return;
      if (pop.hidden || activeInput !== input) openFor(input);
      else closePop();
      return;
    }
    const clickInsidePop = !!e.target.closest(".dp-pop");
    const clickInsideActiveField =
      !!activeField && !!e.target.closest(".text-field.datepicker") && activeField.contains(e.target);
    if (!clickInsidePop && !clickInsideActiveField) closePop();
  });

  document.addEventListener("keydown", (e) => {
    const inputEl = e.target.closest(".dp-input");
    if (!inputEl || !inputEl.closest(".text-field.datepicker")) return;
    if (e.altKey && e.key === "ArrowDown") { e.preventDefault(); openFor(inputEl); }
    if (e.key === "Escape") closePop();
  });

  /* ---------- 열기/닫기/위치 ---------- */
  function openFor(input) {
    if (suppressOpenByPointer) return;
    const field = input.closest(".text-field.datepicker");
    if (!field) return;

    activeInput = input;
    activeField = field;
    window.__DP_ACTIVE_INPUT__ = activeInput;
    window.__DP_ACTIVE_FIELD__ = activeField;

    if (!document.body.contains(pop)) document.body.appendChild(pop);
    pop.style.position = "fixed";

    field.classList.add("is-open");
    syncExpanded(true);
    pop.hidden = false;

    const tp = getType(input);
    let d;
    if (tp === 'date')      d = parseDate(input.value)  || today();
    else if (tp === 'month') d = parseMonth(input.value) || today(); // 내부 계산용 mm→Date
    else                     d = parseYear(input.value)  || today();
    viewYM = { y: d.getFullYear(), m: d.getMonth() };

    render();
    positionPopupFixed(field);

    const focusBtn =
      pop.querySelector('.dp-day.is-selected') ||
      pop.querySelector('.dp-month.is-selected') ||
      pop.querySelector('.dp-year.is-selected') ||
      pop.querySelector('.dp-day,[tabindex="0"]') ||
      pop.querySelector('.dp-month,[tabindex="0"]') ||
      pop.querySelector('.dp-year,[tabindex="0"]');
    focusBtn?.focus();

    window.addEventListener("resize", onRelayout, { passive: true });
    window.addEventListener("scroll", onRelayout, { passive: true });
    bindScrollParents();
  }

  function closePop() {
    if (!activeInput) { pop.hidden = true; return; }
    syncExpanded(false);
    activeField?.classList.remove("is-open");
    pop.hidden = true;
    pop.classList.remove("flipY");
    activeField?.querySelector("[data-dp-toggle]")?.focus();
    activeInput = null; activeField = null;
    window.__DP_ACTIVE_INPUT__ = null; window.__DP_ACTIVE_FIELD__ = null;
    window.removeEventListener("resize", onRelayout);
    window.removeEventListener("scroll", onRelayout);
    cleanupScrollParents();
  }

  function syncExpanded(expanded) {
    const btn = activeField?.querySelector("[data-dp-toggle]");
    btn?.setAttribute("aria-expanded", expanded ? "true" : "false");
  }

  function onRelayout() {
    if (!activeField || pop.hidden) return;
    if (!KEEP_OPEN_ON_SCROLL) { if (!isFieldVisible(activeField)) { closePop(); return; } }
    positionPopupFixed(activeField);
  }

  function positionPopupFixed(field) {
    const r = field.getBoundingClientRect();
    const popW = pop.offsetWidth || 328;
    const popH = pop.offsetHeight || 320;
    const margin = 6;
    let top = r.bottom + margin;
    let left = r.left;
    const maxLeft = Math.max(4, window.innerWidth - popW - 4);
    left = Math.min(Math.max(left, 4), maxLeft);
    const spaceBelow = window.innerHeight - (r.bottom + margin);
    if (spaceBelow < popH) { top = Math.max(4, r.top - popH - margin); pop.classList.add("flipY"); }
    else { pop.classList.remove("flipY"); }
    pop.style.left = `${Math.round(left)}px`;
    pop.style.top  = `${Math.round(top)}px`;
  }

  /* ---------- 렌더 ---------- */
  function render() {
    const y = viewYM.y;
    const m = viewYM.m;
    const tp = getType(activeInput);
    const daysWrap = pop.querySelector(".dp-days");
    daysWrap.innerHTML = "";

    // ✅ 타입별 grid 컬럼 변경
    if (tp === "year") {
      daysWrap.style.display = "grid";
      daysWrap.style.gridTemplateColumns = "repeat(5, 1fr)";
    } else if (tp === "month") {
      daysWrap.style.display = "grid";
      daysWrap.style.gridTemplateColumns = "repeat(4, 1fr)";
    } else {
      daysWrap.style.display = "grid";
      daysWrap.style.gridTemplateColumns = "repeat(7, 1fr)";
    }

    // 주 헤더는 date 타입에서만 표시
    pop.querySelector(".dp-week").style.display = (tp === 'date') ? '' : 'none';

    // 타이틀 & 네비 버튼 표시 제어
    const titleEl = pop.querySelector(".title");
    const prevEl  = pop.querySelector(".prev");
    const nextEl  = pop.querySelector(".next");
    if (tp === 'date') {
      titleEl.textContent = `${y}년 ${m + 1}월`;
      prevEl.style.visibility = nextEl.style.visibility = '';
    } else if (tp === 'month') {
      titleEl.textContent = `월 선택`;          // ✅ 년도 정보 제거
      prevEl.style.visibility = nextEl.style.visibility = 'hidden'; // ✅ 네비 숨김
    } else { // year
      const base = Math.floor(y / 12) * 12;
      titleEl.textContent = `${base}–${base + 11}년`;
      prevEl.style.visibility = nextEl.style.visibility = '';
    }

    if (tp === 'date')      renderDays(y, m, daysWrap);
    else if (tp === 'month') renderMonths(daysWrap);
    else                     renderYears(y, daysWrap);
  }

  // Day 렌더
  function renderDays(y, m, wrap){
    const mode = getMode(activeInput);
    const groupId = activeInput?.dataset.dpGroup;
    const grp = groupId ? (rangeStore.get(groupId) || { start: null, end: null }) : { start: null, end: null };
    const first = new Date(y, m, 1);
    const startW = first.getDay();
    const lastDate = new Date(y, m + 1, 0).getDate();
    const prevLast = new Date(y, m, 0).getDate();
    const cells = [];
    for (let i = 0; i < startW; i++) {
      const d = new Date(y, m - 1, prevLast - startW + i + 1);
      cells.push(makeDayCell(d, true, mode, grp));
    }
    for (let i = 1; i <= lastDate; i++) {
      const d = new Date(y, m, i);
      cells.push(makeDayCell(d, false, mode, grp));
    }
    const remain = 42 - cells.length;
    for (let i = 1; i <= remain; i++) {
      const d = new Date(y, m + 1, i);
      cells.push(makeDayCell(d, true, mode, grp));
    }
    cells.forEach(c => wrap.appendChild(c));
  }

  // ✅ Month 렌더 (년도 없음, 1~12만)
  function renderMonths(wrap){
    const curr = parseMonth(activeInput?.value);
    for (let i=0; i<12; i++){
      const d = new Date(2000, i, 1); // 내부 계산용
      const btn = document.createElement('button');
      btn.type='button';
      btn.className = 'dp-month';
      btn.setAttribute('role','gridcell');
      btn.textContent = (i+1) + '월';
      if (curr && curr.getMonth()===d.getMonth()){
        btn.classList.add('is-selected');
        btn.setAttribute('tabindex','0');
      } else {
        btn.setAttribute('tabindex', (i===0 ? '0' : '-1'));
      }
      btn.addEventListener('click', () => onPickMonth(d));
      btn.addEventListener('keydown', onMonthKey(d));
      wrap.appendChild(btn);
    }
  }

  // Year 렌더 (12개 묶음)
  function renderYears(y, wrap){
    const base = Math.floor(y/12)*12;
    const curr = parseYear(activeInput?.value);
    for (let i=0; i<12; i++){
      const d = new Date(base+i, 0, 1);
      const btn = document.createElement('button');
      btn.type='button';
      btn.className='dp-year';
      btn.setAttribute('role','gridcell');
      btn.textContent = `${d.getFullYear()}년`;
      if (curr && curr.getFullYear() === d.getFullYear()){
        btn.classList.add('is-selected');
        btn.setAttribute('tabindex','0');
      } else {
        btn.setAttribute('tabindex', (i===0 ? '0' : '-1'));
      }
      btn.addEventListener('click', () => onPickYear(d));
      btn.addEventListener('keydown', onYearKey(d));
      wrap.appendChild(btn);
    }
  }

  /* ---------- 셀/선택 ---------- */
  function makeDayCell(d, out, mode, grp) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "dp-day";
    btn.setAttribute("role", "gridcell");
    btn.textContent = d.getDate();
    if (out) btn.dataset.out = "1";
    btn.setAttribute("tabindex", sameDay(d, today()) ? "0" : "-1");
    const curr = parseDate(activeInput?.value);
    if (mode === "single" && sameDay(d, curr)) {
      btn.classList.add("is-selected"); btn.setAttribute("tabindex", "0");
    }
    if (mode === "range") {
      const { start, end } = grp;
      if (start && end && cmp(start, d) <= 0 && cmp(d, end) <= 0) btn.classList.add("in-range");
      if (start && sameDay(d, start)) btn.classList.add("range-start", "is-selected");
      if (end && sameDay(d, end))   btn.classList.add("range-end",   "is-selected");
      if ((start && sameDay(d, start)) || (end && sameDay(d, end))) btn.setAttribute("tabindex","0");
    }
    btn.addEventListener("click", () => onPickDate(d));
    btn.addEventListener("keydown", onDayKey(d));
    return btn;
  }

  function onPickDate(date) {
    const mode = getMode(activeInput);
    if (mode === "single") {
      activeInput.value = fmtDate(date);
      activeInput.dispatchEvent(new Event("change"));
      closePop();
      return;
    }
    const groupId = activeInput.dataset.dpGroup;
    const role = activeInput.dataset.role;
    if (!groupId || !role) return;
    const state = rangeStore.get(groupId) || { start: null, end: null };
    if (role === "start") { state.start = date; if (state.end && cmp(state.start, state.end) > 0) state.end = null; }
    else { state.end = date; if (state.start && cmp(state.end, state.start) < 0) { const t = state.start; state.start = state.end; state.end = t; } }
    rangeStore.set(groupId, state);
    syncRangeInputs(groupId, state);
    if (state.start && state.end) closePop(); else render();
  }

  // ✅ month: MM만 세팅
  function onPickMonth(date){
    activeInput.value = fmtMonth(date); // "MM"
    activeInput.dispatchEvent(new Event("change"));
    closePop();
  }

  function onPickYear(date){
    activeInput.value = fmtYear(date);
    activeInput.dispatchEvent(new Event("change"));
    closePop();
  }

  /* ---------- 키보드 내비 ---------- */
  function onDayKey(baseDate) {
    return (e) => {
      let d = new Date(baseDate);
      const k = e.key;
      if (k === "ArrowLeft") d.setDate(d.getDate() - 1);
      else if (k === "ArrowRight") d.setDate(d.getDate() + 1);
      else if (k === "ArrowUp") d.setDate(d.getDate() - 7);
      else if (k === "ArrowDown") d.setDate(d.getDate() + 7);
      else if (k === "PageUp") d.setMonth(d.getMonth() - 1);
      else if (k === "PageDown") d.setMonth(d.getMonth() + 1);
      else if (k === "Home") d.setDate(1);
      else if (k === "End") d.setMonth(d.getMonth() + 1, 0);
      else if (k === "Enter" || k === " ") { onPickDate(baseDate); return; }
      else return;
      e.preventDefault();
      viewYM = { y: d.getFullYear(), m: d.getMonth() };
      render();
      const focusBtn = [...pop.querySelectorAll(".dp-day")].find(
        (b) => Number(b.textContent) === d.getDate() && !b.dataset.out
      );
      focusBtn && focusBtn.focus();
    };
  }

  function onMonthKey(baseDate){
    return (e) => {
      let d = new Date(baseDate);
      const k = e.key;
      if (k === 'ArrowLeft') d.setMonth(d.getMonth()-1);
      else if (k === 'ArrowRight') d.setMonth(d.getMonth()+1);
      else if (k === 'ArrowUp') d.setMonth(d.getMonth()-3);
      else if (k === 'ArrowDown') d.setMonth(d.getMonth()+3);
      else if (k === 'Home') d.setMonth(0);
      else if (k === 'End') d.setMonth(11);
      else if (k === 'Enter' || k === ' ') { onPickMonth(baseDate); return; }
      else return;
      e.preventDefault();
      viewYM = { y: d.getFullYear(), m: d.getMonth() };
      render();
      const focusBtn = [...pop.querySelectorAll(".dp-month")][d.getMonth()];
      focusBtn && focusBtn.focus();
    };
  }

  function onYearKey(baseDate){
    return (e) => {
      let d = new Date(baseDate);
      const k = e.key;
      if (k === 'ArrowLeft') d.setFullYear(d.getFullYear()-1);
      else if (k === 'ArrowRight') d.setFullYear(d.getFullYear()+1);
      else if (k === 'ArrowUp') d.setFullYear(d.getFullYear()-3);
      else if (k === 'ArrowDown') d.setFullYear(d.getFullYear()+3);
      else if (k === 'PageUp') d.setFullYear(d.getFullYear()-12);
      else if (k === 'PageDown') d.setFullYear(d.getFullYear()+12);
      else if (k === 'Home') d = new Date(Math.floor(d.getFullYear()/12)*12, 0, 1);
      else if (k === 'End')  d = new Date(Math.floor(d.getFullYear()/12)*12 + 11, 0, 1);
      else if (k === 'Enter' || k === ' ') { onPickYear(baseDate); return; }
      else return;
      e.preventDefault();
      viewYM = { y: d.getFullYear(), m: 0 };
      render();
      const base = Math.floor(viewYM.y/12)*12;
      const idx = d.getFullYear() - base;
      const btns = [...pop.querySelectorAll(".dp-year")];
      (btns[idx] || btns[0])?.focus();
    };
  }

  /* ---------- 직접 입력(blur 검증/정규화) ---------- */
  document.addEventListener("blur", (e) => {
    const inp = e.target.closest(".dp-input");
    if (!inp) return;
    const v = (inp.value || "").trim();
    if (!v) return;

    const tp = getType(inp);
    let d = null;

    if (tp === 'date')  d = parseDate(v);
    else if (tp === 'month') d = parseMonth(v);
    else d = parseYear(v);

    if (!d) { inp.value = ""; return; }

    if (tp === 'date')      inp.value = fmtDate(d);
    else if (tp === 'month') inp.value = fmtMonth(d); // ✅ "MM"로 정규화
    else                     inp.value = fmtYear(d);

    // date-range 상호관계 갱신
    if (tp === 'date') {
      const gid = inp.dataset.dpGroup;
      const role = inp.dataset.role;
      if (gid && role) {
        const state = rangeStore.get(gid) || { start: null, end: null };
        state[role] = d;
        if (state.start && state.end && cmp(state.start, state.end) > 0) {
          const t = state.start; state.start = state.end; state.end = t;
        }
        rangeStore.set(gid, state);
        syncRangeInputs(gid, state);
      }
    }
  }, true);

  /* ---------- 모드 판단 / 동기화 ---------- */
  function getMode(input) {
    const rangeHost = input.closest('[data-dp="range"]');
    return rangeHost ? "range" : "single";
  }
  function syncRangeInputs(groupId, state) {
    const inputs = document.querySelectorAll(`.dp-input[data-dp-group="${CSS.escape(groupId)}"]`);
    inputs.forEach(inp => {
      const role = inp.dataset.role;
      if (role === "start" && state.start) inp.value = fmtDate(state.start);
      if (role === "end"   && state.end)   inp.value = fmtDate(state.end);
    });
  }

  /* ---------- 외부 제어 API ---------- */
  window.DP = { open: (inputEl) => openFor(inputEl), close: () => closePop() };
})();