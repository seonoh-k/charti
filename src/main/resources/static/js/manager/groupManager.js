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

// 상세 모달 띄우기
function openChildDetailModal(cardElem) {
    document.getElementById('modalChildName').textContent = cardElem.dataset.childName || '';
    document.getElementById('modalChildGender').textContent = cardElem.dataset.gender || '';
    document.getElementById('modalChildBirthday').textContent = cardElem.dataset.birthday || '';
    document.getElementById('modalChildNickname').textContent = cardElem.dataset.nickname || '';
    document.getElementById('modalChildHeight').textContent = cardElem.dataset.height || '';
    document.getElementById('modalChildWeight').textContent = cardElem.dataset.weight || '';
    document.getElementById('modalChildBirthOrder').textContent = cardElem.dataset.birthOrder || '';
    document.getElementById('modalChildRiskGroup').textContent = cardElem.dataset.riskGroup === "true" ? "예" : "아니오";

    const childId = cardElem.dataset.childId;
    const childName = cardElem.dataset.childName;
    document.getElementById('removeChildBtn').onclick = function () {
        removeChildFromGroup(childId, childName);
    };

    document.getElementById('childDetailModal').classList.remove('hidden');
}

function closeChildDetailModal() {
    document.getElementById('childDetailModal').classList.add('hidden');
}

// 제외는 네가 준거 그대로!
function removeChildFromGroup(childId, childName) {
    if (!confirm(`${childName} 자녀를 그룹에서 제외하시겠습니까?`)) return;

    fetch('/manager/child/remove', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ childId })
    })
    .then(res => res.json())
    .then(data => {
        alert(data.message || "자녀가 그룹에서 제외되었습니다.");
        closeChildDetailModal();
        // 새로고침 또는 ajax로 해당 카드만 지우기 가능
        location.reload();
    })
    .catch(() => alert("제외에 실패했습니다."));
}
