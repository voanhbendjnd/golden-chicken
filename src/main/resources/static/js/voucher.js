function openConfirm(type, id) {
    const modal = document.getElementById('confirmModal');
    const title = document.getElementById('modalTitle');
    const msg = document.getElementById('modalMessage');
    const btn = document.getElementById('confirmBtn');

    if (type === 'disable') {
        title.innerText = 'Vô hiệu hóa voucher';
        msg.innerText = 'Bạn chắc muốn vô hiệu hóa voucher này!';
        btn.href = `/staff/voucher/disable/${id}`;
        btn.className = 'btn btn-warning';
    } else {
        title.innerText = 'Xóa voucher này!';
        msg.innerText = 'Bạn chắc chắn muốn xóa!';
        btn.href = `/staff/voucher/delete/${id}`;
        btn.className = 'btn btn-danger';
    }

    modal.style.display = 'flex';
}

function closeConfirm() {
    document.getElementById('confirmModal').style.display = 'none';
}
document.addEventListener("DOMContentLoaded", function () {
    const startInput = document.getElementById("startAt");
    const endInput = document.getElementById("endAt");
    const type = document.getElementById("discountType");
    const value = document.getElementById("discountValue");
    const unit = document.getElementById("discountUnit");
    const hint = document.getElementById("discountHint");
    const exchangeable = document.getElementById("exchangeable");
    const pointCost = document.getElementById("pointCost");
    const quantity = document.getElementById("quantity");

    function updateExchangeUI() {
        if (!exchangeable) return;
        const exchangeFields = document.querySelectorAll('.exchange-field');
        const isChecked = exchangeable.checked;

        exchangeFields.forEach(field => {
            field.style.display = isChecked ? 'flex' : 'none';
        });

        if (pointCost) {
            pointCost.required = isChecked;
            if (!isChecked) pointCost.value = 0;
        }
        if (quantity) {
            quantity.required = isChecked;
            if (!isChecked) quantity.value = 0;
        }
    }

    function updateDiscountUI(reset = false) {
        if (!type || !value || !unit || !hint) return;
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
        if (reset) {
            value.value = "";
        }
    }

    if (startInput && endInput) {
        startInput.addEventListener("change", () => {
            endInput.min = startInput.value;
            if (endInput.value < startInput.value) {
                endInput.value = startInput.value;
            }
        });
    }

    if (type) {
        type.addEventListener("change", function () {
            updateDiscountUI(true);
        });
    }

    if (exchangeable) {
        exchangeable.addEventListener("change", function () {
            updateExchangeUI();
        });
    }

    // Initialize UI states
    updateDiscountUI(false);
    updateExchangeUI();
});
