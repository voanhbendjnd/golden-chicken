 document.querySelectorAll('.product-card').forEach(card => {

            card.addEventListener("mousemove", (e) => {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;

                const centerX = rect.width / 2;
                const centerY = rect.height / 2;

                const rotateX = ((y - centerY) / 40) * -1;
                const rotateY = (x - centerX) / 40;

                card.style.transform =
                    `rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.05)`;
            });

            card.addEventListener("mouseleave", () => {
                card.style.transform =
                    "rotateX(0deg) rotateY(0deg) scale(1)";
            });

        });
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

            document.querySelectorAll('.btn-add-to-cart').forEach(btn => {
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
                            Swal.fire({
                                title: 'Success',
                                text: 'Add to cart success',
                                icon: 'success',
                                showCancelButton: true,
                                confirmButtonText: 'Go to cart',
                                cancelButtonText: 'Stay here',
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
        });