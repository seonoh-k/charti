// managerPage.js (마이페이지 담당자 정보 전용)

// 수정모드 진입 전 원본값 저장
let originalData = {};

// 수정 모드 활성화
window.enableEditMode = function () {
    const editableFields = ['name', 'nickname'];
    originalData = {};

    editableFields.forEach(id => {
        const field = document.getElementById(id);
        if (!field) return;
        originalData[id] = field.value;
        if (field.hasAttribute('readonly')) {
            field.removeAttribute('readonly');
            field.classList.remove('bg-gray-100');
        }
    });

    // username(아이디)는 항상 readonly
    document.getElementById('editButtonBox').classList.add('hidden');
    document.getElementById('actionButtonBox').classList.remove('hidden');
    // 전화번호 변경시 SMS 인증 섹션 보이게
    document.getElementById('smsSection').classList.remove('hidden');
};

// 수정 모드 취소
window.cancelEdit = function () {
    Object.entries(originalData).forEach(([id, value]) => {
        const field = document.getElementById(id);
        if (!field) return;
        field.value = value;
        field.setAttribute('readonly', true);
        field.classList.add('bg-gray-100');
    });

    document.getElementById('editButtonBox').classList.remove('hidden');
    document.getElementById('actionButtonBox').classList.add('hidden');
    document.getElementById('smsSection').classList.add('hidden');
};

// 수정 완료 (폼 submit)
window.submitEdit = function () {
    document.getElementById('userEditForm').submit();
};
