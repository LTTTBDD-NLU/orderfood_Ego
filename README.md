# EGO Restaurant - Android App

## Cấu trúc Project

```
EgoApp/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/ego/restaurant/
│   │   ├── activities/
│   │   │   ├── LoginActivity.java       ← Prompt 1: FirebaseAuth + SharedPreferences
│   │   │   ├── RegisterActivity.java    ← Prompt 1: Đăng ký MEMBER
│   │   │   ├── CustomerActivity.java    ← Home GUEST/MEMBER
│   │   │   ├── MenuActivity.java        ← Prompt 2: ListView + BaseAdapter
│   │   │   ├── CartActivity.java        ← Prompt 2: Bundle Serializable + Firebase push
│   │   │   ├── WaiterActivity.java      ← Prompt 3: GridLayout + ContextMenu + ValueEventListener
│   │   │   ├── KitchenActivity.java     ← Prompt 4: Landscape + ExecutorService
│   │   │   └── AdminStaffActivity.java  ← Prompt 5: SQLite + softDelete
│   │   ├── adapters/
│   │   │   ├── MenuAdapter.java         ← BaseAdapter + LayoutInflater + ViewHolder
│   │   │   ├── CartAdapter.java
│   │   │   ├── PendingOrderAdapter.java ← Đơn PENDING_CONFIRM
│   │   │   ├── KitchenAdapter.java      ← ExecutorService tải ảnh
│   │   │   ├── DeliveryAdapter.java     ← Đơn DELIVERING
│   │   │   └── StaffAdapter.java        ← SQLite staff list
│   │   ├── models/
│   │   │   ├── MenuItem.java            ← Serializable, giá kép guest/member
│   │   │   ├── Order.java               ← Map bảng Orders
│   │   │   ├── OrderDetail.java         ← Map bảng Order_Details, status flow
│   │   │   ├── Table.java               ← Map bảng Tables
│   │   │   ├── Staff.java               ← SQLite Staff
│   │   │   └── Category.java
│   │   ├── helpers/
│   │   │   ├── DatabaseHelper.java      ← SQLiteOpenHelper
│   │   │   ├── ImageLoadRunnable.java   ← Runnable tải ảnh background
│   │   │   └── FirebaseSeeder.java      ← Seed dữ liệu mẫu
│   │   └── utils/
│   │       ├── PermissionHelper.java    ← Config phân quyền (phân_quyền.txt)
│   │       └── SessionManager.java      ← SharedPreferences session
│   └── res/
│       ├── layout/                      ← Tất cả XML layout
│       └── values/                      ← strings, colors, styles, ids
├── firebase_seed_data.json              ← Dữ liệu mẫu import vào Firebase
├── firebase.rules                       ← Firebase security rules
└── app/google-services.json            ← CẦN THAY BẰNG FILE THẬT từ Firebase Console
```

---

## Luồng Status Đơn Hàng (Order_Details.status)

```
KHÁCH VÃN LAI:
  Đặt món → PENDING_CONFIRM
               ↓ (Nhân viên xác nhận đúng bàn - VERIFY_GUEST_ORDER)
             COOKING
               ↓ (Bếp làm xong - COOKING_COMPLETE)
           DELIVERING
               ↓ (Nhân viên mang lên - DELIVERY_COMPLETE)
           COMPLETED

KHÁCH MEMBER:
  Đặt món → COOKING (thẳng xuống bếp, không cần xác nhận)
               ↓
           DELIVERING → COMPLETED

HỦY: COOKING → CANCELLED (Bếp báo hết nguyên liệu - KITCHEN_OUT_OF_STOCK)
```

---

## Phân Quyền Theo Role

| Role          | Quyền chính                                              |
|---------------|----------------------------------------------------------|
| GUEST         | VIEW_TABLE, ORDER_GUEST_FOOD                             |
| MEMBER        | VIEW_TABLE, ORDER_MEMBER_FOOD (giá ưu đãi)              |
| WAITSTAFF     | VERIFY_GUEST_ORDER, ORDER_FOR_CUSTOMER, DELIVERY_COMPLETE, PROCESS_PAYMENT |
| KITCHEN_STAFF | COOKING_COMPLETE, KITCHEN_OUT_OF_STOCK                   |
| ADMIN         | MANAGE_STAFF_ACCOUNT, MANAGE_STAFF_SCHEDULE + WAITSTAFF  |
| SUPERADMIN    | MANAGE_ADMIN_ACCOUNT, MANAGE_MENU_CATALOG, VIEW_REVENUE_REPORT |

---

## Hướng Dẫn Cài Đặt

### Bước 1: Firebase Setup
1. Vào https://console.firebase.google.com
2. Tạo project **EgoRestaurant**
3. Thêm Android App với package: `com.ego.restaurant`
4. Tải `google-services.json` -> đặt vào `app/`
5. Bật **Authentication → Email/Password**
6. Bật **Realtime Database** (chọn region gần nhất)
7. Vào **Database → Import JSON** → chọn file `firebase_seed_data.json`
8. Vào **Database → Rules** → paste nội dung `firebase.rules`

### Bước 2: Tạo tài khoản nhân viên
Vào **Firebase Authentication → Add User** tạo các email:
- `kitchen@ego.com` / password123  → sau đó vào Database/Users thêm `role: KITCHEN_STAFF`
- `waiter@ego.com` / password123   → thêm `role: WAITSTAFF`
- `admin@ego.com` / password123    → thêm `role: ADMIN`

### Bước 3: Chạy app
```bash
# Android Studio → Open EgoApp → Run
# Hoặc command line:
./gradlew assembleDebug