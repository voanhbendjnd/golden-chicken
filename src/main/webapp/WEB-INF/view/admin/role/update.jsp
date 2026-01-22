<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <title>Update Role</title>
                <meta name="viewport" content="width=device-width, initial-scale=1">

                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            </head>

            <body class="bg-light">

                <div class="container mt-4">

                    <!-- Breadcrumb -->
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><a href="/admin/dashboard">Dashboard</a></li>
                            <li class="breadcrumb-item"><a href="/admin/role">Roles</a></li>
                            <li class="breadcrumb-item active">Update</li>
                        </ol>
                    </nav>

                    <div class="card shadow-sm">
                        <div class="card-header bg-warning text-dark">
                            <h5 class="mb-0">Update Role</h5>
                        </div>

                        <div class="card-body">
                            <form:form method="post" action="/admin/role/update" modelAttribute="updateRole">

                                <form:hidden path="id" />

                                <div class="row">
                                    <!-- Role Info -->
                                    <div class="col-md-6">
                                        <h6 class="mb-3 text-secondary">Role Information</h6>

                                        <div class="mb-3">
                                            <label class="form-label">Role Name</label>
                                            <form:input path="name" class="form-control" />
                                            <form:errors path="name" class="text-danger small" />
                                        </div>

                                        <div class="mb-3">
                                            <label class="form-label">Description</label>
                                            <form:textarea path="description" class="form-control" rows="3" />
                                            <form:errors path="description" class="text-danger small" />
                                        </div>
                                    </div>

                                    <!-- Permissions -->
                                    <div class="col-md-6">
                                        <h6 class="mb-3 text-secondary">Permissions</h6>

                                        <div class="border rounded p-3" style="max-height: 260px; overflow-y: auto;">
                                            <div class="row">
                                                <c:forEach var="permission" items="${permissions}">
                                                    <c:set var="checked" value="false" />

                                                    <c:forEach var="p" items="${updateRole.permissions}">
                                                        <c:if test="${p.id == permission.id}">
                                                            <c:set var="checked" value="true" />
                                                        </c:if>
                                                    </c:forEach>

                                                    <!-- Each permission takes 6 columns = 2 per row -->
                                                    <div class="col-md-6">
                                                        <div class="form-check">
                                                            <input class="form-check-input" type="checkbox"
                                                                name="permissionIds" value="${permission.id}"
                                                                id="perm_${permission.id}" <c:if
                                                                test="${checked}">checked</c:if>
                                                            >
                                                            <label class="form-check-label" for="perm_${permission.id}">
                                                                ${permission.name}
                                                            </label>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </div>

                                        <small class="text-muted">
                                            Update permissions assigned to this role
                                        </small>
                                    </div>


                                    <!-- Actions -->
                                    <div class="d-flex justify-content-end mt-4">
                                        <a href="/admin/role" class="btn btn-outline-secondary me-2">
                                            Cancel
                                        </a>
                                        <button type="submit" class="btn btn-warning">
                                            Update Role
                                        </button>
                                    </div>

                            </form:form>
                        </div>
                    </div>
                </div>

                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
            </body>

            </html>