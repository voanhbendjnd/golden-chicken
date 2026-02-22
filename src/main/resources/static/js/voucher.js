function openConfirm(type, id) {
    const modal = document.getElementById('confirmModal');
    const title = document.getElementById('modalTitle');
    const msg = document.getElementById('modalMessage');
    const btn = document.getElementById('confirmBtn');

    if (type === 'disable') {
        title.innerText = 'Disable Voucher';
        msg.innerText = 'Are you sure you want to disable this voucher?';
        btn.href = `/staff/voucher/disable/${id}`;
        btn.className = 'btn btn-warning';
    } else {
        title.innerText = 'Delete Voucher';
        msg.innerText = 'This action cannot be undone. Continue?';
        btn.href = `/staff/voucher/delete/${id}`;
        btn.className = 'btn btn-danger';
    }

    modal.style.display = 'flex';
}

function closeConfirm() {
    document.getElementById('confirmModal').style.display = 'none';
}
