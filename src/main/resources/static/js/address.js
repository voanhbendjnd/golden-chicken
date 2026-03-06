document.addEventListener('DOMContentLoaded', function () {

    var wardSelect = document.querySelector('select[name="ward"]');
    if (!wardSelect) return;

    var initialWard = wardSelect.dataset.selected || '';

    function setOptions(select, items, placeholder) {
        select.innerHTML = '';

        var ph = document.createElement('option');
        ph.value = '';
        ph.textContent = placeholder;
        select.appendChild(ph);

        items.forEach(function (item) {
            var opt = document.createElement('option');
            opt.value = item;
            opt.textContent = item;
            select.appendChild(opt);
        });
    }

    fetch('https://provinces.open-api.vn/api/v2/p/92?depth=2')
        .then(res => res.json())
        .then(function (data) {

            var wards = [];

            if (data.wards) {
                wards = data.wards;
            }
            else if (data.districts) {
                data.districts.forEach(function (d) {
                    if (d.wards) {
                        wards = wards.concat(d.wards);
                    }
                });
            }

            var wardNames = wards.map(function (w) {
                return w.name;
            });

            setOptions(wardSelect, wardNames, '-- Chọn phường/xã --');

            if (initialWard) {
                wardSelect.value = initialWard;
            }
        })
        .catch(function (err) {
            console.error(err);
            setOptions(wardSelect, [], 'Không tải được phường/xã');
        });
});