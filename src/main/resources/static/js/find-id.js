// 정규표현식
const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
const emailAuthRegex = /^[0-9]+$/;

// 폼 데이터
let formData = {
  email: "",
  verificationCode: "",
};

// 에러 상태
let errors = {
  email: [],
  verificationCode: [],
};

// 인증번호 전송 상태
let isCodeSent = false;
let isEmailVerified = false;

// DOM 요소
const emailInput = document.getElementById("email");
const verificationCodeInput = document.getElementById("verificationCode");
const errorMessageDiv = document.getElementById("errorMessage");
const sendCodeBtn = document.getElementById("sendCodeBtn");
const verifyCodeBtn = document.getElementById("verifyCodeBtn");
const labelVerification = document.getElementById("labelVerification");
const sectionEmail = document.querySelector(".section-email");

// 입력 변경 핸들러
function handleInputChange(field, value) {
  formData[field] = value;
}

// blur 핸들러
function handleBlur(field) {
  const value = formData[field].trim();

  if (field === "email") {
    if (value && !emailRegex.test(value)) {
      errors.email = ["올바른 이메일 형식으로 입력해 주세요!"];
    } else {
      errors.email = [];
    }
  }

  updateUI();
}

// 인증번호 전송 핸들러
function handleSendCode() {
  const email = formData.email.trim();

  if (!email) {
    errors.email = ["이메일을 입력해주세요!"];
    errors.verificationCode = [];
    updateUI();
    return;
  }

  if (!emailRegex.test(email)) {
    errors.email = ["올바른 이메일 형식으로 입력해 주세요!"];
    errors.verificationCode = [];
    updateUI();
    return;
  }

  errors.email = [];
  errors.verificationCode = [];

  $.post("/api/email/findId", { email: email }, function (res) {
    if (res === "TOO_FAST") {
      errors.email = ["잠시 후 다시 시도해주세요."];
      errors.verificationCode = [];
    } else if (res === "FAIL") {
      errors.email = ["유효하지 않은 이메일 형식입니다."];
      errors.verificationCode = [];
      updateUI();
      return;
    } else {
      errors.email = [
        '<span class="success-message">인증코드가 이메일로 전송되었습니다.</span>',
      ];
      errors.verificationCode = [];
      isCodeSent = true;
      sendCodeBtn.querySelector(".btn-text").textContent = "재전송";
    }
    updateUI();
  }).fail(function () {
    errors.email = ["서버와 통신 중 오류가 발생했습니다."];
    errors.verificationCode = [];
    updateUI();
  });
}

// 인증번호 확인 핸들러
function handleVerifyCode() {
  const code = formData.verificationCode.trim();

  if (!code) {
    errors.email = [];
    errors.verificationCode = ["인증코드를 입력해주세요."];
    updateUI();
    return;
  }

  if (!emailAuthRegex.test(code)) {
    errors.email = [];
    errors.verificationCode = ["숫자만 입력 가능합니다!"];
    updateUI();
    return;
  }

  errors.email = [];
  errors.verificationCode = [];

  $.post(
    "/api/email/findIdCheck",
    { code: code, email: formData.email },
    function (res) {
      if (res === "FAIL") {
        errors.email = [];
        errors.verificationCode = ["인증코드가 일치하지 않습니다."];
      } else if (res === "TIMEOUT") {
        errors.email = [];
        errors.verificationCode = [
          "세션이 만료 되었습니다. 이메일 인증을 다시 시도해주세요.",
        ];
        updateUI();
        return;
      } else if (res === "NOT_MATCHED_EMAIL") {
        errors.email = [];
        errors.verificationCode = [
          "이메일이 일치하지 않습니다. 이메일 인증을 다시 시도해주세요.",
        ];
      } else {
        window.location.href = "/mem/findMemberId";
        return;
      }
      updateUI();
    }
  ).fail(function () {
    errors.email = [];
    errors.verificationCode = ["서버와 통신 중 오류가 발생했습니다."];
    updateUI();
  });
}

// UI 업데이트
function updateUI() {
  // 에러 메시지 수집
  const allErrors = [...errors.email, ...errors.verificationCode];

  // 에러 메시지 표시
  if (allErrors.length > 0) {
    errorMessageDiv.classList.add("visible");
    errorMessageDiv.innerHTML = allErrors
      .map((err) => `<div>${err}</div>`)
      .join("");
  } else {
    errorMessageDiv.classList.remove("visible");
    errorMessageDiv.innerHTML = "";
  }

  // 인증번호 필드 표시/숨김 (영역은 항상 차지)
  if (isCodeSent) {
    labelVerification.style.display = "flex";
    verificationCodeInput.style.display = "flex";
    verifyCodeBtn.style.display = "block";
    sectionEmail.classList.add("expanded");
  } else {
    labelVerification.style.display = "none";
    verificationCodeInput.style.display = "none";
    verifyCodeBtn.style.display = "none";
    sectionEmail.classList.remove("expanded");
  }
}

// 이벤트 리스너 등록
emailInput.addEventListener("input", (e) =>
  handleInputChange("email", e.target.value)
);
emailInput.addEventListener("blur", () => handleBlur("email"));

verificationCodeInput.addEventListener("input", (e) =>
  handleInputChange("verificationCode", e.target.value)
);

sendCodeBtn.addEventListener("click", handleSendCode);
verifyCodeBtn.addEventListener("click", handleVerifyCode);

// 초기 UI 설정
updateUI();