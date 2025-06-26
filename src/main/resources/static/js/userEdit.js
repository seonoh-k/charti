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
