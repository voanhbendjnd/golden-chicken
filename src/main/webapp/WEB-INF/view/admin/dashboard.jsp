<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html>

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Role List</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        </head>

        <body>
            <jsp:include page="./layout/header.jsp" />
            <div class="container-fluid">
                <div class="row">
                    <div class="col-md-3 col-lg-2 p-0">
                        <jsp:include page="./layout/sidebar.jsp" />
                    </div>
                    <main class="col-md-9 col-lg-10 ms-sm-auto px-md-4">
                        <div>
                            DASHBOARD PAGE
                        </div>
                    </main>
                </div>

            </div>


            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
            <script>

            </script>
        </body>

        </html>