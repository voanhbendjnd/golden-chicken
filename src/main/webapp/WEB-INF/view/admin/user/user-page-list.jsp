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
                                                                <a href="/admin/user/update/${user.id}"
                                                                    class="btn btn-sm btn-warning">Edit</a>
                                                                <a href="/admin/user/delete/${user.id}"
                                                                    class="btn btn-sm btn-danger"
                                                                    onclick="return confirm('Are you sure?')">Delete</a>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                            <div class="d-flex justify-content-center mt-3">
                                                <nav aria-label="Page navigation">
                                                    <ul class="pagination">
                                                        <li class="page-item ${meta.page == 1 ? 'disabled' : ''}">
                                                            <a class="page-link"
                                                                href="?page=${meta.page - 1}">Previous</a>
                                                        </li>
                                                        <c:forEach begin="1" end="${meta.pages}" var="p">
                                                            <li class="page-item ${meta.page == p ? 'active' : ''}">
                                                                <c:url var="pageUrl" value="/admin/user">
                                                                    <c:param name="page" value="${p}" />
                                                                    <c:param name="size" value="${meta.pageSize}" />
                                                                    <c:if test="${not empty param.filter}">
                                                                        <c:param name="filter"
                                                                            value="${param.filter}" />
                                                                    </c:if>
                                                                </c:url>
                                                                <a class="page-link" href="${pageUrl}">${p}</a>
                                                                <!-- <a class="page-link"
                                                                    href="?page=${p}&size=${meta.pageSize}${not empty param.filter ? '&filter='.concat(param.filter): ''}">${p}</a> -->
                                                            </li>
                                                        </c:forEach>

                                                        <li
                                                            class="page-item ${meta.page == meta.pages ? 'disabled' : ''}">
                                                            <a class="page-link" href="?page=${meta.page + 1}">Next</a>
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
                function handleSearch() {
                    const name = document.getElementById("searchFullName").value;
                    const urlParams = new URLSearchParams(window.location.search);

                    if (name && name.trim() !== "") {
                        urlParams.set('filter', "fullName ~~ '" + name.trim() + "'");
                    } else {
                        urlParams.delete('filter');
                    }

                    urlParams.set('page', '1');

                    window.location.href = window.location.pathname + "?" + urlParams.toString();
                }
                document.getElementById("searchFullName").addEventListener("keypress", function (event) {
                    if (event.key === "Enter") {
                        handleSearch();
                    }
                });
            </script>
        </body>

        </html>