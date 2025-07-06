function fetchParentCards(page = 0) {
    fetch(`/api/group/${groupId}/parent-cards?page=${page}&size=5`)
        .then(res => res.json())
        .then(data => {
            renderParentCards(data.content);
            renderPagination(data.totalPages, data.number);
        });
}

function renderParentCards(cards) {
    const container = document.getElementById('parentCardList');
    if (cards.length === 0) {
        container.innerHTML = `<div class="text-gray-400 text-center py-10">그룹에 속한 자녀가 없습니다.</div>`;
        return;
    }
    container.innerHTML = cards.map(parent => `
        <div class="flex items-start space-x-6 mb-8">
            <div class="w-40 flex-shrink-0 p-4 bg-gray-50 rounded-xl shadow text-sm">
                <div class="font-bold text-base mb-2">${parent.parentName}</div>
                <div class="text-gray-600 text-xs">${parent.parentPhone}</div>
            </div>
            <div class="flex space-x-4">
                ${parent.children.map(child => `
                    <div class="bg-white shadow rounded-xl p-4 min-w-[140px] cursor-pointer hover:bg-gray-50 transition"
                        data-child-id="${child.childId || ''}"
                        data-child-name="${child.childName || ''}"
                        data-gender="${child.gender || ''}"
                        data-birthday="${child.birthday ? child.birthday.substring(0, 10) : ''}"
                        data-height="${child.height || ''}"
                        data-weight="${child.weight || ''}"
                        data-risk-group="${child.riskGroup || ''}"
                        onclick="openChildDetailModal(this)">
                        <div class="font-semibold mb-1">${child.childName}</div>
                        <div>${child.gender} / ${child.birthday ? child.birthday.substring(0, 10) : ''}</div>
                    </div>
                `).join('')}
            </div>
        </div>
    `).join('');
}

function renderPagination(totalPages, currentPage) {
    const container = document.getElementById('pagination');
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }
    let html = '';
    if (currentPage > 0) {
        html += `<button onclick="fetchParentCards(0)" 
                    class="px-3 py-1 rounded bg-gray-200 text-[#374151] hover:bg-[#374151] hover:text-white transition">
                    <i class="fas fa-angle-double-left"></i>
                </button>`;

        html += `<button onclick="fetchParentCards(${currentPage - 1})" 
                    class="px-3 py-1 rounded bg-gray-200 text-[#374151] hover:bg-[#374151] hover:text-white transition">
                    <i class="fas fa-angle-left"></i>
                </button>`;
    }
    for (let i = 0; i < totalPages; i++) {
        html += `<button class="${i === currentPage ? 
            'px-3 py-1 rounded hover:bg-[#374151] hover:text-white transition bg-[#374151] text-white' : 
            'px-3 py-1 rounded hover:bg-[#374151] hover:text-white transition bg-gray-200 text-[#374151]'}" 
            onclick="fetchParentCards(${i})">${i + 1}</button>`;
    }
    if (currentPage < totalPages - 1) {
        html += `<button onclick="fetchParentCards(${currentPage + 1})" 
                    class="px-3 py-1 rounded bg-gray-200 text-[#374151] hover:bg-[#374151] hover:text-white transition">
                    <i class="fas fa-angle-right"></i>
                </button>`;
        html += `<button onclick="fetchParentCards(${totalPages - 1})" 
                    class="px-3 py-1 rounded bg-gray-200 text-[#374151] hover:bg-[#374151] hover:text-white transition">
                    <i class="fas fa-angle-double-right"></i>
                </button>`;
    }
    container.innerHTML = html;
}

// 최초 진입 시
fetchParentCards(0);
