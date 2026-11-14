
"use strict";

// =====================================================
// 1. BIẾN TOÀN CỤC
// =====================================================

const state = {
    token: sessionStorage.getItem("tm_token") || "",
    user: null,
    tours: [],
    customers: [],
    bookings: [],
    admins: [],
    page: "dashboard",
    search: "",
    reportTourId: null
};

const $ = selector => document.querySelector(selector);

// =====================================================
// 2. HÀM HỖ TRỢ
// =====================================================

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, c => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    }[c]));
}

function money(value) {
    return new Intl.NumberFormat("vi-VN").format(
        Number(value) || 0
    ) + " ₫";
}

function localISO(offsetDays = 0) {
    const date = new Date();

    date.setDate(date.getDate() + offsetDays);

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function formatDate(value) {
    if (!value) return "—";

    const date = String(value).split("-");

    if (date.length !== 3) return escapeHtml(value);

    return `${date[2]}/${date[1]}/${date[0]}`;
}

function isAdmin() {
    return state.user?.role === "ADMIN";
}

function isCustomer() {
    return state.user?.role === "CUSTOMER";
}

function loggedIn() {
    return Boolean(state.token && state.user);
}

let toastTimer = null;

function toast(message, error = false) {
    const element = $("#toast");

    clearTimeout(toastTimer);

    element.textContent = message;
    element.className = "toast" + (error ? " error" : "");
    element.hidden = false;

    toastTimer = setTimeout(() => {
        element.hidden = true;
    }, 4000);
}

// =====================================================
// 3. GỌI REST API SPRING BOOT
// =====================================================

async function api(path, method = "GET", body = undefined) {

    const headers = {
        Accept: "application/json"
    };

    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const options = {
        method,
        headers
    };

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(body);
    }

    let response;

    try {
        response = await fetch(path, options);
    } catch (error) {
        throw new Error(
            "Không kết nối được Spring Boot. Kiểm tra server 8081."
        );
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();

    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }

    if (!response.ok) {

        let message =
            data?.message ||
            data?.detail ||
            `Lỗi HTTP ${response.status}`;

        if (data?.errors) {
            message = Object.values(data.errors).join("; ");
        }

        if (response.status === 401) {
            message = "Sai thông tin đăng nhập hoặc phiên đã hết hạn.";
        }

        if (response.status === 403) {
            message = "Bạn không có quyền thực hiện chức năng này.";
        }

        throw new Error(message);
    }

    return data;
}

// =====================================================
// 4. KHỞI ĐỘNG WEBSITE
// =====================================================

document.addEventListener("DOMContentLoaded", async () => {

    setupEvents();

    if (state.token) {

        try {
            state.user = await api("/api/auth/me");
        } catch {
            clearLogin();
        }
    }

    renderUser();

    await refreshData();
});

// =====================================================
// 5. XỬ LÝ SỰ KIỆN
// =====================================================

function setupEvents() {

    document.addEventListener("click", async event => {

        // Chuyển trang
        const pageButton = event.target.closest("[data-page]");

        if (pageButton) {
            showPage(pageButton.dataset.page);
            return;
        }

        // Đổi tên ADMIN
        const adminButton = event.target.closest("[data-edit-admin]");

        if (adminButton) {
            openRenameAdmin(Number(adminButton.dataset.editAdmin));
            return;
        }

        // Các thao tác khác
        const button = event.target.closest("[data-action]");

        if (!button) return;

        const action = button.dataset.action;
        const id = Number(button.dataset.id);

        try {

            switch (action) {

                case "openAuth":
                    openAuth();
                    break;

                case "logout":
                    await logout();
                    break;

                case "editTour":
                    openTourForm(id);
                    break;

                case "deleteTour":
                    await deleteTour(id);
                    break;

                case "bookTour":
                    openBookingForm(id);
                    break;

                case "editCustomer":
                    openCustomerForm(id);
                    break;

                case "deleteCustomer":
                    await deleteCustomer(id);
                    break;

                case "cancelBooking":
                    await cancelBooking(id);
                    break;
            }

        } catch (error) {
            toast(error.message, true);
        }
    });

    // Đăng nhập / Đăng ký
    $("#tabLogin").addEventListener("click", () => {
        setAuthTab("login");
    });

    $("#tabRegister").addEventListener("click", () => {
        setAuthTab("register");
    });

    $("#closeAuth").addEventListener("click", closeAuth);
    $("#closeEdit").addEventListener("click", closeEdit);

    $("#loginForm").addEventListener("submit", login);
    $("#registerForm").addEventListener("submit", register);

    // Tour
    $("#addTourBtn").addEventListener("click", () => {
        openTourForm();
    });

    $("#tourSearch").addEventListener("input", event => {
        state.search = event.target.value.trim().toLowerCase();
        renderTours();
    });

    // Khách hàng
    $("#addCustomerBtn").addEventListener("click", () => {
        openCustomerForm();
    });

    // ADMIN
    $("#addAdminBtn").addEventListener("click", () => {
        openAdminForm();
    });

    // Báo cáo
    $("#reportTour").addEventListener("change", event => {
        state.reportTourId = Number(event.target.value) || null;
        loadReport();
    });

    // Đóng modal
    $("#authModal").addEventListener("click", event => {
        if (event.target === $("#authModal")) closeAuth();
    });

    $("#editModal").addEventListener("click", event => {
        if (event.target === $("#editModal")) closeEdit();
    });

    document.addEventListener("keydown", event => {
        if (event.key === "Escape") {
            closeAuth();
            closeEdit();
        }
    });
}

// =====================================================
// 6. CHUYỂN TRANG
// =====================================================

function showPage(page) {

    if (
        (page === "customers" || page === "reports")
        && !isAdmin()
    ) {
        toast("Chức năng này chỉ dành cho ADMIN.", true);
        return;
    }

    state.page = page;

    const titles = {
        dashboard: "Tổng quan",
        tours: "Quản lý Tour",
        bookings: "Đặt Tour",
        customers: "Khách hàng & ADMIN",
        reports: "Báo cáo doanh thu"
    };

    $("#pageTitle").textContent = titles[page] || "Tour Manager";

    document.querySelectorAll(".page").forEach(element => {
        element.classList.toggle(
            "active",
            element.id === page
        );
    });

    document.querySelectorAll(".menu-item").forEach(element => {
        element.classList.toggle(
            "active",
            element.dataset.page === page
        );
    });

    if (page === "customers" && isAdmin()) {
        loadAdmins();
    }

    if (page === "reports" && isAdmin()) {
        loadReport();
    }

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}

// =====================================================
// 7. ĐĂNG NHẬP / ĐĂNG KÝ
// =====================================================

function openAuth() {

    setAuthTab("login");

    $("#authModal").hidden = false;
}

function closeAuth() {
    $("#authModal").hidden = true;
}

function setAuthTab(tab) {

    const isLogin = tab === "login";

    $("#loginForm").hidden = !isLogin;
    $("#registerForm").hidden = isLogin;

    $("#tabLogin").classList.toggle("active", isLogin);
    $("#tabRegister").classList.toggle("active", !isLogin);
}

function clearLogin() {

    state.token = "";
    state.user = null;

    sessionStorage.removeItem("tm_token");
}

async function finishLogin(result) {

    state.token = result.accessToken;

    sessionStorage.setItem("tm_token", state.token);

    try {
        state.user = await api("/api/auth/me");
    } catch (error) {
        clearLogin();
        throw error;
    }

    closeAuth();
    renderUser();

    await refreshData();

    showPage("dashboard");
}

// Đăng nhập
async function login(event) {

    event.preventDefault();

    const form = event.currentTarget;

    const button = form.querySelector('[type="submit"]');
    button.disabled = true;

    try {

        const request = Object.fromEntries(
            new FormData(form).entries()
        );

        const result = await api(
            "/api/auth/login",
            "POST",
            request
        );

        await finishLogin(result);

        form.reset();

        toast("Đăng nhập thành công!");

    } catch (error) {
        toast(error.message, true);
    } finally {
        button.disabled = false;
    }
}

// Đăng ký
async function register(event) {

    event.preventDefault();

    const form = event.currentTarget;
    const button = form.querySelector('[type="submit"]');

    button.disabled = true;

    try {

        const request = Object.fromEntries(
            new FormData(form).entries()
        );

        const result = await api(
            "/api/auth/register",
            "POST",
            request
        );

        await finishLogin(result);

        form.reset();

        toast("Đăng ký thành công!");

    } catch (error) {
        toast(error.message, true);
    } finally {
        button.disabled = false;
    }
}

// Đăng xuất
async function logout() {

    clearLogin();

    state.bookings = [];
    state.customers = [];
    state.admins = [];

    renderUser();

    showPage("dashboard");

    await refreshData();

    toast("Đã đăng xuất.");
}
function renderUser() {
    document.querySelectorAll("[data-admin]").forEach(element => {
        element.hidden = !isAdmin();
    });
    if (!loggedIn()) {
        $("#userArea").innerHTML = `
            <button class="btn primary"
                    data-action="openAuth">
                Đăng nhập
            </button>
        `;
        return;
    }
    const name = state.user.fullName || "Người dùng";
    $("#userArea").innerHTML = `
        <div class="user-info">
            <div class="avatar">
                ${escapeHtml(name.charAt(0).toUpperCase())}
            </div>
            <div>
                <strong>${escapeHtml(name)}</strong>
                <small>
                    ${escapeHtml(state.user.role)}
                </small>
            </div>
        </div>
        <button class="btn gray small"
                data-action="logout">
            Đăng xuất
        </button>
    `;
}
async function refreshData() {
    try {
        const toursPromise = api("/api/tours");
        const bookingsPromise = isAdmin()
            ? api("/api/bookings")
            : isCustomer()
                ? api("/api/bookings/me")
                : Promise.resolve([]);
        const customersPromise = isAdmin()
            ? api("/api/customers")
            : Promise.resolve([]);
        const [tours, bookings, customers] = await Promise.all([
            toursPromise,
            bookingsPromise,
            customersPromise
        ]);
        state.tours = tours || [];
        state.bookings = bookings || [];
        state.customers = customers || [];
        renderDashboard();
        renderTours();
        renderBookings();
        renderCustomers();
        renderReportOptions();
    } catch (error) {
        toast(error.message, true);
    }
}
function upcomingTours() {
    return state.tours
        .filter(t => t.departureDate > localISO())
        .sort((a, b) => a.departureDate.localeCompare(b.departureDate));
}
function upcomingHTML(tours) {
    if (!tours.length) {
        return `
            <p class="empty">
                Chưa có Tour sắp khởi hành.
            </p>
        `;
    }
    return tours.map(tour => {
        const parts = tour.departureDate.split("-");
        return `
            <div class="upcoming-item">
                <div class="date-box">
                    <strong>${parts[2]}</strong>
                    <small>TH ${parts[1]}</small>
                </div>
                <div class="item-text">
                    <strong>
                        ${escapeHtml(tour.name)}
                    </strong>
                    <small>
                        ${escapeHtml(tour.destination)}
                        • ${tour.durationDays} ngày
                    </small>
                </div>
                <strong class="price">
                    ${money(tour.price)}
                </strong>
            </div>
        `;
    }).join("");
}
function renderDashboard() {
    $("#totalTours").textContent = state.tours.length;
    $("#totalCustomers").textContent = isAdmin() ? state.customers.length : "—";
    const confirmed = state.bookings.filter(booking => booking.status === "CONFIRMED");
    $("#totalBookings").textContent = loggedIn() ? confirmed.length : "—";
    if (isAdmin()) {
        const revenue = confirmed.reduce((sum, booking) => sum + Number(booking.totalAmount), 0)
        $("#totalRevenue").textContent = money(revenue);
    } else {
        $("#totalRevenue").textContent = "—";
    }
    $("#upcomingList").innerHTML = upcomingHTML(upcomingTours().slice(0, 5));
}
function renderTours() {
    const keyword = state.search;
    const tours = state.tours.filter(t => {
        const content = [
            t.name,
            t.destination,
            t.itinerary
        ].join(" ").toLowerCase();
        return content.includes(keyword);
    });
    $("#tourCount").textContent = `${tours.length} Tour`;
    if (!tours.length) {
        $("#tourGrid").innerHTML = `
            <p class="empty">
                Không tìm thấy Tour phù hợp.
            </p>
        `;
        return;
    }
    $("#tourGrid").innerHTML = tours.map(tour => {
        const booked = Number(tour.bookedSeats) || 0;
        const maxSeats = Number(tour.maxSeats) || 1;
        const available = Number(tour.availableSeats);
        const percent = Math.min(100, Math.max(0, booked / maxSeats * 100));
        const future = tour.departureDate > localISO();
        const soldOut = available <= 0;
        let actions = "";
        if (isAdmin()) {
            actions = `
                <button class="btn outline small"
                        data-action="editTour"
                        data-id="${tour.id}">
                    Sửa
                </button>
                <button class="btn danger small"
                        data-action="deleteTour"
                        data-id="${tour.id}">
                    Xóa
                </button>
            `;
        } else {
            actions = `
                <button class="btn primary small"
                        data-action="bookTour"
                        data-id="${tour.id}"
                        ${(!future || soldOut) ? "disabled" : ""}>
                    ${
                !future ? "Đã khởi hành"
                    : soldOut ? "Hết chỗ"
                        : "Đặt Tour"
            }
                </button>
            `;
        }
        return `
            <div class="tour-card">
                <div class="tour-image">🌴</div>
                <div class="tour-body">
                    <small class="destination">
                        ${escapeHtml(tour.destination)}
                    </small>
                    <h3>
                        ${escapeHtml(tour.name)}
                    </h3>
                    <p class="tour-description">
                        ${escapeHtml(tour.itinerary)}
                    </p>
                    <div class="tour-meta">
                        <span>
                            📅 ${formatDate(tour.departureDate)}
                        </span>
                        <span>
                            ⏱ ${tour.durationDays} ngày
                        </span>
                    </div>
                    <div class="capacity">
                        <span>Chỗ đã đặt</span>
                        <strong>${booked}/${maxSeats}</strong>
                    </div>
                    <div class="progress">
                        <div style="width:${percent}%"></div>
                    </div>
                    <div class="tour-footer">
                        <div class="price">
                            ${money(tour.price)}
                            <small>/ người</small>
                        </div>
                        <div class="actions">
                            ${actions}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join("");
}
function openEdit(title, content) {
    $("#editTitle").textContent = title;
    $("#editContent").innerHTML = content;
    $("#editModal").hidden = false;
}
function closeEdit() {
    $("#editModal").hidden = true;
    $("#editContent").innerHTML = "";
}
function openTourForm(id = null) {
    if (!isAdmin()) {
        toast("Chỉ ADMIN được quản lý Tour.", true);
        return;
    }
    const tour = id ? state.tours.find(t => t.id === id) : null;
    if (id && !tour) {
        toast("Không tìm thấy Tour.", true);
        return;
    }
    const editing = Boolean(tour);
    openEdit(editing ? "Cập nhật Tour" : "Thêm Tour mới", `
        <form id="tourForm" class="form">
            <div class="form-grid">
                <div class="field wide">
                    <label>Tên Tour</label>
                    <input name="name"
                           value="${escapeHtml(tour?.name || "")}"
                           required>
                </div>
                <div class="field">
                    <label>Điểm đến</label>
                    <input name="destination"
                           value="${escapeHtml(tour?.destination || "")}"
                           required>
                </div>
                <div class="field">
                    <label>Ngày khởi hành</label>
                    <input name="departureDate"
                           type="date"
                           min="${localISO(1)}"
                           value="${tour?.departureDate || localISO(1)}"
                           required>
                </div>
                <div class="field">
                    <label>Số ngày</label>
                    <input name="durationDays"
                           type="number"
                           min="1"
                           value="${tour?.durationDays || 3}"
                           required>
                </div>
                <div class="field">
                    <label>Số chỗ tối đa</label>
                    <input name="maxSeats"
                           type="number"
                           min="1"
                           value="${tour?.maxSeats || 20}"
                           required>
                </div>
                <div class="field wide">
                    <label>Giá Tour (VNĐ)</label>
                    <input name="price"
                           type="number"
                           min="1"
                           step="1"
                           value="${tour?.price || ""}"
                           required>

                </div>
                <div class="field wide">
                    <label>Lịch trình</label>
                    <textarea name="itinerary"
                              required>${escapeHtml(tour?.itinerary || "")}</textarea>
                </div>
            </div>
            <div class="form-buttons">
                <button type="button"
                        class="btn gray"
                        id="cancelTourForm">
                    Hủy
                </button>
                <button type="submit" class="btn primary">
                    Lưu Tour
               </button>
            </div>
        </form>
        `
    );
    $("#cancelTourForm").onclick = closeEdit;
    $("#tourForm").addEventListener("submit", async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const data = new FormData(form);
        const request = {
            name: data.get("name").trim(),
            destination: data.get("destination").trim(),
            itinerary: data.get("itinerary").trim(),
            departureDate: data.get("departureDate"),
            durationDays: Number(data.get("durationDays")),
            price: Number(data.get("price")),
            maxSeats: Number(data.get("maxSeats"))
        };
        const button = form.querySelector('[type="submit"]');
        button.disabled = true;
        try {
            if (editing) {
                await api(`/api/tours/${id}`, "PUT", request);
            } else {
                await api("/api/tours", "POST", request);
            }
            closeEdit();
            await refreshData();
            toast("Lưu Tour thành công!");
        } catch (error) {
            toast(error.message, true);
        } finally {
            button.disabled = false;
        }
    });
}
async function deleteTour(id) {
    if (!isAdmin()) return;
    if (!confirm("Bạn chắc chắn muốn xóa Tour này?")) {
        return;
    }
    await api(`/api/tours/${id}`, "DELETE");
    await refreshData();
    toast("Đã xóa Tour.");
}
function renderCustomers() {
    if (!isAdmin()) {
        $("#customerTable").innerHTML = "";
        return;
    }
    if (!state.customers.length) {
        $("#customerTable").innerHTML = `
            <tr>
                <td colspan="5" class="empty">
                    Chưa có khách hàng.
                </td>
            </tr>
        `;
        return;
    }
    $("#customerTable").innerHTML = state.customers.map(c => `
        <tr>
            <td>#${c.id}</td>
            <td>
                <strong>${escapeHtml(c.fullName)}</strong>
            </td>
            <td>${escapeHtml(c.email)}</td>
            <td>${escapeHtml(c.phone)}</td>
            <td>
                <div class="actions">
                    <button class="btn outline small"
                            data-action="editCustomer"
                            data-id="${c.id}">
                        Sửa
                    </button>
                    <button class="btn danger small"
                            data-action="deleteCustomer"
                            data-id="${c.id}">
                        Xóa
                    </button>
                </div>
            </td>
        </tr>
    `).join("");
}
function openCustomerForm(id = null) {
    if (!isAdmin()) return;
    const customer = id ? state.customers.find(c => c.id === id) : null;
    if (id && !customer) {
        toast("Không tìm thấy khách hàng.", true);
        return;
    }
    const editing = Boolean(customer);
    openEdit(editing ? "Sửa khách hàng" : "Thêm khách hàng", `
        <form id="customerForm" class="form">
            <div class="field">
                <label>Họ tên</label>
                <input name="fullName"
                       value="${escapeHtml(customer?.fullName || "")}"
                       required>
            </div>
            <div class="field">
                <label>Email</label>
                <input name="email"
                       type="email"
                       value="${escapeHtml(customer?.email || "")}"
                       required>
            </div>
            <div class="field">
                <label>Số điện thoại</label>
                <input name="phone"
                       pattern="0[35789][0-9]{8}"
                       value="${escapeHtml(customer?.phone || "")}"
                       required>
            </div>
            <div class="form-buttons">
                <button type="button"
                        class="btn gray"
                        id="cancelCustomerForm">
                    Hủy
                </button>
                <button type="submit"
                        class="btn primary">
                    Lưu khách hàng
                </button>
            </div>
        </form>
        `
    );
    $("#cancelCustomerForm").onclick = closeEdit;
    $("#customerForm").addEventListener("submit", async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const request = Object.fromEntries(new FormData(form).entries());
        const button = form.querySelector('[type="submit"]');
        button.disabled = true;
        try {
            if (editing) {
                await api(`/api/customers/${id}`, "PUT", request);
            } else {
                await api("/api/customers", "POST", request);
            }
            closeEdit();
            await refreshData();
            toast("Lưu khách hàng thành công!");
        } catch (error) {
            toast(error.message, true);
        } finally {
            button.disabled = false;
        }
    });
}
async function deleteCustomer(id) {
    if (!isAdmin()) return;
    if (!confirm("Bạn chắc chắn muốn xóa khách hàng?")) {
        return;
    }
    await api(`/api/customers/${id}`, "DELETE");
    await refreshData();
    toast("Xóa khách hàng thành công.");
}

function openBookingForm(tourId) {
    if (!loggedIn()) {
        openAuth();
        toast("Vui lòng đăng nhập để đặt Tour.");
        return;
    }
    if (!isCustomer()) {
        toast("Chỉ CUSTOMER được đặt Tour.", true);
        return;
    }
    const tour = state.tours.find(t => t.id === tourId);
    if (!tour) {
        toast("Tour không tồn tại.", true);
        return;
    }
    const available = Number(tour.availableSeats);
    if (tour.departureDate <= localISO() || available <= 0) {
        toast("Tour không còn nhận đặt chỗ.", true);
        return;
    }
    openEdit("Xác nhận Đặt Tour", `
        <div class="panel">
            <h3>${escapeHtml(tour.name)}</h3>
            <p>
                Khởi hành: ${formatDate(tour.departureDate)}
            </p>
            <p>
                Giá: <strong>${money(tour.price)}</strong>/người
            </p>
            <p>
                Còn: <strong>${available} chỗ</strong>
            </p>
        </div>
        <form id="bookingForm" class="form">
            <div class="field">
                <label>Số người đặt</label>
                <input name="guestCount"
                       type="number"
                       min="1"
                       max="${available}"
                       value="1"
                       required>
            </div>
            <div class="panel">
                Tổng tiền:
                <strong class="price" id="bookingTotal">
                    ${money(tour.price)}
                </strong>
            </div>
            <div class="form-buttons">
                <button type="button"
                        class="btn gray"
                        id="cancelBookingForm">
                    Hủy
                </button>
                <button type="submit"
                        class="btn primary">
                    Xác nhận đặt Tour
                </button>
            </div>
        </form>
    `);
    $("#cancelBookingForm").onclick = closeEdit;
    const form = $("#bookingForm");
    const input = form.elements.namedItem("guestCount");
    input.addEventListener("input", () => {
        const count = Number(input.value) || 0;
        $("#bookingTotal").textContent = money(count * Number(tour.price));
    });
    form.addEventListener("submit", async event => {
        event.preventDefault();
        const button = form.querySelector('[type="submit"]');
        button.disabled = true;
        try {
            await api("/api/bookings", "POST", {
                tourId,
                guestCount: Number(input.value)
            });
            closeEdit();
            await refreshData();
            showPage("bookings");
            toast("Đặt Tour thành công!");
        } catch (error) {
            toast(error.message, true);
        } finally {
            button.disabled = false;
        }
    });
}
function renderBookings() {
    if (!loggedIn()) {
        $("#bookingTable").innerHTML = `
            <tr>
                <td colspan="7" class="empty">
                    Đăng nhập để xem lượt đặt Tour.
                </td>
            </tr>
        `;
        return;
    }
    if (!state.bookings.length) {
        $("#bookingTable").innerHTML = `
            <tr>
                <td colspan="7" class="empty">
                    Chưa có lượt đặt Tour.
                </td>
            </tr>
        `;
        return;
    }
    const bookings = [...state.bookings].sort((a, b) => b.id - a.id);
    $("#bookingTable").innerHTML = bookings.map(b => {
        const confirmed = b.status === "CONFIRMED";
        const canCancel = confirmed && b.departureDate > localISO();
        return `
            <tr>
                <td>#BK${b.id}</td>
                <td>
                    ${escapeHtml(b.customerName)}
                </td>
                <td>
                    <strong>${escapeHtml(b.tourName)}</strong>
                </td>
                <td>${b.guestCount}</td>
                <td>
                    <strong>${money(b.totalAmount)}</strong>
                </td>
                <td>
                    <span class="badge ${
            confirmed ? "confirmed" : "cancelled"
        }">
                        ${
            confirmed ? "Đã xác nhận" : "Đã hủy"
        }
                    </span>
                </td>
                <td>
                    ${
            canCancel
                ? `
                                <button class="btn danger small"
                                        data-action="cancelBooking"
                                        data-id="${b.id}">
                                    Hủy đơn
                                </button>
                            `
                : "—"
        }
                </td>
            </tr>
        `;
    }).join("");
}
async function cancelBooking(id) {
    if (!confirm("Bạn muốn hủy lượt đặt Tour này?")) {
        return;
    }
    await api(`/api/bookings/${id}/cancel`, "PATCH");
    await refreshData();
    toast("Đã hủy Booking thành công.");
}
async function loadAdmins() {
    if (!isAdmin()) return;
    try {
        state.admins = await api("/api/admin/accounts");
        if (!state.admins.length) {
            $("#adminTable").innerHTML = `
                <tr>
                    <td colspan="5" class="empty">
                        Chưa có ADMIN nào.
                    </td>
                </tr>
            `;
            return;
        }
        $("#adminTable").innerHTML = state.admins.map(admin => `
            <tr>
                <td>#${admin.id}</td>
                <td>
                    <strong>
                        ${escapeHtml(admin.fullName || "Chưa đặt tên")}
                    </strong>
                </td>
                <td>${escapeHtml(admin.email)}</td>
                <td>
                    <span class="badge confirmed">
                        ADMIN
                    </span>
                </td>
                <td>
                    <button class="btn outline small"
                            data-edit-admin="${admin.id}">
                        Đổi tên
                    </button>
                </td>
            </tr>
        `).join("");
    } catch (error) {
        $("#adminTable").innerHTML = `
            <tr>
                <td colspan="5" class="empty">
                    Không tải được danh sách ADMIN.
                </td>
            </tr>
        `;
        toast(error.message, true);
    }
}

function openAdminForm() {
    if (!isAdmin()) return;
    openEdit("Thêm tài khoản ADMIN", `
        <form id="adminForm" class="form">
            <div class="field">
                <label>Họ và tên ADMIN</label>
                <input name="fullName"
                       maxlength="100"
                       placeholder="Nguyễn Văn An"
                       required>
            </div>
            <div class="field">
                <label>Email</label>
                <input type="email"
                       name="email"
                       placeholder="admin2@gmail.com"
                       required>
            </div>
            <div class="field">
                <label>Mật khẩu</label>
                <input type="password"
                       name="password"
                       minlength="8"
                       autocomplete="new-password"
                       required>

            </div>
            <div class="form-buttons">
                <button type="button"
                        class="btn gray"
                        id="cancelAdminForm">
                    Hủy
                </button>
                <button type="submit" class="btn primary">
                    Tạo ADMIN
                </button>
            </div>
        </form>
    `);
    $("#cancelAdminForm").onclick = closeEdit;
    $("#adminForm").addEventListener("submit", async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const request = Object.fromEntries(new FormData(form).entries());
        request.fullName = request.fullName.trim();
        request.email = request.email.trim();
        const button = form.querySelector('[type="submit"]');
        button.disabled = true;
        try {
            await api("/api/admin/accounts", "POST", request);
            closeEdit();
            await loadAdmins();
            toast("Tạo ADMIN thành công!");
        } catch (error) {
            toast(error.message, true);
        } finally {
            button.disabled = false;
        }
    });
}
function openRenameAdmin(id) {
    if (!isAdmin()) return;
    const admin = state.admins.find(a => a.id === id);
    if (!admin) {
        toast("Không tìm thấy ADMIN.", true);
        return;
    }
    openEdit("Đổi tên ADMIN", `
        <form id="renameAdminForm" class="form">
            <div class="field">
                <label>Họ tên ADMIN mới</label>
                <input name="fullName"
                       maxlength="100"
                       value="${escapeHtml(admin.fullName || "")}"
                       required>
            </div>
            <div class="form-buttons">
                <button type="button"
                        class="btn gray"
                        id="cancelRenameAdmin">
                    Hủy
                </button>
                <button type="submit" class="btn primary">
                    Lưu thay đổi
                </button>
            </div>
        </form>
    `);
    $("#cancelRenameAdmin").onclick = closeEdit;
    $("#renameAdminForm").addEventListener("submit", async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const fullName = new FormData(form).get("fullName").trim();
        const button = form.querySelector('[type="submit"]');
        button.disabled = true;
        try {
            await api(`/api/admin/accounts/${id}/name`, "PUT", { fullName });
            closeEdit();
            await loadAdmins();
            state.user = await api("/api/auth/me");
            renderUser();
            toast("Đổi tên ADMIN thành công!");
        } catch (error) {
            toast(error.message, true);
        } finally {
            button.disabled = false;
        }
    });
}

function renderReportOptions() {
    if (!isAdmin()) return;
    const select = $("#reportTour");
    const exists = state.tours.some(tour => tour.id === state.reportTourId);
    if (!exists) {
        state.reportTourId = state.tours.length ? state.tours[0].id : null;
    }
    select.innerHTML = `
        <option value="">-- Chọn Tour --</option>
        ${state.tours.map(tour => `
            <option value="${tour.id}">
                ${escapeHtml(tour.name)}
            </option>
        `).join("")}
    `;
    select.value = state.reportTourId || "";
    $("#reportUpcoming").innerHTML = upcomingHTML(upcomingTours());
    if (state.page === "reports") {
        loadReport();
    }
}
async function loadReport() {
    if (!isAdmin()) return;
    const id = state.reportTourId;
    if (!id) {
        $("#reportRevenue").textContent = "0 ₫";
        $("#reportBookings").textContent = "0";
        $("#reportSeats").textContent = "0";
        $("#reportAvailable").textContent = "0";
        $("#reportCustomers").innerHTML = `
            <tr>
                <td colspan="4" class="empty">
                    Chưa có Tour để thống kê.
                </td>
            </tr>
        `;
        return;
    }
    try {
        const [revenue, customers] = await Promise.all([
            api(`/api/reports/tours/${id}/revenue`),
            api(`/api/reports/tours/${id}/customers`)
        ]);
        if (state.reportTourId !== id) return;
        $("#reportRevenue").textContent = money(revenue.revenue);
        $("#reportBookings").textContent = revenue.totalBookings;
        $("#reportSeats").textContent = revenue.bookedSeats;
        $("#reportAvailable").textContent = revenue.availableSeats;
        if (!customers.length) {
            $("#reportCustomers").innerHTML = `
                <tr>
                    <td colspan="4" class="empty">
                        Chưa có khách hàng đặt Tour này.
                    </td>
                </tr>
            `;
            return;
        }
        $("#reportCustomers").innerHTML = customers.map(c => `
            <tr>
                <td>#${c.id}</td>
                <td>
                    <strong>${escapeHtml(c.fullName)}</strong>
                </td>
                <td>${escapeHtml(c.email)}</td>
                <td>${escapeHtml(c.phone)}</td>
            </tr>
        `).join("");
    } catch (error) {
        toast(error.message, true);
    }
}
