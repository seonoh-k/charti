// 폼의 원본값을 모두 저장해둘 객체
let originalGroupForm = {};

// 보기/수정 모드 전환 전용 함수
function toggleGroupEdit(editMode) {
    const viewSection = document.getElementById('groupViewSection');
    const editForm = document.getElementById('groupEditForm');

    viewSection.classList.toggle('hidden', editMode);
    editForm.classList.toggle('hidden', !editMode);

    // 수정모드 진입 시 원본값 저장
    if (editMode) {
        originalGroupForm = {
            groupName: document.getElementById('groupName').value,
            groupEmail: document.getElementById('groupEmail').value,
            groupPhoneNumber: document.getElementById('groupPhoneNumber').value,
            targetGroup: document.getElementById('targetGroup').value,
            zipNum: document.getElementById('zipNum').value,
            addr1: document.getElementById('addr1').value,
            addressId: document.getElementById('addressId').value,
        };
    } else {
        // 취소시 원본값을 복원
        document.getElementById('groupName').value = originalGroupForm.groupName;
        document.getElementById('groupEmail').value = originalGroupForm.groupEmail;
        document.getElementById('groupPhoneNumber').value = originalGroupForm.groupPhoneNumber;
        document.getElementById('targetGroup').value = originalGroupForm.targetGroup;
        document.getElementById('zipNum').value = originalGroupForm.zipNum;
        document.getElementById('addr1').value = originalGroupForm.addr1;
        document.getElementById('addressId').value = originalGroupForm.addressId;
    }
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
    document.getElementById('modalChildHeight').textContent = cardElem.dataset.height || '';
    document.getElementById('modalChildWeight').textContent = cardElem.dataset.weight || '';
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
        // 새로고침
        location.reload();
    })
    .catch(() => alert("제외에 실패했습니다."));
}
