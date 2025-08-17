function startOverlay() {
    // 오버레이 컨테이너
    const overlay = document.createElement("div");
    overlay.className = "loading-overlay";

    // 글자 하나씩 추가
    const texts = ["교", "통", "복", "지", "포", "탈"];
    texts.forEach(t => {
        const span = document.createElement("span");
        span.className = "loading-text";
        span.textContent = t;
        overlay.appendChild(span);
    });

    document.body.appendChild(overlay);
}

function endOverlay() {
    const overlay = document.querySelector(".loading-overlay");
    if (!overlay) return;

    // CSS 트랜지션 활용 (opacity 1 → 0)
    overlay.style.transition = "opacity 0.3s ease";
    overlay.style.opacity = "0";

    // 애니메이션 끝나면 DOM 제거
    overlay.addEventListener("transitionend", () => overlay.remove(), { once: true });
}
