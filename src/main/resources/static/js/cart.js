 document.addEventListener('DOMContentLoaded', function () {
            const csrfToken = document.getElementById('csrf-token').value;
            // Nhập số lượng rồi Enter sẽ gọi API
            document.body.addEventListener('keydown', function (e) {
                if (e.target.classList.contains('qty-input') && e.key === 'Enter') {
                    const input = e.target;
                    const id = input.getAttribute('data-id');
                    let value = parseInt(input.value);

                    if (isNaN(value) || value < 1) {
                        value = 1;
                    }

                    if (value > 33) {
                        value = 33;
                        Swal.fire('Thông báo', 'Số lượng tối đa là 33 sản phẩm!', 'info');
                    }

                    input.value = value;
                    updateCart(id, value);
                }
            });
            document.body.addEventListener('input', function (e) {
                if (e.target.classList.contains('qty-input')) {
                    let value = parseInt(e.target.value);
                    if (value > 33) {
                        e.target.value = 33;
                    }
                    if (value < 1) {
                        e.target.value = 1;
                    }
                }
            });
            function calculateFinalTotal() {
                let totalQty = 0;
                let totalPrice = 0;
                let checkedItems = 0;
                const checkboxes = document.querySelectorAll('.item-checkbox');
                var cnt = 0;
                checkboxes.forEach(cb => {
                    if (cb.checked) {
                        const id = cb.getAttribute('data-id');
                        const qty = parseInt(document.getElementById('qty-' + id).value);
                        const price = parseFloat(document.getElementById('subtotal-' + id).getAttribute('data-price'));
                        cnt++;
                        totalQty += qty;
                        totalPrice += (price * qty);
                        checkedItems++;
                    }
                });

                document.getElementById('total-qty').innerText = cnt;
                document.getElementById('total-price').innerText = new Intl.NumberFormat('vi-VN').format(totalPrice) + '₫';

                document.getElementById('btn-order').onclick = function () {
                    const selectedIds = [];
                    document.querySelectorAll('.item-checkbox:checked').forEach(cb => {
                        selectedIds.push(cb.getAttribute('data-id'));
                    });

                    if (selectedIds.length === 0) {
                        Swal.fire('Thông báo', 'Vui lòng chọn ít nhất một sản phẩm để đặt hàng!', 'info');
                        return;
                    }

                    window.location.href = '/checkout?ids=' + selectedIds.join(',');
                };

                const selectAll = document.getElementById('select-all');
                if (selectAll) selectAll.checked = (checkedItems === checkboxes.length && checkboxes.length > 0);
            }

            document.body.addEventListener('change', function (e) {
                if (e.target.id === 'select-all') {
                    document.querySelectorAll('.item-checkbox').forEach(cb => cb.checked = e.target.checked);
                    calculateFinalTotal();
                }
                if (e.target.classList.contains('item-checkbox')) {
                    calculateFinalTotal();
                }
            });

            async function updateCart(productId, quantity) {
                try {
                    const response = await fetch('/cart/update', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrfToken },
                        body: JSON.stringify({ productId: productId, quantity: quantity })
                    });
                    if (response.ok) {
                        const cart = await response.json();
                        renderUI(cart, productId);
                    }
                } catch (err) { console.error("Lỗi:", err); }
            }

            function renderUI(cart, updatedId) {
                const itemData = cart.items.find(i => i.productId == updatedId);
                const itemRow = document.getElementById('item-row-' + updatedId);

                if (!itemData) {
                    if (itemRow) itemRow.remove();
                    
                    if (cart.items.length === 0) {
                        const emptyCart = document.getElementById('empty-cart');
                        const cartContent = document.getElementById('cart-content');
                        if (emptyCart) emptyCart.classList.remove('d-none');
                        if (cartContent) cartContent.classList.add('d-none');
                    }
                } else {
                    document.getElementById('qty-' + updatedId).value = itemData.quantity;
                    const subtotal = document.getElementById('subtotal-' + updatedId);
                    subtotal.innerText = new Intl.NumberFormat('vi-VN').format(itemData.price * itemData.quantity) + '₫';
                }
                
                // Cập nhật số lượng trên header nếu có
                const cartCount = document.getElementById('cart-count');
                if (cartCount) {
                    cartCount.innerText = cart.totalQuantity || cart.items.length;
                }
                
                calculateFinalTotal();
            }

            document.body.addEventListener('click', function (e) {
                const btnUpdate = e.target.closest('.btn-update');
                if (btnUpdate) {
                    const id = btnUpdate.getAttribute('data-id');
                    const action = btnUpdate.getAttribute('data-action');
                    const input = document.getElementById('qty-' + id);
                    let val = parseInt(input.value);

                    if (action === 'minus') {
                        if (val <= 1) return;
                        val = val - 1;
                    }

                    if (action === 'plus') {
                        if (val >= 33) {
                            Swal.fire('Thông báo', 'Số lượng tối đa là 33 sản phẩm!', 'info');
                            return;
                        }
                        val = val + 1;
                    }

                    updateCart(id, val);
                }

                const btnDelete = e.target.closest('.btn-delete');
                if (btnDelete) {
                    const id = btnDelete.getAttribute('data-id');
                    Swal.fire({
                        title: 'Xóa món này?',
                        text: "Món ăn sẽ được loại bỏ khỏi giỏ hàng",
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonColor: '#d33',
                        confirmButtonText: 'Xóa ngay',
                        cancelButtonText: 'Hủy'
                    }).then((result) => {
                        if (result.isConfirmed) updateCart(id, 0);
                    });
                }
            });

            calculateFinalTotal();
        });