<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html>

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Permission Table</title>
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
                                            <h3>Permission Management</h3>
                                            <a href="/admin/permission/create" class="btn btn-primary">Create New
                                                Permission</a>
                                        </div>
                                        <div class="card-body">
                                            <div class="row mb-3">
                                                <div class="col-md-6">
                                                    <div class="input-group">
                                                        <input type="text" id="searchName" class="form-control"
                                                            placeholder="Search by name..."
                                                            value="${param.filter != null ? param.filter.split('\'')[1] : ''}">
                                                        <button class="btn btn-outline-primary" type="button"
                                                            onclick="handleSearch()">
                                                            Search
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                            <form action="/admin/permission/import" method="post"
                                                enctype="multipart/form-data">
                                                <input type="hidden" name="${_csrf.parameterName}"
                                                    value="${_csrf.token}" />

                                                <div class="mb-3">
                                                    <label class="form-label">Choose file Excel (.xlsx)</label>
                                                    <div style="font-style: italic;">Ex: Name, API Path, Method, Module
                                                    </div>
                                                    <input type="file" name="file" class="form-control"
                                                        accept=".xlsx, .xls" required />
                                                </div>
                                                <button type="submit" class="btn btn-success">Upload And Save</button>
                                            </form>
                                            <c:if test="${not empty errorMessage}">
                                                <div class="alert alert-danger" role="alert">
                                                    ${errorMessage}
                                                </div>
                                            </c:if>
                                            <c:if test="${not empty permissions}">
                                                <table class="table table-striped">
                                                    <thead>
                                                        <tr>
                                                            <th>ID</th>
                                                            <th>Name</th>
                                                            <th>API Path</th>
                                                            <th>Method</th>
                                                            <th>Module</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="permission" items="${permissions}">
                                                            <tr>
                                                                <td>
                                                                    ${permission.id}
                                                                </td>
                                                                <td>${permission.name}</td>
                                                                <td>${permission.apiPath}</td>
                                                                <td>${permission.method}</td>
                                                                <td>${permission.module}</td>
                                                                <td>
                                                                    <div style="display: flex; gap:6px">
                                                                        <a href="/admin/permission/update/${permission.id}"
                                                                            class="btn btn-sm btn-warning">Edit</a>
                                                                        <form
                                                                            action="/admin/permission/delete/${permission.id}"
                                                                            method="POST">
                                                                            <button type="submit"
                                                                                class="btn btn-sm btn-danger">Delete</button>
                                                                            <input type="hidden"
                                                                                name="${_csrf.parameterName}"
                                                                                value="${_csrf.token}" />
                                                                        </form>
                                                                    </div>

                                                                    <!-- <a href="/admin/role/delete/${role.id}"
                                                                    class="btn btn-sm btn-danger"
                                                                    onclick="return confirm('Are you sure?')">Delete</a> -->
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
                                                                    <c:url var="pageUrl" value="/admin/permission">
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
                                                                <a class="page-link"
                                                                    href="?page=${meta.page + 1}">Next</a>
                                                            </li>
                                                        </ul>
                                                    </nav>
                                                </div>
                                            </c:if>
                                            <c:if test="${empty permissions}">
                                                <div style="text-align: center;">
                                                    Not Found Permission!
                                                </div>
                                            </c:if>

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
                    const name = document.getElementById("searchName").value;
                    const urlParams = new URLSearchParams(window.location.search);

                    if (name && name.trim() !== "") {
                        urlParams.set('filter', "name ~~ '" + name.trim() + "'");
                    } else {
                        urlParams.delete('filter');
                    }

                    urlParams.set('page', '1');

                    window.location.href = window.location.pathname + "?" + urlParams.toString();
                }
                document.getElementById("searchName").addEventListener("keypress", function (event) {
                    if (event.key === "Enter") {
                        handleSearch();
                    }
                });

            </script>
        </body>

        </html>