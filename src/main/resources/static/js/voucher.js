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
const startInput = document.getElementById("startAt");
const endInput   = document.getElementById("endAt");

// Khi đổi start → giới hạn end
startInput.addEventListener("change", () => {
    endInput.min = startInput.value;

    // nếu end < start thì reset
    if (endInput.value < startInput.value) {
        endInput.value = startInput.value;
    }
});
// check discount value và discount type
const type = document.getElementById("discountType");
const value = document.getElementById("discountValue");
const unit = document.getElementById("discountUnit");
const hint = document.getElementById("discountHint");

function updateDiscountUI() {
    if (type.value === "PERCENT") {

        value.max = 100;
        value.placeholder = "0 - 100";
        unit.textContent = "%";
        hint.textContent = "Enter percent (0 — 100)";

    } else if (type.value === "FIXED") {

        value.removeAttribute("max");
        value.placeholder = "Amount";
        unit.textContent = "VND";
        hint.textContent = "Enter amount in VND";

    } else {

        value.removeAttribute("max");
        unit.textContent = "";
        hint.textContent = "";
    }

    value.value = ""; // reset khi đổi type để tránh nhầm
}

type.addEventListener("change", updateDiscountUI);

// chạy khi load lại form (edit hoặc validation fail)
document.addEventListener("DOMContentLoaded", updateDiscountUI);