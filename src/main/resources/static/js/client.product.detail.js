       document.querySelectorAll(".add-to-cart").forEach(button => {
            button.addEventListener("click", function () {

                const productCard = this.closest(".row");
                const img = document.querySelector(".product-img-main"); // ảnh chính
                const cartIcon = document.querySelector(".fa-cart-shopping");

                if (!img || !cartIcon) return;

                const imgRect = img.getBoundingClientRect();
                const cartRect = cartIcon.getBoundingClientRect();

                const flyingImg = img.cloneNode(true);
                flyingImg.classList.add("fly-img");

                flyingImg.style.top = imgRect.top + "px";
                flyingImg.style.left = imgRect.left + "px";

                document.body.appendChild(flyingImg);

                setTimeout(() => {
                    flyingImg.style.top = cartRect.top + "px";
                    flyingImg.style.left = cartRect.left + "px";
                    flyingImg.style.width = "20px";
                    flyingImg.style.height = "20px";
                    flyingImg.style.opacity = "0.5";
                }, 10);

                setTimeout(() => {
                    flyingImg.remove();
                    cartIcon.classList.add("cart-shake");
                    setTimeout(() => cartIcon.classList.remove("cart-shake"), 400);
                }, 800);
            });
        });

        const style = document.createElement("style");
        style.innerHTML = `
                @keyframes shake {
                    0 % { transform: translateX(0); }
  25% {transform: translateX(-4px); }
                50% {transform: translateX(4px); }
                75% {transform: translateX(-4px); }
                100% {transform: translateX(0); }
}
                .cart-shake {
                    animation: shake 0.4s ease;
}
                `;
        document.head.appendChild(style);
        document.addEventListener('DOMContentLoaded', function () {

            document.querySelectorAll('.btn-minus').forEach(btn => {
                btn.onclick = function () {
                    let input = this.parentElement.querySelector('.input-qty');
                    let value = parseInt(input.value);

                    if (value > 1) {
                        input.value = value - 1;
                    }
                }
            });

            document.querySelectorAll('.btn-plus').forEach(btn => {
                btn.onclick = function () {
                    let input = this.parentElement.querySelector('.input-qty');
                    let value = parseInt(input.value);

                    if (value >= 33) {
                        Swal.fire('Thông báo', 'Số lượng tối đa là 33 sản phẩm!', 'info');
                        return;
                    }

                    input.value = value + 1;
                }
            });
            document.querySelectorAll('.input-qty').forEach(input => {
                input.addEventListener('input', function () {
                    let value = parseInt(this.value);

                    if (isNaN(value) || value < 1) {
                        this.value = 1;
                    }

                    if (value > 33) {
                        this.value = 33;
                        Swal.fire('Thông báo', 'Số lượng tối đa là 33 sản phẩm!', 'info');
                    }
                });
            });
            document.querySelectorAll('.add-to-cart').forEach(btn => {
                btn.onclick = async function () {
                    const productId = this.getAttribute('data-id');
                    const quantity = document.getElementById('qty-' + productId).value;

                    const cartDTO = {
                        productId: parseInt(productId),
                        quantity: parseInt(quantity)
                    };

                    try {
                        const response = await fetch('/cart/add', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
                            },
                            body: JSON.stringify(cartDTO)
                        });

                        if (response.ok) {
                            const newTotal = await response.json();
                            const cartCountElement = document.getElementById('cart-count');
                            if (cartCountElement) {
                                cartCountElement.innerText = newTotal;
                            }
                            Swal.fire({
                                title: 'Thành công',
                                text: 'Thêm vào giỏ hàng thành công',
                                icon: 'success',
                                showCancelButton: true,
                                confirmButtonText: 'Đi tới giỏ hàng',
                                cancelButtonText: 'Ở lại',
                                reverseButtons: true,
                                showClass: {
                                    popup: 'animate__animated animate__fadeInDown'
                                },
                                hideClass: {
                                    popup: 'animate__animated animate__fadeOutUp'
                                }
                            }).then((result) => {
                                if (result.isConfirmed) {
                                    window.location.href = '/cart';
                                }
                            });
                        } else if (response.status === 403) {
                            alert('Vui lòng đăng nhập với tài khoản khách hàng để sử dụng giỏ hàng!');
                        } else {
                            alert('Có lỗi xảy ra, vui lòng thử lại.');
                        }
                    } catch (error) {
                        console.error('Error:', error);
                        alert('Không thể kết nối đến máy chủ!');
                    }
                };
            });
            document.querySelectorAll('.btn-buy-now').forEach(btn => {
                btn.onclick = function (e) {
                    const productId = this.getAttribute('data-id');
                    const qtyInput = document.getElementById('qty-' + productId);
                    if (qtyInput) {
                        const quantity = qtyInput.value;
                        const originalHref = this.getAttribute('href');
                        const urlParts = originalHref.split('?');
                        const baseUrl = urlParts[0];
                        let params = new URLSearchParams(urlParts[1] || '');
                        params.set('quantity', quantity);
                        params.set('productId', productId);
                        this.setAttribute('href', baseUrl + '?' + params.toString());
                    }
                };
            });
        });
        function changeImg(element) {
            document.getElementById('mainImg').src = element.src;
            document.querySelectorAll('.thumb-img').forEach(img => img.classList.remove('active'));
            element.classList.add('active');
        }

        function updateQty(step) {
            const input = document.getElementById('qtyInput');
            let currentVal = parseInt(input.value);
            if (!isNaN(currentVal)) {
                let newVal = currentVal + step;
                if (newVal >= 1) input.value = newVal;
            }
        }