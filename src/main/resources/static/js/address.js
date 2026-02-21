document.addEventListener('DOMContentLoaded', function () {
    var districtSelect = document.querySelector('select[name="district"]');
    var wardSelect = document.querySelector('select[name="ward"]');

    if (!districtSelect || !wardSelect) {
        return;
    }

    var initialDistrict = districtSelect.dataset.selected || '';
    var initialWard = wardSelect.dataset.selected || '';

    function setOptions(select, options, placeholder) {
        select.innerHTML = '';
        var placeholderOption = document.createElement('option');
        placeholderOption.value = '';
        placeholderOption.textContent = placeholder;
        select.appendChild(placeholderOption);

        options.forEach(function (value) {
            var opt = document.createElement('option');
            opt.value = value;
            opt.textContent = value;
            select.appendChild(opt);
        });
    }

    function setState(select, options, placeholder, disabled) {
        setOptions(select, options || [], placeholder);
        select.disabled = !!disabled;
    }

    function normalize(text) {
        if (!text) return '';
        return text
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase()
            .trim();
    }

    setState(districtSelect, [], 'Đang tải Quận/Huyện...', true);
    setState(wardSelect, [], 'Vui lòng chọn Quận/Huyện trước', true);

    fetch('https://provinces.open-api.vn/api/?depth=3')
        .then(function (res) { return res.json(); })
        .then(function (provinces) {
            var target = provinces.find(function (p) {
                var name = normalize(p.name);
                return name === 'can tho' || name.indexOf('can tho') >= 0;
            });

            if (!target) {
                throw new Error('Can Tho not found');
            }

            var districts = Array.isArray(target.districts) ? target.districts : [];
            var districtNames = districts.map(function (d) { return d.name; });

            setState(districtSelect, districtNames, '-- Chọn Quận/Huyện --', false);

            if (initialDistrict) {
                districtSelect.value = initialDistrict;
            }

            function renderWards(districtName) {
                if (!districtName) {
                    setState(wardSelect, [], '-- Chọn Phường/Xã --', true);
                    return;
                }

                var district = districts.find(function (d) { return d.name === districtName; });
                var wards = district && Array.isArray(district.wards) ? district.wards : [];
                var wardNames = wards.map(function (w) { return w.name; });

                setState(wardSelect, wardNames, '-- Chọn Phường/Xã --', false);

                if (initialWard && districtName === initialDistrict) {
                    wardSelect.value = initialWard;
                }
            }

            renderWards(districtSelect.value);

            districtSelect.addEventListener('change', function () {
                renderWards(districtSelect.value);
            });
        })
        .catch(function (err) {
            console.error('Address API error:', err);
            setState(districtSelect, [], 'Không tải được Quận/Huyện', true);
            setState(wardSelect, [], 'Không tải được Phường/Xã', true);
        });
});
