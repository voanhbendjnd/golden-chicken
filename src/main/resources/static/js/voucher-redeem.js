(() => {
    const $ = (id) => document.getElementById(id);

    const tabRedeem = $("tabRedeem");
    const tabHistory = $("tabHistory");
    const redeemSection = $("redeemSection");
    const historySection = $("historySection");
    const myVoucherSection = $("myVoucherSection");
    const btnMyVouchers = $("btnMyVouchers");
    const btnBackFromMy = $("btnBackFromMy");

    if (
        !tabRedeem || !tabHistory || !redeemSection || !historySection ||
        !myVoucherSection || !btnMyVouchers || !btnBackFromMy
    ) return;

    const show = (el, on) => (el.style.display = on ? "" : "none");

    const activateTab = (isRedeem) => {
        tabRedeem.classList.toggle("active", isRedeem);
        tabHistory.classList.toggle("active", !isRedeem);
        tabRedeem.setAttribute("aria-selected", String(isRedeem));
        tabHistory.setAttribute("aria-selected", String(!isRedeem));
    };

    const setTab = (isRedeem) => {
        if (myVoucherSection.style.display !== "none") return;
        activateTab(isRedeem);
        show(redeemSection, isRedeem);
        show(historySection, !isRedeem);
    };

    const showMyVouchers = (showMine) => {
        show(myVoucherSection, showMine);
        show(redeemSection, !showMine);
        show(historySection, false);
        activateTab(true);
    };

    tabRedeem.onclick = () => setTab(true);
    tabHistory.onclick = () => setTab(false);
    btnMyVouchers.onclick = () => showMyVouchers(true);
    btnBackFromMy.onclick = () => showMyVouchers(false);

    document.addEventListener("click", (e) => {
        const btn = e.target.closest("[data-cost]");
        if (!btn) return;

        const cost = btn.dataset.cost;
        alert(`Xác nhận đổi (tốn ${cost} điểm)?`);
    });
})();