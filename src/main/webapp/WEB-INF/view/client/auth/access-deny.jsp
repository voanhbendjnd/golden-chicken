<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

            <title>404 Error - SB Admin</title>
        </head>

        <body>
            <div id="layoutError">
                <div id="layoutError_content">
                    <main>
                        <div class="container">
                            <div class="row justify-content-center">
                                <div class="col-lg-6">
                                    <div class="text-center mt-4">

                                        <img class="mb-4 img-error" src="/images/404page.jpg" style="width: 50vh;" />
                                        <p class="lead">This requested URL was not found on this server.</p>
                                        <button class="btn btn-primary">
                                            <a href="/login" style="text-decoration: none; color: yellow;">
                                                <i class="fas fa-arrow-left me-1"></i>
                                                Return to Dashboard
                                            </a>
                                        </button>

                                    </div>
                                </div>
                            </div>
                        </div>
                    </main>
                </div>

            </div>
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
                crossorigin="anonymous"></script>
            <script src="js/scripts.js"></script>
        </body>

        </html>