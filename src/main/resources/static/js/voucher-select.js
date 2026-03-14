(function () {
    const checkboxes = Array.from(document.querySelectorAll('input[name="voucherIds"]'));
    const form = document.querySelector('form[action*="/checkout/apply-vouchers"]');
    const storageKey = 'selectedVouchers';

    function readStored() {
        try {
            return JSON.parse(localStorage.getItem(storageKey) || '[]');
        } catch (e) {
            localStorage.removeItem(storageKey);
            return [];
        }
    }

    function writeStored(list) {
        localStorage.setItem(storageKey, JSON.stringify(list));
    }

    function syncFromStorage() {
        const stored = readStored();
        const storedIds = new Set(stored.map(v => String(v.id)));
        checkboxes.forEach(cb => {
            cb.checked = storedIds.has(String(cb.value));
        });
    }

    function updateStorageFromCheckbox(cb) {
        const stored = readStored();
        const index = stored.findIndex(v => String(v.id) === String(cb.value));

        if (cb.checked) {
            if (index === -1) {
                stored.push({ id: String(cb.value), type: cb.dataset.type || '' });
            }
        } else if (index !== -1) {
            stored.splice(index, 1);
        }

        writeStored(stored);
    }

    function updateDisabled() {
        const stored = readStored();
        const selectedByType = {
            PRODUCT: stored.some(v => v.type === 'PRODUCT'),
            SHIPPING: stored.some(v => v.type === 'SHIPPING')
        };

        checkboxes.forEach(cb => {
            const type = cb.dataset.type;
            const disabledByMin = cb.dataset.minDisabled === 'true';
            const shouldDisable = disabledByMin || (selectedByType[type] && !cb.checked);
            cb.disabled = shouldDisable;
            const card = cb.closest('.voucher-card');
            if (card) {
                card.classList.toggle('voucher-disabled', shouldDisable);
            }
        });
    }

    checkboxes.forEach(cb => cb.addEventListener('change', () => {
        updateStorageFromCheckbox(cb);
        syncFromStorage();
        updateDisabled();
    }));

    function appendStoredToForm() {
        if (!form) {
            return;
        }
        form.querySelectorAll('input.voucher-hidden').forEach(el => el.remove());
        const stored = readStored();
        const currentIds = new Set(checkboxes.map(cb => String(cb.value)));
        stored.forEach(item => {
            if (currentIds.has(String(item.id))) {
                return;
            }
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'voucherIds';
            input.value = String(item.id);
            input.className = 'voucher-hidden';
            form.appendChild(input);
        });
    }

    if (form) {
        form.addEventListener('submit', () => {
            appendStoredToForm();
            localStorage.removeItem(storageKey);
        });
    }

    syncFromStorage();
    updateDisabled();
})();
