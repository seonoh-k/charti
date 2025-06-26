let originalGroupData = {};

// 수정 모드 진입
function enableGroupEditMode() {
    const editableFields = ['groupName', 'groupEmail', 'groupPhoneNumber'];
    originalGroupData = {};

    // 기존 값 저장 및 readonly 해제
    editableFields.forEach(id => {
        const field = document.getElementById(id);
        if (!field) return;
        originalGroupData[id] = field.value;
        if (field.hasAttribute('readonly')) {
            field.removeAttribute('readonly');
            field.classList.remove('bg-gray-100');
        }
    });
    // 그룹 분류: input 대신 select 보이게
    document.getElementById('targetGroupDisplayBox').classList.add('hidden');
    document.getElementById('targetGroupSelectBox').classList.remove('hidden');
    // 주소 검색 버튼 보이게
    document.getElementById('addressSearchBtn').classList.remove('hidden');

    document.getElementById('groupEditButtonBox').classList.add('hidden');
    document.getElementById('groupActionButtonBox').classList.remove('hidden');
}

// 취소 시 값 복원 및 readonly
function cancelGroupEdit() {
    Object.entries(originalGroupData).forEach(([id, value]) => {
        const field = document.getElementById(id);
        if (!field) return;
        field.value = value;
        field.setAttribute('readonly', true);
        field.classList.add('bg-gray-100');
    });
    // 분류는 다시 input만
    document.getElementById('targetGroupDisplayBox').classList.remove('hidden');
    document.getElementById('targetGroupSelectBox').classList.add('hidden');
    // 주소 검색 버튼 숨기기
    document.getElementById('addressSearchBtn').classList.add('hidden');

    document.getElementById('groupEditButtonBox').classList.remove('hidden');
    document.getElementById('groupActionButtonBox').classList.add('hidden');
}

// 저장 (submit)
function submitGroupEdit() {
    document.getElementById('groupEditForm').submit();
}




