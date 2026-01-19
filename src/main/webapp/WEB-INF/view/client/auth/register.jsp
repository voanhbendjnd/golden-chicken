<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

                <!DOCTYPE html>
                <html>

                <head>

                    <meta charset="utf-8" />
                    <title>Register</title>
                    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
                        rel="stylesheet">
                </head>

                <body class="bg-primary">

                    <div id="layoutAuthentication">
                        <div id="layoutAuthentication_content">
                            <main>
                                <div class="container">
                                    <div class="row justify-content-center">
                                        <div class="col-lg-7">
                                            <div class="card shadow-lg border-0 rounded-lg mt-5">
                                                <div class="card-header">
                                                    <h3 class="text-center font-weight-light my-4">Register</h3>
                                                </div>
                                                <div class="card-body">
                                                    <form:form action="/register" method="post"
                                                        onsubmit="return validPassword()" modelAttribute="registerUser">
                                                        <c:set var="errorPassword">
                                                            <form:errors cssClass="invalid-feedback"
                                                                path="confirmPassword" />
                                                        </c:set>
                                                        <c:set var="errorEmail">
                                                            <form:errors cssClass="invalid-feedback" path="email" />
                                                        </c:set>
                                                        <c:set var="errorFullName">
                                                            <form:errors cssClass="invalid-feedback" path="fullName" />
                                                        </c:set>
                                                        <div class="form-floating mb-3">
                                                            <form:input
                                                                class="form-control ${not empty errorEmail ? 'is-invalid' : ''}"
                                                                type="text" path="fullName"
                                                                placeholder="Enter your full name" />
                                                            <label for="inputFirstName">Full name</label>
                                                            ${errorFullName}
                                                        </div>
                                                        <div class="form-floating mb-3">
                                                            <form:input
                                                                class="form-control ${not empty errorEmail ? 'is-invalid' : ''}"
                                                                path="email" type="email"
                                                                placeholder="name@example.com" />
                                                            <label for="inputEmail">Email</label>
                                                            ${errorEmail}
                                                        </div>
                                                        <div class="form-floating mb-3">

                                                            <form:input class="form-control" path="password"
                                                                id="password" type="password"
                                                                placeholder="Create a password" />
                                                            <label for="inputPassword">Password</label>
                                                        </div>
                                                        <div class="form-floating mb-3">
                                                            <form:input id="confirmPassword"
                                                                class="form-control ${not empty errorPassword ? 'is-invalid' : ''}"
                                                                path="confirmPassword" type="password"
                                                                placeholder="Confirm password" />
                                                            <label for="inputPasswordConfirm">Confirm
                                                                Password</label>
                                                            ${errorPassword}
                                                            <form:errors path="confirmPassword" />
                                                        </div>
                                                        <div id="error-msg"></div>
                                                        <div class="mt-4 mb-0">
                                                            <div class="d-grid">
                                                                <button type="submit"
                                                                    class="btn btn-primary btn-block">Create
                                                                    Account</button>
                                                            </div>
                                                        </div>
                                                    </form:form>
                                                </div>
                                                <div class="card-footer text-center py-3">
                                                    <div class="small"><a href="/login">Have an account? Go to
                                                            login</a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </main>
                        </div>

                    </div>
                    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
                        crossorigin="anonymous"></script>
                    <script>
                        function validPassword() {
                            var password = document.getElementById("password").value;
                            var confirm = document.getElementById("confirmPassword").value;
                            var errorElement = document.getElementById("error-msg");

                            if (password !== confirm) {
                                errorElement.innerHTML = "Password And Confirm password Not The Same!";
                                errorElement.style.color = "red";
                                return false; // Ngăn không cho submit
                            }
                            return true; // Cho phép submit
                        }
                    </script>
                </body>

                </html>