// 📌 주소 검색 모달 열기
function openAddressModal() {
    document.getElementById('addressModal').classList.remove('hidden');
}

// 📌 주소 검색 모달 닫기
function closeAddressModal() {
    document.getElementById('addressModal').classList.add('hidden');
}

// 📌 주소 검색 요청 및 결과 표시
async function searchAddress() {
    const dong = document.getElementById('dongInput').value.trim();

    if (dong.length < 2) {
    alert("최소 두 글자 이상 입력해주세요.");
    return;
    }

    // 서버에 주소 검색 요청
    try {
    const res = await fetch(`/api/address/search?dong=${encodeURIComponent(dong)}`);
    const list = await res.json();

    const resultDiv = document.getElementById('addressResult');
    resultDiv.innerHTML = ''; // 이전 검색 결과 초기화

    // 검색 결과 없을 때 메시지 출력
    if (list.length === 0) {
        resultDiv.innerHTML = '<p class="text-gray-500">검색 결과가 없습니다.</p>';
        return;
    }

    list.forEach(address => {
        const fullAddr = `${address.sido} ${address.gugun} ${address.dong}${address.bunji ? ' ' + address.bunji : ''}`;

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'w-full text-left px-4 py-2 border rounded hover:bg-purple-100 text-black';
        btn.innerText = `${address.zipNum} ${fullAddr}`;
        btn.onclick = () => selectAddress(address);

        resultDiv.appendChild(btn);
    });
    } catch (err) {
    console.error("주소 검색 실패:", err);
    alert("주소 검색 중 오류가 발생했습니다.");
    }
}

// 📌 주소 선택 시 입력 필드에 채우기
function selectAddress(address) {
    const fullAddr = `${address.sido} ${address.gugun} ${address.dong}${address.bunji ? ' ' + address.bunji : ''}`;
    document.getElementById('zipNum').value = address.zipNum;
    document.getElementById('addr1').value = fullAddr;
    document.getElementById('addressId').value = address.id; // hidden 필드에 addressId 저장
    closeAddressModal(); // 모달 닫기
}

// 📌 addressId로 주소 상세 조회 (필요 시 사용)
async function fetchAddressInfo(addressId) {
    try {
        const res = await fetch(`/api/address/${addressId}`); // 주소 상세 조회 API 호출
        if (!res.ok) throw new Error("주소 조회 실패");

    const address = await res.json();

    const fullAddr = `${address.sido} ${address.gugun} ${address.dong}${address.bunji ? ' ' + address.bunji : ''}`;
    document.getElementById('zipNum').value = address.zipNum;
    document.getElementById('addr1').value = fullAddr;
    } catch (err) {
    console.error(err);
    alert("주소 정보를 불러오는 데 실패했습니다.");
    }
}

