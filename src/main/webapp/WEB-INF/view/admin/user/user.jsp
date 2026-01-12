<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Create User Page</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            </head>

            <body>
                <div class="container mt-5">
                    <div class="row justify-content-center">
                        <div class="col-md-8">
                            <div class="card">
                                <div class="card-header">
                                    <h3 class="text-center">Create New User</h3>
                                </div>
                                <div class="card-body">
                                    <form:form method="post" action="/admin/user/create" modelAttribute="newUser">
                                        <div class="mb-3">
                                            <label class="form-label">Email:</label>
                                            <form:input type="email" class="form-control" path="email"
                                                required="true" />
                                            <form:errors path="email" style="color: red;" />
                                        </div>



                                        <div class="mb-3">
                                            <label class="form-label">Full Name:</label>
                                            <form:input type="text" class="form-control" path="fullName"
                                                required="true" />
                                            <form:errors path="fullName" style="color: red;" />

                                        </div>

                                        <div class="mb-3">
                                            <label class="form-label">Phone Number:</label>
                                            <form:input type="text" class="form-control" path="phone" />
                                            <form:errors path="phone" style="color: red;" />

                                        </div>

                                        <div class="mb-3">
                                            <label class="form-label">Password:</label>
                                            <form:input type="password" class="form-control" path="password"
                                                required="true" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">Confirm Password:</label>
                                            <form:input type="password" class="form-control" path="confirmPassword"
                                                required="true" />
                                        </div>
                                        <div class="mb-3">
                                            <labal class="form-label">Role</labal>
                                            <select id="roleSelect" name="roleId" class="form-select" required>
                                                <option value="">Select Role</option>
                                                <c:forEach var="role" items="${roles}">
                                                    <option value="${role.id}">${role.name}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div id="addressGroup" style="display: none;">
                                            <div class="row mb-3">
                                                <div class="col-md-4">
                                                    <label class="form-label">Tỉnh/Thành phố:</label>
                                                    <select class="form-select" disabled>
                                                        <option>TP. Cần Thơ</option>
                                                    </select>
                                                </div>
                                                <div class="col-md-4">
                                                    <label class="form-label">Quận/Huyện:</label>
                                                    <form:select id="district" path="district" class="form-select">
                                                        <option value="">Chọn Quận Huyện</option>
                                                    </form:select>
                                                </div>
                                                <div class="col-md-4">
                                                    <label class="form-label">Phường/Xã:</label>
                                                    <form:select id="ward" path="ward" class="form-select">
                                                        <option value="">Chọn Phường Xã</option>
                                                    </form:select>
                                                </div>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">Số nhà, tên đường:</label>
                                                <form:input path="address" class="form-control"
                                                    placeholder="Example: 123 Street ABC..." />
                                            </div>
                                        </div>

                                        <div id="staffTypeGroup" class="mb-3" style="display: none;">
                                            <label class="form-label">Staff Type:</label>
                                            <form:select path="staffType" class="form-select">
                                                <form:option value="RECEPTIONIST">Receptionist</form:option>
                                                <form:option value="SHIPPER">Shipper</form:option>
                                                <form:option value="MANAGER">Manager</form:option>
                                            </form:select>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">Status:</label>
                                            <form:select path="status" class="form-select">
                                                <form:option value="true">Active</form:option>
                                                <form:option value="false">Non-active</form:option>
                                            </form:select>
                                            <form:errors path="status" cssClass="text-danger" />
                                        </div>



                                        <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                                            <a href="/admin/user" class="btn btn-secondary me-md-2">Cancel</a>
                                            <button type="submit" class="btn btn-primary">Create User</button>
                                        </div>
                                    </form:form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
            </body>
            <script>
                const canThoData = {
                    "Quận Ninh Kiều": ["Phường An Bình", "Phường An Cư", "Phường An Hòa", "Phường An Khánh", "Phường An Nghiệp", "Phường An Phú", "Phường Cái Khế", "Phường Hưng Lợi", "Phường Tân An", "Phường Thới Bình", "Phường Xuân Khánh"],
                    "Quận Cái Răng": ["Phường Ba Láng", "Phường Hưng Phú", "Phường Hưng Thạnh", "Phường Lê Bình", "Phường Phú Thứ", "Phường Tân Phú", "Phường Thường Thạnh"],
                    "Quận Bình Thủy": ["Phường An Thới", "Phường Bình Thủy", "Phường Bùi Hữu Nghĩa", "Phường Long Hòa", "Phường Long Tuyền", "Phường Thới An Đông", "Phường Trà An", "Phường Trà Nóc"],
                    "Quận Ô Môn": ["Phường Châu Văn Liêm", "Phường Phước Thới", "Phường Thới An", "Phường Thới Hòa", "Phường Thới Long", "Phường Trường Lạc"],
                    "Quận Thốt Nốt": ["Phường Tân Hưng", "Phường Tân Lộc", "Phường Thạnh Hòa", "Phường Thốt Nốt", "Phường Thuận An", "Phường Thuận Hưng", "Phường Thới Thuận", "Phường Trung Kiên", "Phường Trung Nhất"],
                    "Huyện Phong Điền": ["Thị trấn Phong Điền", "Xã Giai Xuân", "Xã Mỹ Khánh", "Xã Nhơn Ái", "Xã Nhơn Nghĩa", "Xã Tân Thới", "Xã Trường Long"],
                    "Huyện Thới Lai": ["Thị trấn Thới Lai", "Xã Định Môn", "Xã Đông Bình", "Xã Đông Thuận", "Xã Tân Thạnh", "Xã Thới Tân", "Xã Thới Thạnh", "Xã Trường Thành"],
                    "Huyện Cờ Đỏ": ["Thị trấn Cờ Đỏ", "Xã Đông Hiệp", "Xã Đông Thắng", "Xã Thạnh Phú", "Xã Thới Đông", "Xã Thới Hung", "Xã Thới Xuân", "Xã Trung An", "Xã Trung Hưng", "Xã Trung Thạnh"],
                    "Huyện Vĩnh Thạnh": ["Thị trấn Vĩnh Thạnh", "Thị trấn Thạnh An", "Xã Thạnh An", "Xã Thạnh Lộc", "Xã Thạnh Mỹ", "Xã Thạnh Quới", "Xã Thạnh Tiến", "Xã Vĩnh Bình", "Xã Vĩnh Trinh"]
                };
                document.addEventListener("DOMContentLoaded", function () {
                    const roleSelect = document.getElementById("roleSelect");
                    const addressGroup = document.getElementById("addressGroup");
                    const staffTypeGroup = document.getElementById("staffTypeGroup");
                    const districtSelect = document.getElementById("district");
                    const wardSelect = document.getElementById("ward");

                    // 1. Logic ẩn/hiện Form theo Role
                    function toggleFieldsByRole() {
                        if (!roleSelect) return;
                        const selectedText = roleSelect.options[roleSelect.selectedIndex].text.toUpperCase();
                        if (selectedText === "CUSTOMER") {
                            addressGroup.style.display = "block";
                            staffTypeGroup.style.display = "none";
                        } else if (selectedText === "STAFF" || selectedText === "ADMIN") {
                            addressGroup.style.display = "none";
                            staffTypeGroup.style.display = "block";
                        } else {
                            addressGroup.style.display = "none";
                            staffTypeGroup.style.display = "none";
                        }
                    }

                    // 2. Đổ dữ liệu Quận/Huyện Cần Thơ
                    function loadCanThoDistricts() {
                        districtSelect.innerHTML = '<option value="">-- Chọn Quận/Huyện --</option>';
                        for (let district in canThoData) {
                            let opt = document.createElement("option");
                            opt.value = district;
                            opt.innerHTML = district;
                            districtSelect.appendChild(opt);
                        }
                    }

                    // 3. Sự kiện khi chọn Quận -> Đổ Phường/Xã
                    districtSelect.addEventListener("change", function () {
                        wardSelect.innerHTML = '<option value="">-- Chọn Phường/Xã --</option>';
                        const wards = canThoData[this.value];
                        if (wards) {
                            wards.forEach(ward => {
                                let opt = document.createElement("option");
                                opt.value = ward;
                                opt.innerHTML = ward;
                                wardSelect.appendChild(opt);
                            });
                        }
                    });

                    roleSelect.addEventListener("change", toggleFieldsByRole);

                    // Khởi tạo
                    toggleFieldsByRole();
                    loadCanThoDistricts();
                });
            </script>

            </html>