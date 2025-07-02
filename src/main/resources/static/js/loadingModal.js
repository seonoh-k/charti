export function showLoadingModal(message = "처리 중입니다...") {
  const modal = document.getElementById("loading-modal");
  const messageEl = document.getElementById("loading-message");
  const spinnerEl = document.getElementById("loading-spinner");

  if (!modal || !messageEl || !spinnerEl) return;

  messageEl.textContent = message;
  spinnerEl.classList.remove("hidden");
  modal.classList.remove("hidden");
}

export function hideLoadingModal() {
  const modal = document.getElementById("loading-modal");
  if (modal) modal.classList.add("hidden");
}

export function showErrorModal(message = "에러", onClose) {
  const modal = document.getElementById("loading-modal");
  const messageEl = document.getElementById("loading-message");
  const spinnerEl = document.getElementById("loading-spinner");
  const errorIconEl = document.getElementById("error-icon");

  if (!modal || !messageEl || !spinnerEl || !errorIconEl) return;
  // 1초간 로딩 상태 유지 → 자연스러운 전환
  setTimeout(() => {
    spinnerEl.classList.add("hidden");         // 로딩 스피너 숨김
    errorIconEl.classList.remove("hidden");   // ❌ 아이콘 표시
    messageEl.textContent = message;
   // 1.5초 뒤에 모달 닫기 + 상태 초기화
    setTimeout(() => {
      hideLoadingModal();
      spinnerEl.classList.remove("hidden");
      errorIconEl.classList.add("hidden");
      messageEl.textContent = "처리 중입니다...";

      if (typeof onClose === "function") onClose();
    }, 1500);
  }, 1000);
}

export function showSuccessModal(message = "가입 완료!", onClose) {
  const modal = document.getElementById("loading-modal");
  const messageEl = document.getElementById("loading-message");
  const spinnerEl = document.getElementById("loading-spinner");
  const successIconEl = document.getElementById("success-icon");

  if (!modal || !messageEl || !spinnerEl || !successIconEl) return;

  spinnerEl.classList.add("hidden");
  successIconEl.classList.remove("hidden");
  messageEl.textContent = message;

  // 1.5초 후 모달 닫고 콜백 실행
  setTimeout(() => {
    hideLoadingModal();
    spinnerEl.classList.remove("hidden");
    successIconEl.classList.add("hidden");
    messageEl.textContent = "처리 중입니다...";
    if (typeof onClose === "function") onClose();
  }, 1500);
}


