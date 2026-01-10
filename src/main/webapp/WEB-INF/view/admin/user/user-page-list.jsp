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
            <div class="container mt-5">
                <div class="row">
                    <div class="col-12">
                        <div class="card">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <h3>User Management</h3>
                                <a href="/admin/user/create" class="btn btn-primary">Create New User</a>
                            </div>
                            <div class="card-body">
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
                                                <a class="page-link" href="?page=${meta.page - 1}">Previous</a>
                                            </li>
                                            <c:forEach begin="1" end="${meta.pages}" var="p">
                                                <li class="page-item ${meta.page == p ? 'active' : ''}">
                                                    <a class="page-link"
                                                        href="?page=${p}&size=${meta.pageSize}">${p}</a>
                                                </li>
                                            </c:forEach>

                                            <li class="page-item ${meta.page == meta.pages ? 'disabled' : ''}">
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

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>