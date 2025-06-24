// managerPage.js
// ––––––––––––––––
// 자녀 모달, 수정 모드, SMS · 주소 검색 토글 처리

// 자녀 목록 모달 열기
// 1) 전역 children 배열 가져오기
const children = window.MANAGER_CHILDREN || [];

// 자녀 모달 열기
window.openChildModal = function() {
const container = document.getElementById('childListContainer');
container.innerHTML = '';

if (!children.length) {
    container.innerHTML = '<p class="text-gray-500">등록된 자녀가 없습니다.</p>';
} else {
    children.forEach(child => {
    const div = document.createElement('div');
    div.className = 'border p-3 rounded bg-gray-100';

    // 2열 그리드 레이아웃
    div.innerHTML = `
        <div class="grid grid-cols-3 gap-3">
        <p><strong>이름:</strong> ${child.name}</p>
        <p><strong>성별:</strong> ${child.gender}</p>
        <p><strong>별명:</strong> ${child.nickname || '—'}</p>
        <p><strong>생일:</strong> ${child.birthday}</p>
        <p><strong>체중:</strong> ${child.weight}</p>
        <p><strong>신장:</strong> ${child.height}</p>
        </div>
    `;

    container.appendChild(div);
    });
}

document.getElementById('childModal').classList.remove('hidden');
};

window.closeChildModal = function() {
document.getElementById('childModal').classList.add('hidden');
};

// 수정 모드 진입 전 필드 값 저장용
let originalData = {};

// 수정 모드 활성화
window.enableEditMode = function () {
    const editableFields = document.querySelectorAll('#userEditForm input');
    originalData = {};

    editableFields.forEach(field => {
        originalData[field.id] = field.value;

        // username, phoneNumber 필드는 계속 readonly
        if (['username', 'phoneNumber'].includes(field.id)) return;

        if (field.hasAttribute('readonly')) {
            field.removeAttribute('readonly');
            field.classList.remove('bg-gray-100');
        }
    });

    document.getElementById('editButtonBox').classList.add('hidden');
    document.getElementById('actionButtonBox').classList.remove('hidden');

    // SMS 인증 섹션
    document.getElementById('smsSection').classList.remove('hidden');
    // 주소 검색 버튼
    document.getElementById('addressSearchBtn').classList.remove('hidden');
    // 그룹 분류 토글
    document.getElementById('targetGroupDisplayBox').classList.add('hidden');
    document.getElementById('targetGroupSelectBox').classList.remove('hidden');
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
    document.getElementById('addressSearchBtn').classList.add('hidden');
    document.getElementById('targetGroupDisplayBox').classList.remove('hidden');
    document.getElementById('targetGroupSelectBox').classList.add('hidden');
};

// 수정 완료
window.submitEdit = function () {
    document.getElementById('userEditForm').submit();
};
