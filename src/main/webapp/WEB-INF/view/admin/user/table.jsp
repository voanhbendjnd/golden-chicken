<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html>

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>User List</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        </head>

        <body>
            <jsp:include page="..//layout/header.jsp" />
            <div class="container-fluid">
                <div class="row">
                    <div class="col-md-3 col-lg-2 p-0">
                        <jsp:include page="..//layout/sidebar.jsp" />
                    </div>
                    <main class="col-md-9 col-lg-10 ms-sm-auto px-md-4">
                        <div class="container mt-5">
                            <div class="row">
                                <div class="col-12">
                                    <div class="card">
                                        <div class="card-header d-flex justify-content-between align-items-center">
                                            <h3>User Management</h3>
                                            <a href="/admin/user/create" class="btn btn-primary">Create New User</a>
                                        </div>
                                        <div class="card-body">
                                            <div class="row mb-3">
                                                <div class="col-md-6">
                                                    <div class="input-group">
                                                        <input type="text" id="searchFullName" class="form-control"
                                                            placeholder="Search by full name..."
                                                            value="${param.fullName}">
                                                        <button class="btn btn-outline-primary" type="button"
                                                            onclick="handleSearch()">
                                                            Search
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                            <table class="table table-striped">
                                                <thead>
                                                    <tr>
                                                        <th>ID</th>
                                                        <th>Email</th>
                                                        <th>Full Name</th>
                                                        <th>Phone</th>
                                                        <th>Status</th>
                                                        <th>Actions</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="user" items="${users}">
                                                        <tr>
                                                            <td>
                                                                <a href="/admin/user/${user.id}">${user.id}</a>

                                                            </td>
                                                            <td>${user.email}</td>
                                                            <td>${user.fullName}</td>
                                                            <td>${user.phone}</td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${user.status}">
                                                                        <span class="badge bg-success">Active</span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="badge bg-danger">Non-Active</span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td>
                                                                <div style="display: flex; gap:6px">
                                                                    <a href="/admin/user/update/${user.id}"
                                                                        class="btn btn-sm btn-warning">Edit</a>
                                                                    <form action="/admin/user/delete/${user.id}"
                                                                        method="POST">
                                                                        <button type="submit"
                                                                            class="btn btn-sm btn-danger">Delete</button>
                                                                        <input type="hidden"
                                                                            name="${_csrf.parameterName}"
                                                                            value="${_csrf.token}" />
                                                                    </form>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                            <div class="d-flex justify-content-center mt-3">
                                                <nav aria-label="Page navigation">
                                                    <ul class="pagination">
                                                        <li class="page-item ${meta.page == 1 ? 'disabled' : ''}">
                                                            <c:url var="prevUrl" value="/admin/user">
                                                                <c:param name="page" value="${meta.page - 1}" />
                                                                <c:param name="size" value="${meta.pageSize}" />
                                                                <c:if test="${not empty param.fullName}">
                                                                    <c:param name="fullName"
                                                                        value="${param.fullName}" />
                                                                </c:if>
                                                            </c:url>
                                                            <a class="page-link" href="${prevUrl}">Previous</a>
                                                        </li>
                                                        <c:forEach begin="1" end="${meta.pages}" var="p">
                                                            <li class="page-item ${meta.page == p ? 'active' : ''}">
                                                                <c:url var="pageUrl" value="/admin/user">
                                                                    <c:param name="page" value="${p}" />
                                                                    <c:param name="size" value="${meta.pageSize}" />
                                                                    <c:if test="${not empty param.fullName}">
                                                                        <c:param name="fullName"
                                                                            value="${param.fullName}" />
                                                                    </c:if>
                                                                </c:url>
                                                                <a class="page-link" href="${pageUrl}">${p}</a>
                                                            </li>
                                                        </c:forEach>

                                                        <li
                                                            class="page-item ${meta.page == meta.pages ? 'disabled' : ''}">
                                                        </li>
                                                        <c:url var="nextUrl" value="/admin/user">
                                                            <c:param name="page" value="${meta.page + 1}" />
                                                            <c:param name="size" value="${meta.pageSize}" />
                                                            <c:if test="${not empty param.fullName}">
                                                                <c:param name="fullName" value="${param.fullName}" />
                                                            </c:if>
                                                        </c:url>
                                                        <a class="page-link" href="${nextUrl}">Next</a>
                                                        </li>
                                                    </ul>
                                                </nav>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </main>
                </div>

            </div>



            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
            <script>
                console.log("Search script loaded");

                function handleSearch() {
                    try {
                        const searchInput = document.getElementById("searchFullName");
                        if (!searchInput) {
                            console.error("Search input not found!");
                            return;
                        }

                        const searchTerm = searchInput.value.trim();
                        console.log("Searching for:", searchTerm);

                        const currentUrl = new URL(window.location);
                        const params = new URLSearchParams(currentUrl.search);

                        // Use simple fullName parameter instead of filter for now
                        if (searchTerm) {
                            params.set('fullName', searchTerm);
                        } else {
                            params.delete('fullName');
                        }

                        // Reset to first page when searching
                        params.set('page', '1');

                        // Keep other parameters like size
                        if (!params.has('size')) {
                            params.set('size', '5');
                        }

                        const newUrl = `${currentUrl.pathname}?${params.toString()}`;
                        console.log("Navigating to:", newUrl);

                        window.location.href = newUrl;

                    } catch (error) {
                        console.error("Error in handleSearch:", error);
                        alert("An error occurred while searching. Please try again.");
                    }
                }



                document.addEventListener("DOMContentLoaded", function () {
                    console.log("DOM loaded, setting up search");
                    const searchInput = document.getElementById("searchFullName");
                    if (searchInput) {
                        searchInput.addEventListener("keypress", function (event) {
                            if (event.key === "Enter") {
                                event.preventDefault();
                                handleSearch();
                            }
                        });
                    }
                });
            </script>
        </body>

        </html>