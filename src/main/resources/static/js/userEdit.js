// 📁 /js/userEdit.js

function openPasswordModal() {
    document.getElementById('passwordModal').classList.remove('hidden');
}
function closePasswordModal() {
    document.getElementById('passwordModal').classList.add('hidden');
}

async function submitPasswordChange() {
    const current = document.getElementById('currentPassword').value;
    const newPass = document.getElementById('newPassword').value;
    const confirm = document.getElementById('confirmPassword').value;

    if (newPass !== confirm) {
        alert("새 비밀번호가 일치하지 않습니다.");
        return;
    }

    const response = await fetch('/api/users/update/password', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentPassword: current, newPassword: newPass, confirmPassword: confirm })
    });

    if (response.ok) {
        alert("비밀번호가 변경되었습니다.");
        closePasswordModal();
    } else {
        const result = await response.json();
        alert(result.message || "비밀번호 변경 실패");
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const saveBtn = document.getElementById('saveBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', async (e) => {
            e.preventDefault();

            const payload = {
                name: document.getElementById('name').value,
                nickname: document.getElementById('nickname').value,
                addressId: parseInt(document.getElementById('addressId').value),
                phoneNumber: document.getElementById('phoneNumber').value,
                newUid: getNewUid()
            };

            try {
                const res = await fetch('/api/users/update', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const result = await res.json();

                if (res.ok) {
                    alert(result.message || "회원 정보가 수정되었습니다.");
                    location.reload();
                } else {
                    alert(result.message || "수정에 실패했습니다.");
                }
            } catch (error) {
                console.error("요청 중 에러 발생:", error);
                alert("알 수 없는 오류가 발생했습니다.");
            }
        });
    }
});

function confirmDelete() {
    if (!confirm("정말로 탈퇴하시겠습니까?")) return;
    fetch('/api/users/delete', { method: 'DELETE' })
        .then(res => {
            if (res.ok) {
                alert("탈퇴되었습니다.");
                location.href = "/loginForm";
            } else {
                alert("탈퇴 실패");
            }
        });
}
