<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <title>Create Role</title>
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
                            <li class="breadcrumb-item active">Create</li>
                        </ol>
                    </nav>

                    <div class="card shadow-sm">
                        <div class="card-header bg-primary text-white">
                            <h5 class="mb-0">Create New Role</h5>
                        </div>

                        <div class="card-body">
                            <form:form method="post" action="/admin/role/create" modelAttribute="newRole">

                                <div class="row">
                                    <!-- Role Info -->
                                    <div class="col-md-6">
                                        <h6 class="mb-3 text-secondary">Role Information</h6>

                                        <div class="mb-3">
                                            <label class="form-label">Role Name</label>
                                            <form:input path="name" class="form-control"
                                                placeholder="e.g. ADMIN, STAFF" />
                                            <form:errors path="name" class="text-danger small" />
                                        </div>

                                        <div class="mb-3">
                                            <label class="form-label">Description</label>
                                            <form:textarea path="description" class="form-control" rows="3"
                                                placeholder="Describe the role responsibilities" />
                                            <form:errors path="description" class="text-danger small" />
                                        </div>
                                    </div>

                                    <!-- Permissions -->
                                    <div class="col-md-6">
                                        <h6 class="mb-3 text-secondary">Permissions</h6>

                                        <div class="border rounded p-3" style="max-height: 260px; overflow-y: auto;">
                                            <c:forEach var="permission" items="${permissions}">
                                                <div class="form-check">
                                                    <input class="form-check-input" type="checkbox" name="permissionIds"
                                                        value="${permission.id}" id="perm_${permission.id}">
                                                    <label class="form-check-label" for="perm_${permission.id}">
                                                        ${permission.name}
                                                    </label>
                                                </div>
                                            </c:forEach>
                                        </div>

                                        <small class="text-muted">
                                            Select permissions assigned to this role
                                        </small>
                                    </div>
                                </div>

                                <!-- Actions -->
                                <div class="d-flex justify-content-end mt-4">
                                    <a href="/admin/role" class="btn btn-outline-secondary me-2">
                                        Cancel
                                    </a>
                                    <button type="submit" class="btn btn-primary">
                                        Create Role
                                    </button>
                                </div>

                            </form:form>
                        </div>
                    </div>
                </div>

                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
            </body>

            </html>