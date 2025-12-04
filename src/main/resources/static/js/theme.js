document.addEventListener('DOMContentLoaded', function () {
  const KEY = 'theme';
  const root = document.documentElement;
  const toggle = document.getElementById('theme-toggle');

  // 초기 체크박스 상태 동기화
  const current = root.getAttribute('data-theme') || 'light';
  if (toggle) toggle.checked = current === 'dark';

  // 토글 변경 → 테마 적용 + 저장
  if (toggle) {
    toggle.addEventListener('change', function () {
      const next = this.checked ? 'dark' : 'light';
      root.setAttribute('data-theme', next);
      try { localStorage.setItem(KEY, next); } catch (e) {}
    });
  }

  // 시스템 선호도 변경 시(옵션) 자동 추적하려면 아래 주석 해제
  // const mq = window.matchMedia('(prefers-color-scheme: dark)');
  // mq.addEventListener('change', (e) => {
  //   const stored = localStorage.getItem(KEY);
  //   if (!stored) { // 사용자가 강제 지정하지 않은 경우만 시스템 따라감
  //     const next = e.matches ? 'dark' : 'light';
  //     root.setAttribute('data-theme', next);
  //   }
  // });
});
