// 그룹 검색 모달 열기
function openGroupModal() {
    document.getElementById('groupModal').classList.remove('hidden');
}

// 그룹 검색 모달 닫기
function closeGroupModal() {
    document.getElementById('groupModal').classList.add('hidden');
}

// 입력된 이름으로 그룹 검색 요청
async function searchGroup() {
    const name = document.getElementById('groupSearchInput').value.trim();  // 검색어 입력값 가져오기
    const resultDiv = document.getElementById('groupResult');               // 결과 출력 영역
    resultDiv.innerHTML = '';                                              // 이전 검색결과 초기화

    // 2글자 미만 입력 시 경고
    if (name.length < 2) {
        alert("기관 이름을 두 글자 이상 입력하세요.");
        return;
    }

    // 서버에 그룹 검색 요청 (GET 방식)
    const res = await fetch(`/api/group/search?name=${encodeURIComponent(name)}`);
    const list = await res.json();  // 결과를 JSON 형태로 파싱

    // 결과 없음 처리
    if (list.length === 0) {
        resultDiv.innerHTML = '<p class="text-gray-500">검색 결과가 없습니다.</p>';
        return;
    }

    // 결과 리스트 반복 출력
    list.forEach(group => {
        const item = document.createElement('div');
        item.className = 'cursor-pointer hover:bg-gray-100 p-2 border rounded';

        // 결과 항목 구성
        item.innerHTML = `
            <strong>${group.name}</strong><br/>
            이메일: ${group.email}<br/>
            연락처: ${group.phoneNumber}
        `;

        // 항목 클릭 시 폼에 데이터 채우고 모달 닫기
        item.onclick = () => {
            fillGroupFields(group);
            closeGroupModal();
        };

        resultDiv.appendChild(item);
    });
}

// 선택된 그룹 정보 폼에 자동 입력
function fillGroupFields(group) {
    document.getElementById('groupId').value = group.id;
    document.getElementById('groupName').value = group.name;
    document.getElementById('groupEmail').value = group.email;
    document.getElementById('groupPhone').value = group.phoneNumber;
    document.getElementById('addressId').value = group.addressId;

    setGroupFieldsReadonly(true);
    // 주소 정보도 비동기로 불러오기
    fetchAddressInfo(group.addressId);
}


function enableGroupManualInput() {
    // groupId 제거 (새 그룹 생성 흐름)
    document.getElementById('groupId').value = '';

  // 읽기 전용 해제
    setGroupFieldsReadonly(false);
}

function setGroupFieldsReadonly(isReadonly) {
    const fields = ['groupName', 'groupEmail', 'groupPhone'];   //
    fields.forEach(id => {
        const el = document.getElementById(id);
        el.readOnly = isReadonly;

        // 스타일도 함께 바꾸면 UX 향상
        if (isReadonly) {
        el.classList.add('bg-gray-100', 'text-gray-500');
        } else {
        el.classList.remove('bg-gray-100', 'text-gray-500');
        }
    });
}
