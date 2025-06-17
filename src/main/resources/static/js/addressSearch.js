// 주소 검색 모달 열기
function openAddressModal() {
    document.getElementById('addressModal').classList.remove('hidden');
}

// 주소 검색 모달 닫기
function closeAddressModal() {
    document.getElementById('addressModal').classList.add('hidden');
}

// 주소 검색 요청 및 결과 처리
async function searchAddress() {
    const dong = document.getElementById('dongInput').value.trim();

    // 동 입력이 비어있으면 경고
    if (!dong) {
        alert("동 이름을 입력하세요.");
        return;
    }
    //  최소 2글자 입력 제한
    if (dong.length < 2) {
        alert("최소 두글자 이상 입력해주세요");
        return;
    }

    // 서버에 주소 검색 요청
    const res = await fetch(`/api/address/search?dong=${encodeURIComponent(dong)}`);
    const list = await res.json();

    const resultDiv = document.getElementById('addressResult');
    resultDiv.innerHTML = ''; // 이전 검색 결과 초기화

    // 검색 결과 없을 때 메시지 출력
    if (list.length === 0) {
        resultDiv.innerHTML = '<p class="text-gray-500">검색 결과가 없습니다.</p>';
        return;
    }

    // 검색 결과를 버튼 형태로 동적으로 추가
    list.forEach(address => {
        const item = document.createElement('button');
        item.type = 'button';
        item.className = 'w-full text-left text-gray-900 px-3 py-2 border rounded hover:bg-purple-100';
        item.innerText = `${address.zipNum} ${address.sido} ${address.gugun} ${address.dong}`;
        item.onclick = () => selectAddress(address); // 주소 선택 시 처리 함수 호출
        resultDiv.appendChild(item);
    });
}

// 주소 선택 시 폼 필드에 값 채우기
function selectAddress(address) {
    const form = document.forms['signupForm'];
    form.zipNum.value = address.zipNum;
    form.addr1.value = `${address.sido} ${address.gugun} ${address.dong}`;
    form.addressId.value = address.id; // hidden 필드에 addressId 저장
    closeAddressModal(); // 모달 닫기
}
