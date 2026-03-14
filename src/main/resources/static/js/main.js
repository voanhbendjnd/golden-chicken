/* --- Autocomplete Search Logic --- */
$(document).ready(function () {
    const $searchInput = $('#modalSearchInput');
    const $suggestionBox = $('#modalSuggestionBox');
    const $submitBtn = $('#modalSearchSubmit');

    let debounceTimer = null;

    function goToMenuSearch(raw) {
        const q = (raw || '').trim();
        if (!q) return;
        window.location.href = `/menu?q=${encodeURIComponent(q)}`;
    }

    $searchInput.on('input', function () {
        const query = $(this).val().trim();

        // Bước 1: Kiểm tra độ dài từ khóa (ít nhất 1 từ)
        if (query.length < 1) {
            $suggestionBox.addClass('d-none');
            return;
        }

        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            // Bước 2: Gọi API trả về JSON
            fetch(`/product/suggestions?query=${encodeURIComponent(query)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.length > 0) {
                        let html = '';
                        data.forEach(product => {
                            // Highlight từ khóa khớp
                            const regex = new RegExp(`(${query})`, 'gi');
                            const highlightedName = product.name.replace(regex, '<b>$1</b>');

                            html += `
                                <div class="suggestion-item" onclick="window.location.href='/product/${product.id}'">
                                    <i class="fa fa-search"></i>
                                    <span>${highlightedName}</span>
                                </div>
                            `;
                        });
                        $suggestionBox.html(html).removeClass('d-none');
                    } else {
                        $suggestionBox.addClass('d-none');
                    }
                })
                .catch(error => console.error('Error fetching suggestions:', error));
        }, 150);
    });

    $searchInput.on('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            goToMenuSearch($searchInput.val());
        }
    });

    $submitBtn.on('click', function () {
        goToMenuSearch($searchInput.val());
    });

    // Bước 3: Đóng hộp gợi ý khi click ra ngoài
    $(document).on('click', function (e) {
        if (!$searchInput.is(e.target) && !$suggestionBox.is(e.target) && $suggestionBox.has(e.target).length === 0) {
            $suggestionBox.addClass('d-none');
        }
    });
});

/* --- Home hero search: Enter/bấm tìm => sang menu và lọc --- */
$(document).ready(function () {
    const $heroInput = $('#heroSearchInput');
    const $heroSuggestionBox = $('#heroSuggestionBox');
    const $heroSubmit = $('#heroSearchSubmit');

    let heroDebounceTimer = null;

    function goToMenuSearch(raw) {
        const q = (raw || '').trim();
        if (!q) return;
        window.location.href = `/menu?q=${encodeURIComponent(q)}`;
    }

    if ($heroInput.length) {
        $heroInput.on('input', function () {
            const query = $(this).val().trim();
            if (query.length < 1) {
                $heroSuggestionBox.addClass('d-none');
                return;
            }

            clearTimeout(heroDebounceTimer);
            heroDebounceTimer = setTimeout(() => {
                fetch(`/product/suggestions?query=${encodeURIComponent(query)}`)
                    .then(response => response.json())
                    .then(data => {
                        if (data.length > 0) {
                            let html = '';
                            data.forEach(product => {
                                const regex = new RegExp(`(${query})`, 'gi');
                                const highlightedName = product.name.replace(regex, '<b>$1</b>');
                                html += `
                                    <div class="suggestion-item" onclick="window.location.href='/product/${product.id}'">
                                        <i class="fa fa-search"></i>
                                        <span>${highlightedName}</span>
                                    </div>
                                `;
                            });
                            $heroSuggestionBox.html(html).removeClass('d-none');
                        } else {
                            $heroSuggestionBox.addClass('d-none');
                        }
                    })
                    .catch(error => console.error('Error fetching hero suggestions:', error));
            }, 150);
        });

        $heroInput.on('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                goToMenuSearch($heroInput.val());
            }
        });

        $heroSubmit.on('click', function () {
            goToMenuSearch($heroInput.val());
        });

        $(document).on('click', function (e) {
            if (!$heroInput.is(e.target) && !$heroSuggestionBox.is(e.target) && $heroSuggestionBox.has(e.target).length === 0) {
                $heroSuggestionBox.addClass('d-none');
            }
        });
    }
});

(function ($) {
    "use strict";

    // Spinner
    var spinner = function () {
        setTimeout(function () {
            if ($('#spinner').length > 0) {
                $('#spinner').removeClass('show');
            }
        }, 1);
    };
    spinner(0);

    $(document).ready(function () {


    // Fixed Navbar
    $(window).scroll(function () {
        if ($(window).width() < 992) {
            if ($(this).scrollTop() > 55) {
                $('.fixed-top').addClass('shadow');
            } else {
                $('.fixed-top').removeClass('shadow');
            }
        } else {
            if ($(this).scrollTop() > 55) {
                $('.fixed-top').addClass('shadow').css('top', -55);
            } else {
                $('.fixed-top').removeClass('shadow').css('top', 0);
            }
        } 
    });
    
    
   // Back to top button
   $(window).scroll(function () {
    if ($(this).scrollTop() > 300) {
        $('.back-to-top').fadeIn('slow');
    } else {
        $('.back-to-top').fadeOut('slow');
    }
    });
    $('.back-to-top').click(function () {
        $('html, body').animate({scrollTop: 0}, 1500, 'easeInOutExpo');
        return false;
    });


    // Testimonial carousel
    $(".testimonial-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 2000,
        center: false,
        dots: true,
        loop: true,
        margin: 25,
        nav : true,
        navText : [
            '<i class="bi bi-arrow-left"></i>',
            '<i class="bi bi-arrow-right"></i>'
        ],
        responsiveClass: true,
        responsive: {
            0:{
                items:1
            },
            576:{
                items:1
            },
            768:{
                items:1
            },
            992:{
                items:2
            },
            1200:{
                items:2
            }
        }
    });


    // vegetable carousel
    $(".vegetable-carousel").owlCarousel({
        autoplay: true,
        smartSpeed: 1500,
        center: false,
        dots: true,
        loop: true,
        margin: 25,
        nav : true,
        navText : [
            '<i class="bi bi-arrow-left"></i>',
            '<i class="bi bi-arrow-right"></i>'
        ],
        responsiveClass: true,
        responsive: {
            0:{
                items:1
            },
            576:{
                items:1
            },
            768:{
                items:2
            },
            992:{
                items:3
            },
            1200:{
                items:4
            }
        }
    });


    // Modal Video
    $(document).ready(function () {
        var $videoSrc;
        $('.btn-play').click(function () {
            $videoSrc = $(this).data("src");
        });
        console.log($videoSrc);

        $('#videoModal').on('shown.bs.modal', function (e) {
            $("#video").attr('src', $videoSrc + "?autoplay=1&amp;modestbranding=1&amp;showinfo=0");
        })

        $('#videoModal').on('hide.bs.modal', function (e) {
            $("#video").attr('src', $videoSrc);
        })
    });



    // Product Quantity
    $('.quantity button').on('click', function () {
        var button = $(this);
        var oldValue = button.parent().parent().find('input').val();
        if (button.hasClass('btn-plus')) {
            var newVal = parseFloat(oldValue) + 1;
        } else {
            if (oldValue > 0) {
                var newVal = parseFloat(oldValue) - 1;
            } else {
                newVal = 0;
            }
        }
        button.parent().parent().find('input').val(newVal);
    });
    });

})(jQuery);
