<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Update Detail Page</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            </head>

            <body>
                <div class="container mt-5">
                    <div class="row justify-content-center">
                        <div class="col-md-8">
                            <div class="card">
                                <div class="card-header">
                                    <h3 class="text-center">User Detail</h3>
                                </div>
                                <div class="card-body">
                                    <div>${userData.fullName}</div>
                                    <div>${userData.phone}</div>
                                    <div>${userData.email}</div>
                                    <div>
                                        <c:choose>
                                            <c:when test="${userData.status == true}">
                                                <span class="badge bg-success">Active</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-danger">Non-Active</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>


                                </div>

                            </div>
                            <a href="/admin/user" class="btn btn-secondary me-md-2" style="margin-top: 10px;">Back</a>

                        </div>
                    </div>
                </div>

                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
            </body>

            </html>