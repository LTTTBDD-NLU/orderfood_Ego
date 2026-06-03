package com.ego.restaurant.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ego.restaurant.R;
import com.ego.restaurant.adapters.StaffAdapter;
import com.ego.restaurant.helpers.DatabaseHelper;
import com.ego.restaurant.models.Staff;
import com.ego.restaurant.utils.PermissionHelper;
import com.ego.restaurant.utils.SessionManager;

import java.util.ArrayList;

public class AdminStaffActivity extends AppCompatActivity {

    private EditText etStaffName, etStaffRole;
    private Button   btnAddStaff, btnRoleWaiter, btnRoleKitchen, btnRoleAdmin;
    private TextView tvBack;
    private ListView lvStaff;

    private StaffAdapter     staffAdapter;
    private ArrayList<Staff> staffList = new ArrayList<>();
    private String selectedRole = "WAITSTAFF";

    private String currentUserRole;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_staff);

        currentUserRole = new SessionManager(this).getRole();
        if (!PermissionHelper.hasPermission(currentUserRole, PermissionHelper.MANAGE_STAFF_ACCOUNT)) {
            Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        etStaffName    = findViewById(R.id.et_staff_name);
        etStaffRole    = findViewById(R.id.et_staff_role);
        btnAddStaff    = findViewById(R.id.btn_add_staff);
        btnRoleWaiter  = findViewById(R.id.btn_role_waiter);
        btnRoleKitchen = findViewById(R.id.btn_role_kitchen);
        btnRoleAdmin   = findViewById(R.id.btn_role_admin);
        tvBack         = findViewById(R.id.tv_back_dashboard);
        lvStaff        = findViewById(R.id.lv_staff);

        boolean canCreateAdmin = PermissionHelper.hasPermission(
                currentUserRole, PermissionHelper.MANAGE_ADMIN_ACCOUNT);
        if (!canCreateAdmin) {
            btnRoleAdmin.setVisibility(android.view.View.GONE);
        }

        selectRole("WAITSTAFF");
        btnRoleWaiter.setOnClickListener(v  -> selectRole("WAITSTAFF"));
        btnRoleKitchen.setOnClickListener(v -> selectRole("KITCHEN_STAFF"));
        btnRoleAdmin.setOnClickListener(v   -> {
            if (!canCreateAdmin) {
                Toast.makeText(this, "Chỉ SUPERADMIN mới được tạo tài khoản ADMIN",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            selectRole("ADMIN");
        });

        staffAdapter = new StaffAdapter(this, staffList);
        lvStaff.setAdapter(staffAdapter);
        refreshList();

        btnAddStaff.setOnClickListener(v -> addStaff());
        tvBack.setOnClickListener(v -> finish());

        lvStaff.setOnItemLongClickListener((parent, view, pos, id) -> {
            showLockDialog(staffList.get(pos));
            return true;
        });

        lvStaff.setOnItemClickListener((parent, view, pos, id) -> {
            Staff s = staffList.get(pos);
            if ("ADMIN".equals(s.getRole())
                    && !PermissionHelper.hasPermission(currentUserRole,
                            PermissionHelper.MANAGE_ADMIN_ACCOUNT)) {
                Toast.makeText(this, "Chỉ SUPERADMIN mới quản lý tài khoản ADMIN",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(s.getName())
                    .setMessage("Vai trò: " + getRoleLabel(s.getRole())
                            + "\nTrạng thái: " + (s.getStatus() == 1 ? "Hoạt động" : "Bị khóa")
                            + "\n\nMật khẩu mặc định: ego@1234"
                            + "\n(Nhân viên tự đổi sau khi đăng nhập)")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void addStaff() {
        String name = etStaffName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) { etStaffName.setError("Nhập tên nhân viên"); return; }

        if ("ADMIN".equals(selectedRole)
                && !PermissionHelper.hasPermission(currentUserRole,
                        PermissionHelper.MANAGE_ADMIN_ACCOUNT)) {
            Toast.makeText(this, "Không có quyền tạo tài khoản ADMIN", Toast.LENGTH_SHORT).show();
            selectRole("WAITSTAFF");
            return;
        }

        long id = DatabaseHelper.getInstance(this).insertStaff(name, selectedRole);
        if (id < 0) { Toast.makeText(this, "Lỗi thêm nhân viên", Toast.LENGTH_SHORT).show(); return; }

        new AlertDialog.Builder(this)
                .setTitle("✅ Đã tạo tài khoản")
                .setMessage("Nhân viên: " + name
                        + "\nVai trò: " + getRoleLabel(selectedRole)
                        + "\n\n🔑 Mật khẩu mặc định: ego@1234"
                        + "\n\n⚠ Giao thông tin này cho nhân viên!")
                .setPositiveButton("Đã lưu", null)
                .show();

        etStaffName.setText("");
        refreshList();
    }

    private void showLockDialog(Staff s) {
        if ("ADMIN".equals(s.getRole())
                && !PermissionHelper.hasPermission(currentUserRole,
                        PermissionHelper.MANAGE_ADMIN_ACCOUNT)) {
            Toast.makeText(this, "Chỉ SUPERADMIN mới quản lý tài khoản ADMIN",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        boolean active = s.getStatus() == 1;
        String  action = active ? "Khóa tài khoản" : "Mở khóa tài khoản";
        new AlertDialog.Builder(this)
                .setTitle(action)
                .setMessage("Bạn có muốn " + action.toLowerCase()
                        + " của \"" + s.getName() + "\"?")
                .setPositiveButton("Đồng ý", (d, w) -> {
                    DatabaseHelper.getInstance(this).updateStaffStatus(s.getId(), active ? 0 : 1);
                    refreshList();
                    Toast.makeText(this, action + " thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void selectRole(String role) {
        selectedRole = role;
        etStaffRole.setText(getRoleLabel(role));
        android.content.res.ColorStateList red   = android.content.res.ColorStateList.valueOf(0xFFE64A19);
        android.content.res.ColorStateList white = android.content.res.ColorStateList.valueOf(0xFFF5F5F5);
        btnRoleWaiter.setBackgroundTintList("WAITSTAFF".equals(role)      ? red : white);
        btnRoleKitchen.setBackgroundTintList("KITCHEN_STAFF".equals(role) ? red : white);
        btnRoleAdmin.setBackgroundTintList("ADMIN".equals(role)           ? red : white);
        btnRoleWaiter.setTextColor("WAITSTAFF".equals(role)      ? 0xFFFFFFFF : 0xFF212121);
        btnRoleKitchen.setTextColor("KITCHEN_STAFF".equals(role) ? 0xFFFFFFFF : 0xFF212121);
        btnRoleAdmin.setTextColor("ADMIN".equals(role)           ? 0xFFFFFFFF : 0xFF212121);
    }

    private void refreshList() {
        staffList.clear();
        staffList.addAll(DatabaseHelper.getInstance(this).getAllStaff());
        staffAdapter.notifyDataSetChanged();
    }

    private String getRoleLabel(String r) {
        if (r == null) return "";
        switch (r.toUpperCase()) {
            case "KITCHEN_STAFF": return "Nhân viên Bếp";
            case "ADMIN":         return "Quản lý sảnh";
            default:              return "Phục vụ";
        }
    }
}
