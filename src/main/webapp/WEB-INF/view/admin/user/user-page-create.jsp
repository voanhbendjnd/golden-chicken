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

            </html>