// 📁 /js/sms-auth.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/9.22.2/firebase-app.js";
import { getAuth, RecaptchaVerifier, signInWithPhoneNumber } from "https://www.gstatic.com/firebasejs/9.22.2/firebase-auth.js";

// 🔐 Firebase config (환경에 맞게 수정)
    const firebaseConfig = {
        apiKey: "AIzaSyBsnvbbi1SQSHe9v3Nzt7R23eELXlv4KMI",
        authDomain: "charti-5da7d.firebaseapp.com",
        projectId: "charti-5da7d",
        storageBucket: "charti-5da7d.firebasestorage.app",
        messagingSenderId: "308166362794",
        appId: "1:308166362794:web:00035f97aca288228972d3",
        measurementId: "G-TYBBZC80J9"
    };

const app  = initializeApp(firebaseConfig);
const auth = getAuth(app);

// Invisible reCAPTCHA 설정
window.recaptchaVerifier = new RecaptchaVerifier('recaptcha-container', {
size: 'invisible'
}, auth);

// 전역 변수
let confirmationResult = null;
let smsIdToken = null;
let verifiedPhoneNumber = ''; // 인증 성공 시 저장될 전화번호
let newUid = ''; // ✅ 새 UID 저장용
// 🔹 인증번호 요청
async function sendSmsOtp(mode = 'signup') {
    
    const p1 = document.getElementById('phone1').value;
    const p2 = document.getElementById('phone2').value.trim();
    const p3 = document.getElementById('phone3').value.trim();

    if (!/^\d{4}$/.test(p2) || !/^\d{4}$/.test(p3)) {
        alert('휴대폰 번호를 정확히 입력하세요.');
        return;
    }
        verifiedPhoneNumber = `+82${p1.substring(1)}${p2}${p3}`;
    console.log("👉 verifiedPhoneNumber:", verifiedPhoneNumber);

    if (mode === 'signup') {
        // 중복 체크 예시 (옵션) 
        const checkRes = await fetch('/api/check-phone', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phoneNumber: verifiedPhoneNumber })
        });
        const checkData = await checkRes.json();
        if (!checkRes.ok || checkData.exists) {
            alert('이미 가입된 전화번호입니다.');
            return;
        }
    }

    try {
        confirmationResult = await signInWithPhoneNumber(auth, verifiedPhoneNumber, window.recaptchaVerifier);
        alert('인증번호를 전송했습니다.');
        document.getElementById('otp').disabled = false;
        document.getElementById('verify-otp-btn').disabled = false;
    } catch (err) {
        console.error('OTP 전송 실패:', err);
        alert('인증번호 전송 실패');
    }
}

// 🔹 인증번호 확인
async function verifySmsOtp(mode = 'signup') {
    const code = document.getElementById('otp').value.trim();
    if (!code) {
        alert('인증번호를 입력하세요.');
        return;
    }
    
    try {
        const userCredential = await confirmationResult.confirm(code);
        smsIdToken = await userCredential.user.getIdToken();
        const newUser = userCredential.user;
        newUid = newUser.uid; 

        // newPassword 필드가 있을경우에만 작동
        const pw1 = document.getElementById('newPassword');
        const pw2 = document.getElementById('confirmPassword');
        if (pw1) pw1.readOnly = false;
        if (pw2) pw2.readOnly = false;


        

        alert('인증 성공');
        if (mode === 'signup') {
            // ✅ 인증된 전화번호 입력란에 자동 삽입
            document.getElementById('phoneNumber').value = verifiedPhoneNumber;

            // 입력필드 비활성화
            ['phone1', 'phone2', 'phone3', 'otp'].forEach(id => document.getElementById(id).disabled = true);
            document.getElementById('send-otp-btn').disabled = true;
            document.getElementById('verify-otp-btn').disabled = true;
        }
    } catch (err) {
        console.error('인증 실패:', err);
        alert('인증 실패');
    }
}

// ✅ 외부 접근 가능하게 내보냄
window.sendSmsOtp = sendSmsOtp;
window.verifySmsOtp = verifySmsOtp;
window.getVerifiedSmsToken = () => smsIdToken;
window.getNewUid = () => newUid;
