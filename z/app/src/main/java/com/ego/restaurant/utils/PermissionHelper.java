package com.ego.restaurant.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionHelper {

    public static final String VIEW_TABLE            = "VIEW_TABLE";
    public static final String ORDER_GUEST_FOOD      = "ORDER_GUEST_FOOD";
    public static final String ORDER_MEMBER_FOOD     = "ORDER_MEMBER_FOOD";
    public static final String ORDER_FOR_CUSTOMER    = "ORDER_FOR_CUSTOMER";
    public static final String VERIFY_GUEST_ORDER    = "VERIFY_GUEST_ORDER";
    public static final String COOKING_COMPLETE      = "COOKING_COMPLETE";
    public static final String KITCHEN_OUT_OF_STOCK  = "KITCHEN_OUT_OF_STOCK";
    public static final String DELIVERY_COMPLETE     = "DELIVERY_COMPLETE";
    public static final String PROCESS_PAYMENT       = "PROCESS_PAYMENT";
    public static final String REGISTER_OWN_SHIFT    = "REGISTER_OWN_SHIFT";
    public static final String MANAGE_STAFF_SCHEDULE = "MANAGE_STAFF_SCHEDULE";
    public static final String MANAGE_ADMIN_SCHEDULE = "MANAGE_ADMIN_SCHEDULE";
    public static final String MANAGE_STAFF_ACCOUNT  = "MANAGE_STAFF_ACCOUNT";
    public static final String MANAGE_ADMIN_ACCOUNT  = "MANAGE_ADMIN_ACCOUNT";
    public static final String MANAGE_MENU_CATALOG   = "MANAGE_MENU_CATALOG";
    public static final String VIEW_REVENUE_REPORT   = "VIEW_REVENUE_REPORT";

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        ROLE_PERMISSIONS.put("GUEST", new HashSet<>(Arrays.asList(
                VIEW_TABLE, ORDER_GUEST_FOOD
        )));
        ROLE_PERMISSIONS.put("MEMBER", new HashSet<>(Arrays.asList(
                VIEW_TABLE, ORDER_MEMBER_FOOD
        )));
        ROLE_PERMISSIONS.put("WAITSTAFF", new HashSet<>(Arrays.asList(
                VIEW_TABLE, ORDER_FOR_CUSTOMER, VERIFY_GUEST_ORDER,
                DELIVERY_COMPLETE, PROCESS_PAYMENT, REGISTER_OWN_SHIFT
        )));
        ROLE_PERMISSIONS.put("KITCHEN_STAFF", new HashSet<>(Arrays.asList(
                COOKING_COMPLETE, KITCHEN_OUT_OF_STOCK
        )));
        ROLE_PERMISSIONS.put("ADMIN", new HashSet<>(Arrays.asList(
                VIEW_TABLE, ORDER_FOR_CUSTOMER, VERIFY_GUEST_ORDER,
                PROCESS_PAYMENT, REGISTER_OWN_SHIFT,
                MANAGE_STAFF_SCHEDULE, MANAGE_STAFF_ACCOUNT
        )));
        ROLE_PERMISSIONS.put("SUPERADMIN", new HashSet<>(Arrays.asList(
                VIEW_TABLE, ORDER_FOR_CUSTOMER, VERIFY_GUEST_ORDER,
                PROCESS_PAYMENT, REGISTER_OWN_SHIFT,
                MANAGE_ADMIN_SCHEDULE, MANAGE_STAFF_SCHEDULE,
                MANAGE_ADMIN_ACCOUNT, MANAGE_STAFF_ACCOUNT,
                MANAGE_MENU_CATALOG, VIEW_REVENUE_REPORT
        )));
    }

    public static boolean hasPermission(String roleCode, String permissionCode) {
        if (roleCode == null || permissionCode == null) return false;
        Set<String> perms = ROLE_PERMISSIONS.get(roleCode.toUpperCase());
        return perms != null && perms.contains(permissionCode);
    }

    public static String getHomeActivityForRole(String roleCode) {
        if (roleCode == null) return "LoginActivity";
        switch (roleCode.toUpperCase()) {
            case "GUEST":
            case "MEMBER":        return "CustomerActivity";
            case "WAITSTAFF":     return "WaiterActivity";
            case "KITCHEN_STAFF": return "KitchenActivity";
            case "ADMIN":
            case "SUPERADMIN":    return "AdminDashboardActivity";
            default:              return "LoginActivity";
        }
    }
}
