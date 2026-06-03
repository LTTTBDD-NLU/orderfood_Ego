package com.ego.restaurant.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ego.restaurant.models.MenuItem;
import com.ego.restaurant.models.Order;
import com.ego.restaurant.models.OrderDetail;
import com.ego.restaurant.models.Staff;
import com.ego.restaurant.models.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    public  static final String DB_NAME    = "ego_restaurant.db";
    private static final int    DB_VERSION = 2;

    public static final String T_USERS   = "Users";
    public static final String U_ID      = "id";
    public static final String U_NAME    = "full_name";
    public static final String U_EMAIL   = "email";
    public static final String U_PHONE   = "phone";
    public static final String U_PWD     = "password_hash";
    public static final String U_ROLE    = "role";
    public static final String U_STATUS  = "status";

    public static final String T_TABLES  = "Tables";
    public static final String TB_ID     = "table_id";
    public static final String TB_NAME   = "table_name";
    public static final String TB_CAP    = "capacity";
    public static final String TB_STATUS = "status";
    public static final String TB_ORD    = "current_order_id";

    public static final String T_MENU    = "Menu_Items";
    public static final String M_ID      = "item_id";
    public static final String M_NAME    = "item_name";
    public static final String M_CAT     = "category";
    public static final String M_IMG     = "image_url";
    public static final String M_GPRICE  = "guest_price";
    public static final String M_MPRICE  = "member_price";
    public static final String M_STATUS  = "status";
    public static final String T_ORDERS  = "Orders";
    public static final String O_ID      = "order_id";
    public static final String O_TABLE   = "table_id";
    public static final String O_TNAME   = "table_name";
    public static final String O_USER    = "user_id";
    public static final String O_ROLE    = "role_code";
    public static final String O_STATUS  = "order_status";
    public static final String O_TOTAL   = "total_amount";
    public static final String O_CREATED = "created_at";
    public static final String O_PAID    = "paid_at";

    public static final String T_DETAILS  = "Order_Details";
    public static final String D_ID       = "detail_id";
    public static final String D_ORDER    = "order_id";
    public static final String D_ITEM     = "item_id";
    public static final String D_INAME    = "item_name";
    public static final String D_IMGURL   = "image_url";
    public static final String D_QTY      = "quantity";
    public static final String D_PRICE    = "unit_price";
    public static final String D_NOTE     = "note";
    public static final String D_TABLE    = "table_id";
    public static final String D_TNAME    = "table_name";
    public static final String D_STATUS   = "status";
    public static final String D_TIME     = "order_time";
    public static final String D_FINISH   = "finished_at";

    public static final String T_SCHED   = "Schedules";
    public static final String S_ID      = "id";
    public static final String S_UID     = "user_id";
    public static final String S_DATE    = "date_key";
    public static final String S_MORN    = "morning";
    public static final String S_AFTER   = "afternoon";
    public static final String S_EVE     = "evening";
    public static final String S_STATUS  = "status";
    public static final String T_PROMO   = "Promotions";
    public static final String P_ID      = "id";
    public static final String P_NAME    = "name";
    public static final String P_DISC    = "discount_percent";
    public static final String P_TARGET  = "target";
    public static final String P_ACTIVE  = "active";

    private static DatabaseHelper instance;
    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USERS + "(" +
                U_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                U_NAME   + " TEXT NOT NULL," +
                U_EMAIL  + " TEXT UNIQUE NOT NULL," +
                U_PHONE  + " TEXT DEFAULT ''," +
                U_PWD    + " TEXT NOT NULL," +
                U_ROLE   + " TEXT NOT NULL DEFAULT 'MEMBER'," +
                U_STATUS + " INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE " + T_TABLES + "(" +
                TB_ID     + " TEXT PRIMARY KEY," +
                TB_NAME   + " TEXT NOT NULL," +
                TB_CAP    + " INTEGER DEFAULT 4," +
                TB_STATUS + " TEXT DEFAULT 'EMPTY'," +
                TB_ORD    + " TEXT DEFAULT '')");

        db.execSQL("CREATE TABLE " + T_MENU + "(" +
                M_ID     + " TEXT PRIMARY KEY," +
                M_NAME   + " TEXT NOT NULL," +
                M_CAT    + " TEXT DEFAULT ''," +
                M_IMG    + " TEXT DEFAULT ''," +
                M_GPRICE + " REAL DEFAULT 0," +
                M_MPRICE + " REAL DEFAULT 0," +
                M_STATUS + " TEXT DEFAULT 'AVAILABLE')");

        db.execSQL("CREATE TABLE " + T_ORDERS + "(" +
                O_ID      + " TEXT PRIMARY KEY," +
                O_TABLE   + " TEXT NOT NULL," +
                O_TNAME   + " TEXT DEFAULT ''," +
                O_USER    + " TEXT DEFAULT ''," +
                O_ROLE    + " TEXT DEFAULT 'GUEST'," +
                O_STATUS  + " TEXT DEFAULT 'IN_PROGRESS'," +
                O_TOTAL   + " REAL DEFAULT 0," +
                O_CREATED + " INTEGER DEFAULT 0," +
                O_PAID    + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + T_DETAILS + "(" +
                D_ID     + " TEXT PRIMARY KEY," +
                D_ORDER  + " TEXT NOT NULL," +
                D_ITEM   + " TEXT DEFAULT ''," +
                D_INAME  + " TEXT NOT NULL," +
                D_IMGURL + " TEXT DEFAULT ''," +
                D_QTY    + " INTEGER DEFAULT 1," +
                D_PRICE  + " REAL DEFAULT 0," +
                D_NOTE   + " TEXT DEFAULT ''," +
                D_TABLE  + " TEXT DEFAULT ''," +
                D_TNAME  + " TEXT DEFAULT ''," +
                D_STATUS + " TEXT DEFAULT 'PENDING_CONFIRM'," +
                D_TIME   + " INTEGER DEFAULT 0," +
                D_FINISH + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + T_SCHED + "(" +
                S_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                S_UID    + " INTEGER NOT NULL," +
                S_DATE   + " TEXT NOT NULL," +
                S_MORN   + " INTEGER DEFAULT 0," +
                S_AFTER  + " INTEGER DEFAULT 0," +
                S_EVE    + " INTEGER DEFAULT 0," +
                S_STATUS + " TEXT DEFAULT 'REGISTERED'," +
                "UNIQUE(" + S_UID + "," + S_DATE + "))");

        db.execSQL("CREATE TABLE " + T_PROMO + "(" +
                P_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                P_NAME   + " TEXT NOT NULL," +
                P_DISC   + " REAL DEFAULT 0," +
                P_TARGET + " TEXT DEFAULT 'MEMBER'," +
                P_ACTIVE + " INTEGER DEFAULT 1)");

        seedInitialData(db);
        Log.d(TAG, "onCreate: tất cả bảng đã tạo + seed dữ liệu mẫu");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String[] tables = {T_PROMO, T_SCHED, T_DETAILS, T_ORDERS, T_MENU, T_TABLES, T_USERS};
        for (String t : tables) db.execSQL("DROP TABLE IF EXISTS " + t);
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Admin mặc định
        insertUser(db, "Quản lý EGO",   "admin@ego.vn",   "0900000001", "admin123",   "ADMIN",         1);
        insertUser(db, "Super Admin",    "super@ego.vn",   "0900000000", "super123",   "SUPERADMIN",    1);
        insertUser(db, "Nguyễn Phục Vụ","waiter@ego.vn",  "0911111111", "waiter123",  "WAITSTAFF",     1);
        insertUser(db, "Trần Đầu Bếp",  "kitchen@ego.vn", "0922222222", "kitchen123", "KITCHEN_STAFF", 1);
        insertUser(db, "Lê Thành Viên", "member@ego.vn",  "0933333333", "member123",  "MEMBER",        1);

        // 16 bàn
        for (int i = 1; i <= 16; i++) {
            String id = String.format("T%02d", i);
            ContentValues cv = new ContentValues();
            cv.put(TB_ID, id); cv.put(TB_NAME, "Bàn " + String.format("%02d", i));
            cv.put(TB_CAP, 4); cv.put(TB_STATUS, "EMPTY"); cv.put(TB_ORD, "");
            db.insert(T_TABLES, null, cv);
        }

        // 16 món ăn mẫu
        String[][] menu = {
            {"Phở bò đặc biệt",    "Món chính",   "85000",  "75000"},
            {"Bún bò Huế",         "Món chính",   "75000",  "65000"},
            {"Cơm tấm sườn bì",    "Món chính",   "65000",  "58000"},
            {"Mì Quảng gà",        "Món chính",   "60000",  "52000"},
            {"Hủ tiếu Nam Vang",   "Món chính",   "70000",  "62000"},
            {"Sườn nướng mật ong", "Món chính",   "95000",  "85000"},
            {"Lẩu hải sản 2 người","Lẩu",         "250000", "220000"},
            {"Gỏi cuốn tôm thịt",  "Khai vị",     "45000",  "38000"},
            {"Chả giò chiên giòn", "Khai vị",     "50000",  "42000"},
            {"Bánh xèo miền Nam",  "Khai vị",     "55000",  "48000"},
            {"Cà phê sữa đá",      "Đồ uống",     "35000",  "30000"},
            {"Nước cam tươi",      "Đồ uống",     "40000",  "34000"},
            {"Trà đào cam sả",     "Đồ uống",     "45000",  "38000"},
            {"Sinh tố bơ",         "Đồ uống",     "50000",  "42000"},
            {"Kem 3 vị",           "Tráng miệng", "35000",  "30000"},
            {"Bánh flan caramen",  "Tráng miệng", "30000",  "25000"},
        };
        for (String[] m : menu) {
            ContentValues cv = new ContentValues();
            cv.put(M_ID,     UUID.randomUUID().toString());
            cv.put(M_NAME,   m[0]); cv.put(M_CAT,    m[1]);
            cv.put(M_IMG,    "");
            cv.put(M_GPRICE, Double.parseDouble(m[2]));
            cv.put(M_MPRICE, Double.parseDouble(m[3]));
            cv.put(M_STATUS, "AVAILABLE");
            db.insert(T_MENU, null, cv);
        }

        // Khuyến mãi mặc định
        ContentValues p1 = new ContentValues();
        p1.put(P_NAME,"Ưu đãi thành viên"); p1.put(P_DISC,10.0);
        p1.put(P_TARGET,"MEMBER"); p1.put(P_ACTIVE,1);
        db.insert(T_PROMO, null, p1);
    }

    private void insertUser(SQLiteDatabase db, String name, String email,
                             String phone, String pwd, String role, int status) {
        ContentValues cv = new ContentValues();
        cv.put(U_NAME, name); cv.put(U_EMAIL, email); cv.put(U_PHONE, phone);
        cv.put(U_PWD,  pwd);  cv.put(U_ROLE,  role);  cv.put(U_STATUS, status);
        db.insert(T_USERS, null, cv);
    }

    public Staff login(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + T_USERS + " WHERE " + U_EMAIL + "=? AND " + U_PWD + "=?",
                new String[]{email, password});
        Staff s = null;
        if (c.moveToFirst()) {
            if (c.getInt(c.getColumnIndexOrThrow(U_STATUS)) == 0) {
                c.close(); db.close();
                return null; // locked
            }
            s = new Staff();
            s.setId(c.getInt(c.getColumnIndexOrThrow(U_ID)));
            s.setName(c.getString(c.getColumnIndexOrThrow(U_NAME)));
            s.setRole(c.getString(c.getColumnIndexOrThrow(U_ROLE)));
            s.setStatus(1);
        }
        c.close(); db.close();
        return s;
    }

    public long registerMember(String name, String email, String phone, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(U_NAME, name); cv.put(U_EMAIL, email); cv.put(U_PHONE, phone);
        cv.put(U_PWD,  password); cv.put(U_ROLE, "MEMBER"); cv.put(U_STATUS, 1);
        long id = -1;
        try { id = db.insertOrThrow(T_USERS, null, cv); }
        catch (android.database.sqlite.SQLiteConstraintException e) { id = -2; } // email trùng
        db.close();
        return id;
    }
    public boolean changePassword(int userId, String oldPwd, String newPwd) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + T_USERS +
                " WHERE " + U_ID + "=? AND " + U_PWD + "=?",
                new String[]{String.valueOf(userId), oldPwd});
        boolean ok = c.moveToFirst();
        c.close();
        if (!ok) { db.close(); return false; }
        ContentValues cv = new ContentValues();
        cv.put(U_PWD, newPwd);
        db.update(T_USERS, cv, U_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return true;
    }

    public ArrayList<Staff> getAllStaff() {
        ArrayList<Staff> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_USERS +
                " WHERE " + U_ROLE + " IN ('WAITSTAFF','KITCHEN_STAFF','ADMIN')" +
                " ORDER BY " + U_STATUS + " DESC," + U_NAME, null);
        while (c.moveToNext()) {
            Staff s = new Staff();
            s.setId(c.getInt(c.getColumnIndexOrThrow(U_ID)));
            s.setName(c.getString(c.getColumnIndexOrThrow(U_NAME)));
            s.setRole(c.getString(c.getColumnIndexOrThrow(U_ROLE)));
            s.setStatus(c.getInt(c.getColumnIndexOrThrow(U_STATUS)));
            list.add(s);
        }
        c.close(); db.close();
        return list;
    }

    public long insertStaff(String name, String role) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(U_NAME, name); cv.put(U_EMAIL, name.replaceAll("\\s","").toLowerCase()
                + System.currentTimeMillis() + "@ego.vn");
        cv.put(U_PHONE,""); cv.put(U_PWD,"ego@1234");
        cv.put(U_ROLE, role); cv.put(U_STATUS,1);
        long id = db.insert(T_USERS, null, cv);
        db.close(); return id;
    }

    public int updateStaffStatus(int staffId, int newStatus) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(U_STATUS, newStatus);
        int rows = db.update(T_USERS, cv, U_ID + "=?", new String[]{String.valueOf(staffId)});
        db.close(); return rows;
    }

    public ArrayList<Table> getAllTables() {
        ArrayList<Table> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_TABLES + " ORDER BY " + TB_ID, null);
        while (c.moveToNext()) {
            Table t = new Table();
            t.setTableId(c.getString(c.getColumnIndexOrThrow(TB_ID)));
            t.setTableName(c.getString(c.getColumnIndexOrThrow(TB_NAME)));
            t.setStatus(c.getString(c.getColumnIndexOrThrow(TB_STATUS)));
            list.add(t);
        }
        c.close(); db.close();
        return list;
    }

    public void updateTableStatus(String tableId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TB_STATUS, status);
        db.update(T_TABLES, cv, TB_ID + "=?", new String[]{tableId});
        db.close();
    }

    public void setTableCurrentOrder(String tableId, String orderId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TB_ORD, orderId);
        db.update(T_TABLES, cv, TB_ID + "=?", new String[]{tableId});
        db.close();
    }

    public ArrayList<MenuItem> getAvailableMenu() {
        ArrayList<MenuItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_MENU +
                " WHERE " + M_STATUS + "='AVAILABLE' ORDER BY " + M_CAT + "," + M_NAME, null);
        while (c.moveToNext()) {
            MenuItem m = new MenuItem();
            m.setItemId(c.getString(c.getColumnIndexOrThrow(M_ID)));
            m.setItemName(c.getString(c.getColumnIndexOrThrow(M_NAME)));
            m.setCategoryId(c.getString(c.getColumnIndexOrThrow(M_CAT)));
            m.setImageUrl(c.getString(c.getColumnIndexOrThrow(M_IMG)));
            m.setGuestPrice(c.getDouble(c.getColumnIndexOrThrow(M_GPRICE)));
            m.setMemberPrice(c.getDouble(c.getColumnIndexOrThrow(M_MPRICE)));
            m.setStatus(c.getString(c.getColumnIndexOrThrow(M_STATUS)));
            list.add(m);
        }
        c.close(); db.close();
        return list;
    }

    public ArrayList<MenuItem> getAllMenuAdmin() {
        ArrayList<MenuItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_MENU + " ORDER BY " + M_CAT + "," + M_NAME, null);
        while (c.moveToNext()) {
            MenuItem m = new MenuItem();
            m.setItemId(c.getString(c.getColumnIndexOrThrow(M_ID)));
            m.setItemName(c.getString(c.getColumnIndexOrThrow(M_NAME)));
            m.setCategoryId(c.getString(c.getColumnIndexOrThrow(M_CAT)));
            m.setImageUrl(c.getString(c.getColumnIndexOrThrow(M_IMG)));
            m.setGuestPrice(c.getDouble(c.getColumnIndexOrThrow(M_GPRICE)));
            m.setMemberPrice(c.getDouble(c.getColumnIndexOrThrow(M_MPRICE)));
            m.setStatus(c.getString(c.getColumnIndexOrThrow(M_STATUS)));
            list.add(m);
        }
        c.close(); db.close();
        return list;
    }

    public long insertMenuItem(String name, String cat, String img, double gp, double mp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(M_ID, UUID.randomUUID().toString()); cv.put(M_NAME,name);
        cv.put(M_CAT,cat); cv.put(M_IMG,img);
        cv.put(M_GPRICE,gp); cv.put(M_MPRICE,mp); cv.put(M_STATUS,"AVAILABLE");
        long r = db.insert(T_MENU, null, cv);
        db.close(); return r;
    }

    public void updateMenuItem(String itemId, String name, String cat,
                                String img, double gp, double mp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(M_NAME,name); cv.put(M_CAT,cat); cv.put(M_IMG,img);
        cv.put(M_GPRICE,gp); cv.put(M_MPRICE,mp);
        db.update(T_MENU, cv, M_ID + "=?", new String[]{itemId});
        db.close();
    }

    public void updateMenuItemStatus(String itemId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(M_STATUS, status);
        db.update(T_MENU, cv, M_ID + "=?", new String[]{itemId});
        db.close();
    }

    public String createOrder(String tableId, String tableName, String userId, String role) {
        String orderId = UUID.randomUUID().toString();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(O_ID,      orderId);   cv.put(O_TABLE,   tableId);
        cv.put(O_TNAME,   tableName); cv.put(O_USER,    userId);
        cv.put(O_ROLE,    role);      cv.put(O_STATUS,  "IN_PROGRESS");
        cv.put(O_TOTAL,   0.0);       cv.put(O_CREATED, System.currentTimeMillis());
        cv.put(O_PAID,    0L);
        db.insert(T_ORDERS, null, cv);
        // Đánh dấu bàn
        ContentValues tv = new ContentValues();
        tv.put(TB_STATUS, "MEMBER".equals(role) ? "OCCUPIED" : "OCCUPIED");
        tv.put(TB_ORD,    orderId);
        db.update(T_TABLES, tv, TB_ID + "=?", new String[]{tableId});
        db.close();
        return orderId;
    }

    public Order getActiveOrderByTable(String tableId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_ORDERS +
                " WHERE " + O_TABLE + "=? AND " + O_STATUS + " NOT IN ('PAID','CANCELLED')" +
                " ORDER BY " + O_CREATED + " DESC LIMIT 1",
                new String[]{tableId});
        Order o = null;
        if (c.moveToFirst()) {
            o = cursorToOrder(c);
            o.setItems(getOrderDetails(db, o.getOrderId()));
        }
        c.close(); db.close();
        return o;
    }

    public Order getOrderById(String orderId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_ORDERS +
                " WHERE " + O_ID + "=?", new String[]{orderId});
        Order o = null;
        if (c.moveToFirst()) {
            o = cursorToOrder(c);
            o.setItems(getOrderDetails(db, orderId));
        }
        c.close(); db.close();
        return o;
    }

    public void updateOrderStatus(String orderId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(O_STATUS, status);
        if ("PAID".equals(status)) cv.put(O_PAID, System.currentTimeMillis());
        db.update(T_ORDERS, cv, O_ID + "=?", new String[]{orderId});
        db.close();
    }

    public void recalcOrderTotal(String orderId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + D_QTY + "*" + D_PRICE + ") FROM " + T_DETAILS +
                " WHERE " + D_ORDER + "=? AND " + D_STATUS + " != 'CANCELLED'",
                new String[]{orderId});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        ContentValues cv = new ContentValues();
        cv.put(O_TOTAL, total);
        db.update(T_ORDERS, cv, O_ID + "=?", new String[]{orderId});
        db.close();
    }

    public ArrayList<Order> getPaidOrders(long fromTs, long toTs) {
        ArrayList<Order> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_ORDERS +
                " WHERE " + O_STATUS + "='PAID' AND " + O_PAID + " BETWEEN ? AND ?" +
                " ORDER BY " + O_PAID + " DESC",
                new String[]{String.valueOf(fromTs), String.valueOf(toTs)});
        while (c.moveToNext()) {
            Order o = cursorToOrder(c);
            o.setItems(getOrderDetails(db, o.getOrderId()));
            list.add(o);
        }
        c.close(); db.close();
        return list;
    }

    private Order cursorToOrder(Cursor c) {
        Order o = new Order();
        o.setOrderId(c.getString(c.getColumnIndexOrThrow(O_ID)));
        o.setTableId(c.getString(c.getColumnIndexOrThrow(O_TABLE)));
        o.setTableName(c.getString(c.getColumnIndexOrThrow(O_TNAME)));
        o.setUserId(c.getString(c.getColumnIndexOrThrow(O_USER)));
        o.setRoleCode(c.getString(c.getColumnIndexOrThrow(O_ROLE)));
        o.setOrderStatus(c.getString(c.getColumnIndexOrThrow(O_STATUS)));
        o.setTotalAmount(c.getDouble(c.getColumnIndexOrThrow(O_TOTAL)));
        o.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(O_CREATED)));
        o.setPaidAt(c.getLong(c.getColumnIndexOrThrow(O_PAID)));
        return o;
    }

    public String insertDetail(String orderId, String itemId, String itemName,
                                String imgUrl, int qty, double price,
                                String note, String tableId, String tableName, String status) {
        String did = UUID.randomUUID().toString();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(D_ID,    did);     cv.put(D_ORDER,  orderId);
        cv.put(D_ITEM,  itemId);  cv.put(D_INAME,  itemName);
        cv.put(D_IMGURL,imgUrl);  cv.put(D_QTY,    qty);
        cv.put(D_PRICE, price);   cv.put(D_NOTE,   note);
        cv.put(D_TABLE, tableId); cv.put(D_TNAME,  tableName);
        cv.put(D_STATUS,status);  cv.put(D_TIME,   System.currentTimeMillis());
        cv.put(D_FINISH,0L);
        db.insert(T_DETAILS, null, cv);
        db.close();
        return did;
    }

    public void updateDetailStatus(String detailId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(D_STATUS, status);
        if ("DELIVERING".equals(status) || "COMPLETED".equals(status) || "CANCELLED".equals(status))
            cv.put(D_FINISH, System.currentTimeMillis());
        db.update(T_DETAILS, cv, D_ID + "=?", new String[]{detailId});
        db.close();
    }

    public void confirmOrderTable(String orderId, String tableId, String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(D_TABLE,  tableId);
        cv.put(D_TNAME,  tableName);
        cv.put(D_STATUS, "COOKING");
        db.update(T_DETAILS, cv, D_ORDER + "=? AND " + D_STATUS + "='PENDING_CONFIRM'",
                new String[]{orderId});
        // Cập nhật order-level tableName
        ContentValues ocv = new ContentValues();
        ocv.put(O_TABLE, tableId); ocv.put(O_TNAME, tableName);
        db.update(T_ORDERS, ocv, O_ID + "=?", new String[]{orderId});
        // Bàn → OCCUPIED
        ContentValues tcv = new ContentValues();
        tcv.put(TB_STATUS, "OCCUPIED"); tcv.put(TB_ORD, orderId);
        db.update(T_TABLES, tcv, TB_ID + "=?", new String[]{tableId});
        db.close();
    }
    public ArrayList<OrderDetail> getDetailsByStatus(String status) {
        ArrayList<OrderDetail> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT d.*, o." + O_TNAME + " AS _tname FROM " +
                T_DETAILS + " d LEFT JOIN " + T_ORDERS + " o ON d." + D_ORDER + "=o." + O_ID +
                " WHERE d." + D_STATUS + "=? ORDER BY d." + D_TIME,
                new String[]{status});
        while (c.moveToNext()) list.add(cursorToDetail(c));
        c.close(); db.close();
        return list;
    }

    private ArrayList<OrderDetail> getOrderDetails(SQLiteDatabase db, String orderId) {
        ArrayList<OrderDetail> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM " + T_DETAILS +
                " WHERE " + D_ORDER + "=? ORDER BY " + D_TIME,
                new String[]{orderId});
        while (c.moveToNext()) list.add(cursorToDetail(c));
        c.close();
        return list;
    }

    private OrderDetail cursorToDetail(Cursor c) {
        OrderDetail d = new OrderDetail();
        d.setDetailId(c.getString(c.getColumnIndexOrThrow(D_ID)));
        d.setOrderId(c.getString(c.getColumnIndexOrThrow(D_ORDER)));
        d.setItemId(c.getString(c.getColumnIndexOrThrow(D_ITEM)));
        d.setItemName(c.getString(c.getColumnIndexOrThrow(D_INAME)));
        d.setImageUrl(c.getString(c.getColumnIndexOrThrow(D_IMGURL)));
        d.setQuantity(c.getInt(c.getColumnIndexOrThrow(D_QTY)));
        d.setUnitPrice(c.getDouble(c.getColumnIndexOrThrow(D_PRICE)));
        d.setNote(c.getString(c.getColumnIndexOrThrow(D_NOTE)));
        d.setTableId(c.getString(c.getColumnIndexOrThrow(D_TABLE)));
        // tableName có thể từ join hoặc column D_TNAME
        int tnIdx = c.getColumnIndex("_tname");
        if (tnIdx >= 0 && !c.isNull(tnIdx))
            d.setTableName(c.getString(tnIdx));
        else
            d.setTableName(c.getString(c.getColumnIndexOrThrow(D_TNAME)));
        d.setStatus(c.getString(c.getColumnIndexOrThrow(D_STATUS)));
        d.setOrderTime(c.getLong(c.getColumnIndexOrThrow(D_TIME)));
        return d;
    }

    public void saveSchedule(int userId, String dateKey, boolean morning, boolean afternoon, boolean evening) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(S_UID,   userId);  cv.put(S_DATE,  dateKey);
        cv.put(S_MORN,  morning  ? 1 : 0);
        cv.put(S_AFTER, afternoon? 1 : 0);
        cv.put(S_EVE,   evening  ? 1 : 0);
        cv.put(S_STATUS,"REGISTERED");
        db.insertWithOnConflict(T_SCHED, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void markAttendance(int userId, String dateKey) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(S_STATUS, "ATTENDED");
        db.update(T_SCHED, cv, S_UID + "=? AND " + S_DATE + "=?",
                new String[]{String.valueOf(userId), dateKey});
        db.close();
    }

    public Cursor getScheduleForUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + T_SCHED + " WHERE " + S_UID + "=?" +
                " ORDER BY " + S_DATE, new String[]{String.valueOf(userId)});
    }

    public int countAttendedDays(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + T_SCHED +
                " WHERE " + S_UID + "=? AND " + S_STATUS + "='ATTENDED'",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close(); db.close();
        return count;
    }

    public Cursor getPromotions() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + T_PROMO + " ORDER BY " + P_ACTIVE + " DESC", null);
    }

    public void insertPromotion(String name, double disc, String target) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(P_NAME, name); cv.put(P_DISC, disc);
        cv.put(P_TARGET, target); cv.put(P_ACTIVE, 1);
        db.insert(T_PROMO, null, cv);
        db.close();
    }

    public void togglePromotion(int promoId, boolean active) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(P_ACTIVE, active ? 1 : 0);
        db.update(T_PROMO, cv, P_ID + "=?", new String[]{String.valueOf(promoId)});
        db.close();
    }
}
