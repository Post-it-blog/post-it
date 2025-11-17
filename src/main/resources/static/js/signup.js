// 정규표현식
const idRegex = /^[A-Za-z0-9]{5,16}$/;
const pwdRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=])\S{8,16}$/;
const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
const nameRegex = /^[A-Za-z가-힣]{2,30}$/;
const nickRegex = /^[A-Za-z가-힣0-9_-]{4,20}$/;
const emailAuthRegex = /^[0-9]+$/;

// 폼 데이터
let formData = {
  username: "",
  password: "",
  passwordConfirm: "",
  name: "",
  nickname: "",
  email: "",
  verificationCode: "",
};

// 에러 상태
let errors = {
  username: [],
  password: [],
  passwordConfirm: [],
  name: [],
  nickname: [],
  email: [],
  verificationCode: [],
};

// 인증번호 전송 상태
let isCodeSent = false;
let isEmailVerified = false;

// DOM 요소
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const passwordConfirmInput = document.getElementById("passwordConfirm");
const nameInput = document.getElementById("name");
const nicknameInput = document.getElementById("nickname");
const emailInput = document.getElementById("email");
const verificationCodeInput = document.getElementById("verificationCode");

const firstErrorDiv = document.getElementById("firstError");
const secondErrorDiv = document.getElementById("secondError");
const thirdErrorDiv = document.getElementById("thirdError");

const sendCodeBtn = document.getElementById("sendCodeBtn");
const verifyCodeBtn = document.getElementById("verifyCodeBtn");
const submitBtn = document.getElementById("submitBtn");

const section2 = document.querySelector(".section-2");
const section3 = document.querySelector(".section-3");
const submitButtonContainer = document.querySelector(
  ".submit-button-container"
);
const labelVerification = document.getElementById("labelVerification");

// 입력 변경 핸들러
function handleInputChange(field, value) {
  formData[field] = value;
}

// blur 핸들러
function handleBlur(field) {
  const value = formData[field].trim();
  let errorMsg = "";

  if (field === "username") {
    if (!idRegex.test(value)) {
      errors.username = [
        "아이디: 5~16자의 영문 대/소문자, 숫자를 사용해 주세요.",
      ];
      updateUI();
      return;
    }

    $.post("/mem/checkId", { userId: value }, function (res) {
      if (res === "DUPLICATE") {
        errors.username = ["아이디: 사용할 수 없는 아이디입니다."];
      } else {
        errors.username = [];
      }
      updateUI();
    }).fail(function () {
      errors.username = ["서버와 통신 중 오류가 발생했습니다."];
      updateUI();
    });
    return;
  }

  if (field === "nickname") {
    if (!nickRegex.test(value)) {
      errors.nickname = [
        "닉네임: 4~20자의 한글, 영문 대/소문자를 사용해 주세요. (사용 가능한 특수문자 -, _)",
      ];
      updateUI();
      return;
    }

    $.post("/mem/checkNick", { nickname: value }, function (res) {
      if (res === "FAIL") {
        errors.nickname = [
          "닉네임: 4~20자의 한글, 영문 대/소문자를 사용해 주세요. (사용 가능한 특수문자 -, _)",
        ];
      } else if (res === "DUPLICATE") {
        errors.nickname = ["이미 사용 중인 닉네임입니다."];
      } else {
        errors.nickname = [];
      }
      updateUI();
    }).fail(function () {
      errors.nickname = ["서버와 통신 중 오류가 발생했습니다."];
      updateUI();
    });
    return;
  }

  switch (field) {
    case "password":
      if (value && !pwdRegex.test(value)) {
        errorMsg =
          "비밀번호: 8~16자의 영문자, 숫자, 특수문자의 조합을 사용해 주세요.<br>(사용 가능한 특수문자: !@#$%^&*()_+-=)";
      }
      break;
    case "passwordConfirm":
      if (value && formData.password !== value) {
        errorMsg = "비밀번호 확인: 비밀번호가 일치하지 않습니다.";
      }
      break;
    case "name":
      if (value && !nameRegex.test(value)) {
        errorMsg =
          "이름: 한글, 영문 대/소문자를 사용해 주세요. (특수기호, 공백 사용 불가)";
      }
      break;
  }

  errors[field] = errorMsg ? [errorMsg] : [];
  updateUI();
}

// 인증번호 전송 핸들러
function handleSendCode() {
  const email = formData.email.trim();

  if (!email) {
    errors.email = ["이메일을 입력해주세요!"];
    updateUI();
    return;
  }

  if (!emailRegex.test(email)) {
    errors.email = ["올바른 이메일 형식으로 입력해 주세요!"];
    updateUI();
    return;
  }

  errors.email = [];

  $.post("/api/email/send", { email: email }, function (res) {
    if (res === "TOO_FAST") {
      errors.email = ["잠시 후 다시 시도해주세요."];
    } else if (res === "DUPLICATE") {
      errors.email = ["사용할 수 없는 이메일입니다."];
      updateUI();
      return;
    } else if (res === "FAIL") {
      errors.email = ["유효하지 않은 이메일 형식입니다."];
      updateUI();
      return;
    } else {
      errors.email = [
        '<span class="success-message">인증코드가 이메일로 전송되었습니다.</span>',
      ];
      isCodeSent = true;
      sendCodeBtn.querySelector(".btn-text").textContent = "재전송";
    }
    updateUI();
  }).fail(function () {
    errors.email = ["서버와 통신 중 오류가 발생했습니다."];
    updateUI();
  });
}

// 인증번호 확인 핸들러
function handleVerifyCode() {
  const code = formData.verificationCode.trim();

  if (!code) {
    errors.email = ["인증코드를 입력해주세요."];
    updateUI();
    return;
  }

  $.post(
    "/api/email/check",
    { code: code, email: formData.email },
    function (res) {
      if (res === "FAIL") {
        errors.email = ["인증코드가 일치하지 않습니다."];
      } else if (res === "TIMEOUT") {
        errors.email = [
          "세션이 만료 되었습니다. 이메일 인증을 다시 시도해주세요.",
        ];
        updateUI();
        return;
      } else if (res === "NOT_MATCHED_EMAIL") {
        errors.email = [
          "이메일이 일치하지 않습니다. 이메일 인증을 다시 시도해주세요.",
        ];
      } else {
        errors.email = [
          '<span class="success-message">이메일 인증이 완료 되었습니다.</span>',
        ];
        isEmailVerified = true;
      }
      updateUI();
    }
  ).fail(function () {
    errors.email = ["서버와 통신 중 오류가 발생했습니다."];
    updateUI();
  });
}

// 폼 검증
function validateForm() {
  const newErrors = {
    username: [],
    password: [],
    passwordConfirm: [],
    name: [],
    nickname: [],
    email: [],
    verificationCode: [],
  };
  let isValid = true;

  // 아이디 검증
  if (!formData.username.trim()) {
    newErrors.username.push("아이디를 입력해주세요!");
    isValid = false;
  } else if (!idRegex.test(formData.username)) {
    newErrors.username.push("5~16자의 영문자, 숫자로 조합해 주세요!");
    isValid = false;
  } else if (errors.username.length > 0) {
    newErrors.username = [...errors.username];
    isValid = false;
  }

  // 비밀번호 검증
  if (!formData.password) {
    newErrors.password.push("비밀번호를 입력해주세요!");
    isValid = false;
  } else if (!pwdRegex.test(formData.password)) {
    newErrors.password.push("8~16자의 영문자, 숫자, 특수문자로 조합해 주세요!");
    isValid = false;
  }

  // 비밀번호 확인 검증
  if (!formData.passwordConfirm) {
    newErrors.passwordConfirm.push("비밀번호 확인을 입력해주세요!");
    isValid = false;
  } else if (formData.password !== formData.passwordConfirm) {
    newErrors.passwordConfirm.push("비밀번호가 일치하지 않습니다!");
    isValid = false;
  }

  // 이름 검증
  if (!formData.name.trim()) {
    newErrors.name.push("이름을 입력해주세요!");
    isValid = false;
  } else if (!nameRegex.test(formData.name)) {
    newErrors.name.push("2~30자의 한글 또는 영문으로 입력해 주세요!");
    isValid = false;
  }

  // 닉네임 검증
  if (!formData.nickname.trim()) {
    newErrors.nickname.push("닉네임을 입력해주세요!");
    isValid = false;
  } else if (!nickRegex.test(formData.nickname)) {
    newErrors.nickname.push(
      "4~20자의 한글, 영문, 숫자, _를 사용할 수 있습니다."
    );
    isValid = false;
  } else if (errors.nickname.length > 0) {
    newErrors.nickname = [...errors.nickname];
    isValid = false;
  }

  // 이메일 검증
  if (!formData.email.trim()) {
    newErrors.email.push("이메일을 입력해주세요!");
    isValid = false;
  } else if (!emailRegex.test(formData.email)) {
    newErrors.email.push("올바른 이메일 형식으로 입력해 주세요!");
    isValid = false;
  } else if (!isEmailVerified) {
    newErrors.email.push("이메일 인증이 완료되지 않았습니다!");
    isValid = false;
  }

  // 인증번호 검증
  if (isCodeSent && !isEmailVerified) {
    if (!formData.verificationCode.trim()) {
      newErrors.verificationCode.push("인증번호를 입력해주세요!");
      isValid = false;
    } else if (!emailAuthRegex.test(formData.verificationCode)) {
      newErrors.verificationCode.push("숫자만 입력 가능합니다!");
      isValid = false;
    }
  }

  errors = newErrors;
  updateUI();
  return isValid;
}

// 제출 핸들러
function handleSubmit(e) {
  e.preventDefault();

  if (validateForm()) {
    const dataToSend = {
      userId: formData.username,
      password: formData.password,
      name: formData.name,
      nickname: formData.nickname,
      email: formData.email,
    };

    $.ajax({
      url: "/mem/signup",
      type: "POST",
      contentType: "application/json",
      data: JSON.stringify(dataToSend),

      success: function (res) {
        if (res === "SUCCESS") {
          window.location.href = "/mem/welcome";
        } else if (res === "FAIL") {
          errors.email = ["잘못된 접근입니다."];
          updateUI();
        }
      },
      error: function (jqXHR, textStatus, errorThrown) {
        console.error("AJAX ERROR: ", textStatus, errorThrown);
        alert("회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
      },
    });
  }
}

// UI 업데이트
function updateUI() {
  // 에러 메시지 그룹별 수집
  const firstGroupErrors = [
    ...errors.username,
    ...errors.password,
    ...errors.passwordConfirm,
  ];
  const secondGroupErrors = [...errors.name, ...errors.nickname];
  const thirdGroupErrors = [...errors.email, ...errors.verificationCode];

  // 첫 번째 그룹 에러 메시지 표시
  if (firstGroupErrors.length > 0) {
    firstErrorDiv.classList.add("visible");
    firstErrorDiv.innerHTML = firstGroupErrors
      .map((err) => `<div>${err}</div>`)
      .join("");
  } else {
    firstErrorDiv.classList.remove("visible");
    firstErrorDiv.innerHTML = "";
  }

  // 두 번째 그룹 에러 메시지 표시
  if (secondGroupErrors.length > 0) {
    secondErrorDiv.classList.add("visible");
    secondErrorDiv.innerHTML = secondGroupErrors
      .map((err) => `<div>${err}</div>`)
      .join("");
  } else {
    secondErrorDiv.classList.remove("visible");
    secondErrorDiv.innerHTML = "";
  }

  // 세 번째 그룹 에러 메시지 표시
  if (thirdGroupErrors.length > 0) {
    thirdErrorDiv.classList.add("visible");
    thirdErrorDiv.innerHTML = thirdGroupErrors
      .map((err) => `<div>${err}</div>`)
      .join("");
  } else {
    thirdErrorDiv.classList.remove("visible");
    thirdErrorDiv.innerHTML = "";
  }

  // 인증번호 필드 표시/숨김
  if (isCodeSent) {
    labelVerification.style.display = "flex";
    verificationCodeInput.style.display = "flex";
    verifyCodeBtn.style.display = "block";
    section3.classList.add("expanded");
  } else {
    labelVerification.style.display = "none";
    verificationCodeInput.style.display = "none";
    verifyCodeBtn.style.display = "none";
    section3.classList.remove("expanded");
  }

  // 에러 메시지 높이 계산 (각 에러 메시지 25px)
  const firstErrorHeight =
    firstGroupErrors.length > 0 ? firstGroupErrors.length * 25 + 20 : 0;
  const secondErrorHeight =
    secondGroupErrors.length > 0 ? secondGroupErrors.length * 25 + 20 : 0;
  const thirdErrorHeight =
    thirdGroupErrors.length > 0 ? thirdGroupErrors.length * 25 + 20 : 0;

  // section-3의 현재 높이
  const section3Height = isCodeSent ? 135 : 68;

  // 섹션 위치 동적 조정
  const section2Top = 417 + firstErrorHeight;
  const section3Top = section2Top + 135 + 34 + secondErrorHeight;
  const submitTop = section3Top + section3Height + 43 + thirdErrorHeight;

  section2.style.top = `${section2Top}px`;
  section2.style.left = "50%";
  section2.style.transform = "translateX(-50%)";
  secondErrorDiv.style.top = `${section2Top + 135 + 15}px`;
  section3.style.top = `${section3Top}px`;
  section3.style.left = "50%";
  section3.style.transform = "translateX(-50%)";
  thirdErrorDiv.style.top = `${section3Top + section3Height + 15}px`;
  submitButtonContainer.style.top = `${submitTop}px`;
}

// 이벤트 리스너 등록
usernameInput.addEventListener("input", (e) =>
  handleInputChange("username", e.target.value)
);
usernameInput.addEventListener("blur", () => handleBlur("username"));

passwordInput.addEventListener("input", (e) =>
  handleInputChange("password", e.target.value)
);
passwordInput.addEventListener("blur", () => handleBlur("password"));

passwordConfirmInput.addEventListener("input", (e) =>
  handleInputChange("passwordConfirm", e.target.value)
);
passwordConfirmInput.addEventListener("blur", () =>
  handleBlur("passwordConfirm")
);

nameInput.addEventListener("input", (e) =>
  handleInputChange("name", e.target.value)
);
nameInput.addEventListener("blur", () => handleBlur("name"));

nicknameInput.addEventListener("input", (e) =>
  handleInputChange("nickname", e.target.value)
);
nicknameInput.addEventListener("blur", () => handleBlur("nickname"));

emailInput.addEventListener("input", (e) =>
  handleInputChange("email", e.target.value)
);
emailInput.addEventListener("blur", () => handleBlur("email"));

verificationCodeInput.addEventListener("input", (e) =>
  handleInputChange("verificationCode", e.target.value)
);
verificationCodeInput.addEventListener("blur", () =>
  handleBlur("verificationCode")
);

sendCodeBtn.addEventListener("click", handleSendCode);
verifyCodeBtn.addEventListener("click", handleVerifyCode);
submitBtn.addEventListener("click", handleSubmit);

// 초기 UI 설정
updateUI();
