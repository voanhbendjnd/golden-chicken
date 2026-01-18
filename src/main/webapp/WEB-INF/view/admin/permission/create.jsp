<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Create Permission Page</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            </head>

            <body>
                <div class="container mt-5">
                    <div class="row justify-content-center">
                        <div class="col-md-8">
                            <div class="card">
                                <div class="card-header">
                                    <h3 class="text-center">Create New Permission</h3>
                                </div>
                                <div class="card-body">
                                    <form:form method="post" action="/admin/permission/create"
                                        modelAttribute="permission">
                                        <div class="mb-3">
                                            <label class="form-label">Name</label>
                                            <form:input type="text" class="form-control" path="name" required="true" />
                                            <form:errors path="name" style="color: red;" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">API Path</label>
                                            <form:input type="text" class="form-control" path="apiPath"
                                                required="true" />
                                            <form:errors path="apiPath" style="color: red;" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">Method</label>
                                            <form:input type="text" class="form-control" path="method"
                                                required="true" />
                                            <form:errors path="method" style="color: red;" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label">Module</label>
                                            <form:input type="text" class="form-control" path="module"
                                                required="true" />
                                            <form:errors path="module" style="color: red;" />

                                        </div>



                                        <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                                            <a href="/admin/permission" class="btn btn-secondary me-md-2">Cancel</a>
                                            <button type="submit" class="btn btn-primary">Create Permission</button>
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