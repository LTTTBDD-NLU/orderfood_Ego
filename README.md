# EGO - Android App

## Giới thiệu
EGO Restaurant là ứng dụng quản lý đặt món và vận hành nhà hàng dành cho nền tảng Android. Phiên bản này đã được tối ưu hóa để chạy Offline hoàn toàn sử dụng cơ sở dữ liệu SQLite local, đảm bảo tốc độ phản hồi nhanh và không phụ thuộc vào kết nối mạng.

## Cấu trúc Project
```text
EgoApp/
├── app/src/main/
│   ├── AndroidManifest.xml ← Cấu hình quyền & định nghĩa Activity
│   ├── java/com/ego/restaurant/
│   │   ├── activities/
│   │   │   ├── LoginActivity.java ← Đăng nhập dựa trên dữ liệu Staff/Member trong SQLite
│   │   │   ├── RegisterActivity.java ← Đăng ký tài khoản MEMBER mới vào SQLite
│   │   │   ├── CustomerActivity.java ← Màn hình chính cho GUEST/MEMBER
│   │   │   ├── MenuActivity.java ← Hiển thị danh sách món ăn từ SQLite
│   │   │   ├── CartActivity.java ← Quản lý giỏ hàng & tạo đơn hàng vào SQLite
│   │   │   ├── WaiterActivity.java ← Nghiệp vụ cho nhân viên phục vụ (xác nhận đơn, thanh toán)
│   │   │   ├── KitchenActivity.java ← Màn hình bếp xử lý chế biến đơn hàng
│   │   │   └── AdminStaffActivity.java ← Quản lý nhân sự với tính năng xóa mềm (softDelete)
│   │   ├── adapters/
│   │   │   ├── MenuAdapter.java ← Hiển thị món ăn (giá kép cho Guest/Member)
│   │   │   ├── CartAdapter.java ← Hiển thị vật phẩm trong giỏ hàng
│   │   │   ├── PendingOrderAdapter.java ← Xử lý đơn hàng chờ xác nhận (PENDING_CONFIRM)
│   │   │   ├── KitchenAdapter.java ← Quản lý danh sách món đang chế biến
│   │   │   └── StaffAdapter.java ← Hiển thị danh sách nhân viên từ SQLite
│   │   ├── models/
│   │   │   ├── MenuItem.java ← Đối tượng món ăn (giá Guest/Member)
│   │   │   ├── Order.java ← Thông tin đơn hàng tổng quát
│   │   │   ├── OrderDetail.java ← Chi tiết từng món & luồng trạng thái đơn hàng
│   │   │   ├── Table.java ← Quản lý thông tin bàn ăn
│   │   │   └── Staff.java ← Thông tin nhân viên (hỗ trợ phân quyền)
│   │   ├── helpers/
│   │   │   ├── DatabaseHelper.java ← Lớp kế thừa SQLiteOpenHelper quản lý DB local
│   │   │   └── ImageLoadRunnable.java ← Tải ảnh món ăn từ bộ nhớ local/assets
│   │   └── utils/
│   │       ├── PermissionHelper.java ← Kiểm tra quyền hạn dựa trên file cấu hình
│   │       └── SessionManager.java ← Lưu trữ phiên đăng nhập qua SharedPreferences
│   └── res/
│       ├── layout/ ← Giao diện XML cho các màn hình
│       └── values/ ← Định nghĩa màu sắc, chuỗi ký tự, và style ứng dụng
```

## Luồng Trạng Thái Đơn Hàng (Order_Details.status)
Hệ thống quản lý trạng thái món ăn đồng bộ trực tiếp trong SQLite:

### KHÁCH VÃN LAI (GUEST):
* Đặt món → `PENDING_CONFIRM`
* Nhân viên xác nhận bàn (`VERIFY_GUEST_ORDER`) → `COOKING`
* Bếp hoàn thành (`COOKING_COMPLETE`) → `DELIVERING`
* Phục vụ giao xong (`DELIVERY_COMPLETE`) → `COMPLETED`

### THÀNH VIÊN (MEMBER):
* Đặt món → `COOKING` *(Tự động xác nhận, không qua bước Pending)*
* Xử lý tiếp theo tương tự luồng Guest → `DELIVERING` → `COMPLETED`

### TRƯỜNG HỢP HỦY:
* Bếp báo hết món (`KITCHEN_OUT_OF_STOCK`) → `CANCELLED`

## Phân Quyền Vai Trò (Roles)
Dữ liệu quyền hạn được quản lý nội bộ thông qua `PermissionHelper`:

| Vai trò | Quyền hạn chính |
| :--- | :--- |
| **GUEST** | Xem bàn, đặt món giá thường |
| **MEMBER** | Xem bàn, đặt món giá ưu đãi |
| **WAITSTAFF** | Xác nhận đơn Guest, phục vụ món, xử lý thanh toán |
| **KITCHEN_STAFF** | Cập nhật trạng thái chế biến, báo hết món |
| **ADMIN** | Quản lý tài khoản nhân viên, lịch làm việc |
| **SUPERADMIN** | Quản lý danh mục thực đơn, xem báo cáo doanh thu |

## Ghi chú Kỹ thuật
* **Database:** SQLite
* **Min SDK:** 26 (Android 8.0).
* **Target SDK:** 34 (Android 14).
* **Offline First:** Mọi thao tác lưu trữ đơn hàng, thông tin nhân sự và thực đơn đều thực hiện trên thiết bị thông qua `DatabaseHelper`.

---

## Cấu trúc Bảng CSDL (DatabaseHelper.java)
Dưới đây là danh sách chi tiết các bảng cần thiết trong SQLite để lưu trữ và vận hành toàn bộ luồng nghiệp vụ trên:

### 1. Bảng `Users`
Quản lý tài khoản đăng nhập của cả Thành viên (Member) và Nhân viên các bộ phận.
* `id` (INTEGER PRIMARY KEY AUTOINCREMENT): Mã định danh duy nhất.
* `username` (TEXT UNIQUE): Tên tài khoản không trùng lặp.
* `password` (TEXT): Mật khẩu đã mã hóa.
* `role` (TEXT): Vai trò hệ thống (`MEMBER`, `WAITSTAFF`, `KITCHEN_STAFF`, `ADMIN`, `SUPERADMIN`).
* `is_deleted` (INTEGER DEFAULT 0): Cờ đánh dấu xóa mềm phục vụ quản lý nhân sự (0: đang hoạt động, 1: đã xóa).

### 2. Bảng `Tables`
Quản lý thông tin và trạng thái của các bàn ăn tại nhà hàng.
* `id` (INTEGER PRIMARY KEY AUTOINCREMENT): Số thứ tự bàn.
* `table_name` (TEXT): Tên hoặc số bàn (Ví dụ: "Bàn số 5").
* `status` (TEXT DEFAULT 'EMPTY'): Trạng thái bàn hiện tại (`EMPTY`: Bàn trống, `OCCUPIED`: Bàn có khách).

### 3. Bảng `MenuItems`
Danh mục thực đơn món ăn của nhà hàng với cơ chế giá kép.
* `id` (INTEGER PRIMARY KEY AUTOINCREMENT): Mã món ăn.
* `name` (TEXT): Tên món ăn.
* `price_guest` (REAL): Giá bán áp dụng cho Khách vãng lai.
* `price_member` (REAL): Giá ưu đãi áp dụng cho Thành viên.
* `image_path` (TEXT): Đường dẫn tệp ảnh lưu trong thư mục `assets` hoặc bộ nhớ local.
* `is_available` (INTEGER DEFAULT 1): Trạng thái món (1: Còn món, 0: Bếp báo hết món).

### 4. Bảng `Orders`
Thông tin tổng quát của một đơn đặt món tại bàn.
* `id` (INTEGER PRIMARY KEY AUTOINCREMENT): Mã hóa đơn / đơn hàng.
* `table_id` (INTEGER, FOREIGN KEY): Liên kết với bảng `Tables`.
* `customer_id` (INTEGER, FOREIGN KEY): Liên kết với bảng `Users` (Có thể nhận giá trị `NULL` nếu là `GUEST`).
* `total_price` (REAL DEFAULT 0): Tổng giá trị của toàn bộ đơn hàng.
* `created_at` (DATETIME DEFAULT CURRENT_TIMESTAMP): Thời gian khởi tạo đơn.
* `status` (TEXT): Trạng thái tổng quan của đơn hàng.

### 5. Bảng `OrderDetails`
Chi tiết từng món ăn trong đơn hàng và quản lý luồng trạng thái chế biến đồng bộ.
* `id` (INTEGER PRIMARY KEY AUTOINCREMENT): Mã chi tiết hóa đơn.
* `order_id` (INTEGER, FOREIGN KEY): Liên kết với bảng `Orders`.
* `menu_item_id` (INTEGER, FOREIGN KEY): Liên kết với bảng `MenuItems`.
* `quantity` (INTEGER): Số lượng đĩa/phần đặt mua.
* `status` (TEXT): Trạng thái xử lý của món ăn (`PENDING_CONFIRM`, `COOKING`, `DELIVERING`, `COMPLETED`, `CANCELLED`).