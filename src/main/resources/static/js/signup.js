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
let isEmailVerified = false; // 이메일 인증 완료 여부

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
const sendCodeText = document.getElementById("sendCodeText");
const verificationGroup = document.getElementById("verificationGroup");
const emailFrame = document.getElementById("emailFrame");
const emailDivider = document.getElementById("emailDivider");

const submitBtn = document.getElementById("submitBtn");
const logo = document.getElementById("logo");
const secondGroup = document.getElementById("secondGroup");
const thirdGroup = document.getElementById("thirdGroup");
const submitButton = document.getElementById("submitButton");

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
          errors.username = ["아이디: 5~16자의 영문 대/소문자, 숫자를 사용해 주세요."];
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
    case "nickname":
      if (value && !nickRegex.test(value)) {
        errorMsg =
          "닉네임: 4~20자의 한글, 영문 대/소문자를 사용해 주세요. (사용 가능한 특수문자 -, _)";
      }
      break;
    case "email":
      if (value && !emailRegex.test(value)) {
        errorMsg = "이메일: 올바른 이메일 형식으로 입력해 주세요.";
      } else if (value) {
        const reservedEmails = ["admin@example.com", "test@example.com"];
        if (reservedEmails.includes(value.toLowerCase())) {
          errorMsg = "이메일: 사용할 수 없는 이메일입니다.";
        }
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
  isCodeSent = true;
  isEmailVerified = true; // 인증번호 전송 시 인증 완료로 간주 (실제로는 인증번호 확인 로직 필요)
  sendCodeText.textContent = "재전송";
  verificationGroup.style.display = "block";
  emailFrame.classList.add("expanded");
  emailDivider.classList.add("visible");
  emailDivider.style.display = "block";
  updateUI();
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
  }

  // 이메일 검증
  if (!formData.email.trim()) {
    newErrors.email.push("이메일을 입력해주세요!");
    isValid = false;
  } else if (!emailRegex.test(formData.email)) {
    newErrors.email.push("올바른 이메일 형식으로 입력해 주세요!");
    isValid = false;
  } else if (!isEmailVerified) {
    newErrors.email.push("이메일 인증이 완료되지 않았습니다.");
    isValid = false;
  }

  // 인증번호 검증
  if (isCodeSent) {
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
    console.log("회원가입 데이터:", formData);
    alert("회원가입이 완료되었습니다!");

    // 폼 초기화
    formData = {
      username: "",
      password: "",
      passwordConfirm: "",
      name: "",
      nickname: "",
      email: "",
      verificationCode: "",
    };

    usernameInput.value = "";
    passwordInput.value = "";
    passwordConfirmInput.value = "";
    nameInput.value = "";
    nicknameInput.value = "";
    emailInput.value = "";
    verificationCodeInput.value = "";

    isCodeSent = false;
    isEmailVerified = false;
    sendCodeText.textContent = "인증번호 전송";
    verificationGroup.style.display = "none";
    emailFrame.classList.remove("expanded");
    emailDivider.classList.remove("visible");
    emailDivider.style.display = "none";

    errors = {
      username: [],
      password: [],
      passwordConfirm: [],
      name: [],
      nickname: [],
      email: [],
      verificationCode: [],
    };

    updateUI();
  }
}

// UI 업데이트
function updateUI() {
  // 모바일 여부 확인
  const isMobile = window.innerWidth <= 480;

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
    firstErrorDiv.textContent = "";
  }

  // 두 번째 그룹 에러 메시지 표시
  if (secondGroupErrors.length > 0) {
    secondErrorDiv.classList.add("visible");
    secondErrorDiv.innerHTML = secondGroupErrors
      .map((err) => `<div>${err}</div>`)
      .join("");
  } else {
    secondErrorDiv.classList.remove("visible");
    secondErrorDiv.textContent = "";
  }

  // 세 번째 그룹 에러 메시지 표시
  if (thirdGroupErrors.length > 0) {
    thirdErrorDiv.classList.add("visible");
    thirdErrorDiv.innerHTML = thirdGroupErrors
      .map((err) => `<div>${err}</div>`)
      .join("");
  } else {
    thirdErrorDiv.classList.remove("visible");
    thirdErrorDiv.textContent = "";
  }

  // 에러 메시지 높이 계산 (각 에러 메시지 20px)
  const firstErrorHeight =
    firstGroupErrors.length > 0 ? firstGroupErrors.length * 20 : 0;
  const secondErrorHeight =
    secondGroupErrors.length > 0 ? secondGroupErrors.length * 20 : 0;
  const thirdErrorHeight =
    thirdGroupErrors.length > 0 ? thirdGroupErrors.length * 20 : 0;

  // 각 그룹의 동적 위치 계산 (모바일/데스크탑 구분)
  if (isMobile) {
    const scale = 0.653;
    const firstGroupTop = 174;
    const logoTop = firstGroupTop - 25 - 51;
    const firstErrorTop = firstGroupTop + 150 * scale;
    const secondGroupTop =
      firstGroupTop + 150 * scale + firstErrorHeight * scale + 15;
    const thirdGroupTop =
      secondGroupTop + 100 * scale + secondErrorHeight * scale + 15;
    const buttonTop =
      thirdGroupTop +
      (isCodeSent ? 100 : 50) * scale +
      thirdErrorHeight * scale +
      15;

    // 위치 업데이트
    logo.style.top = `${logoTop}px`;
    firstErrorDiv.style.top = `${firstErrorTop}px`;
    secondGroup.style.top = `${secondGroupTop}px`;
    secondErrorDiv.style.top = `${secondGroupTop + 100 * scale}px`;
    thirdGroup.style.top = `${thirdGroupTop}px`;
    thirdErrorDiv.style.top = `${
      thirdGroupTop + (isCodeSent ? 100 : 50) * scale
    }px`;
    submitButton.style.top = `${buttonTop}px`;
  } else {
    const firstGroupTop = 174;
    const logoTop = firstGroupTop - 25 - 51;
    const secondGroupTop = firstGroupTop + 150 + firstErrorHeight + 20;
    const thirdGroupTop = secondGroupTop + 100 + secondErrorHeight + 20;
    const buttonTop =
      thirdGroupTop + (isCodeSent ? 100 : 50) + thirdErrorHeight + 20;

    // 위치 업데이트
    logo.style.top = `${logoTop}px`;
    secondGroup.style.top = `${secondGroupTop}px`;
    secondErrorDiv.style.top = `${secondGroupTop + 110}px`;
    thirdGroup.style.top = `${thirdGroupTop}px`;
    thirdErrorDiv.style.top = `${thirdGroupTop + (isCodeSent ? 110 : 60)}px`;
    submitButton.style.top = `${buttonTop}px`;
  }
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
submitBtn.addEventListener("click", handleSubmit);

// 초기 UI 설정
updateUI();
