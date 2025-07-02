let originalUserForm = {}; // 진입 시 원본값 저장

function toggleUserEdit(editMode) {
    const viewSection = document.getElementById('userViewSection');
    const editForm = document.getElementById('userEditForm');

    viewSection.classList.toggle('hidden', editMode);
    editForm.classList.toggle('hidden', !editMode);

    if (editMode) {
        // 원본값 전체 저장
        originalUserForm = {
            name: document.getElementById('name').value,
            nickname: document.getElementById('nickname').value,
            phoneNumber: document.getElementById('phoneNumber').value,
            zipNum: document.getElementById('zipNum').value,
            addr1: document.getElementById('addr1').value,
            major: document.getElementById('major').value,
            career: document.getElementById('career').value,
            license: document.querySelector("#hiddenLicense").value,
            // 주소 id, 있으면 저장
            addressId: document.getElementById('addressId') ? document.getElementById('addressId').value : ''
        };

        // SMS 입력/버튼 등 상태 초기화
        document.getElementById('phone1').disabled = false;
        document.getElementById('phone2').disabled = false;
        document.getElementById('phone3').disabled = false;
        document.getElementById('send-otp-btn').disabled = false;
        document.getElementById('phone2').value = '';
        document.getElementById('phone3').value = '';
        document.getElementById('otp').value = '';
        document.getElementById('otp').disabled = true;
        document.getElementById('verify-otp-btn').disabled = true;

        // 인증 전역변수 리셋(외부모듈에서 관리하는 값도 초기화 권장)
        window.smsIdToken = null;
        window.verifiedPhoneNumber = '';
        window.newUid = '';

    } else {
        // 원본값 복원
        document.getElementById('name').value = originalUserForm.name;
        document.getElementById('nickname').value = originalUserForm.nickname;
        document.getElementById('phoneNumber').value = originalUserForm.phoneNumber;
        document.getElementById('zipNum').value = originalUserForm.zipNum;
        document.getElementById('addr1').value = originalUserForm.addr1;
        document.getElementById('major').value = originalUserForm.major;
        document.getElementById('career').value = originalUserForm.career;
        document.querySelector("#hiddenLicense").value = originalUserForm.license;

        // 주소id가 있으면 복원 (없으면 무시)
        if (document.getElementById('addressId')) {
            document.getElementById('addressId').value = originalUserForm.addressId;
        }

        // SMS 입력/버튼 상태 비활성화
        document.getElementById('phone1').disabled = true;
        document.getElementById('phone2').disabled = true;
        document.getElementById('phone3').disabled = true;
        document.getElementById('send-otp-btn').disabled = true;
        document.getElementById('otp').value = '';
        document.getElementById('otp').disabled = true;
        document.getElementById('verify-otp-btn').disabled = true;

        // 인증값 리셋
        window.smsIdToken = null;
        window.verifiedPhoneNumber = '';
        window.newUid = '';
    }
}


// 수정 완료 (폼 submit)

function submitEdit() {
    document.getElementById('userEditForm').submit();
}