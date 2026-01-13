<div class="d-flex flex-column flex-shrink-0 p-3 text-white bg-dark" style="width: 180px; min-height:100vh">
    <a href="/" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none">
        <svg class="bi me-2" width="40" height="32">
            <use xlink:href="#bootstrap"></use>
        </svg>
        <span class="fs-4">Admin Dashboard</span>
    </a>
    <hr>
    <ul class="nav nav-pills flex-column mb-auto">
        <li class="nav-item">
            <a href="/" class="nav-link text-white" aria-current="page">
                <svg class="bi me-2" width="16" height="16">
                    <use xlink:href="#home"></use>
                </svg>
                Home
            </a>
        </li>
        <li>
            <a href="/admin" class="nav-link text-white">
                <svg class="bi me-2" width="16" height="16">
                    <use xlink:href="#speedometer2"></use>
                </svg>
                Dashboard
            </a>
        </li>
        <li>
            <a href="/admin/order" class="nav-link text-white">
                <svg class="bi me-2" width="16" height="16">
                    <use xlink:href="#table"></use>
                </svg>
                Orders
            </a>
        </li>
        <li>
            <a href="/admin/product" class="nav-link text-white">
                <svg class="bi me-2" width="16" height="16">
                    <use xlink:href="#grid"></use>
                </svg>
                Products
            </a>
        </li>
        <li>
            <a href="/admin/user" class="nav-link text-white">
                <svg class="bi me-2" width="16" height="16">
                    <use xlink:href="#people-circle"></use>
                </svg>
                Users
            </a>
        </li>
        <li>
            <a href="/admin/role" class="nav-link text-white">
                <svg class="bi me-2" width="16" height="16">
                    <use xlink:href="#people-circle"></use>
                </svg>
                Roles
            </a>
        </li>

    </ul>
    <div class="sb-sidenav-footer">
        <div class="small">Logged in as:</div>
        ${fullName}
    </div>

</div>