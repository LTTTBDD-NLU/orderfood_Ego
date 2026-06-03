package com.ego.restaurant.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME        = "EgoRestaurantSession";
    private static final String KEY_UID          = "uid";
    private static final String KEY_ROLE         = "role";
    private static final String KEY_NAME         = "full_name";
    private static final String KEY_EMAIL        = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static final String KEY_ACTIVE_ORDER_ID    = "active_order_id";
    private static final String KEY_ACTIVE_TABLE_ID    = "active_table_id";
    private static final String KEY_ACTIVE_TABLE_NAME  = "active_table_name";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(int userId, String role, String fullName, String email) {
        editor.putInt(KEY_UID, userId);
        editor.putString(KEY_ROLE,         role);
        editor.putString(KEY_NAME,         fullName);
        editor.putString(KEY_EMAIL,        email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveGuestSession() {
        editor.putInt(KEY_UID, 0);
        editor.putString(KEY_ROLE, "GUEST");
        editor.putString(KEY_NAME, "");
        editor.putString(KEY_EMAIL, "");
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId()    { return prefs.getInt(KEY_UID, 0); }
    public String getUid()    { return String.valueOf(prefs.getInt(KEY_UID, 0)); }
    public String getRole()   { return prefs.getString(KEY_ROLE,  "GUEST"); }
    public String getFullName()  { return prefs.getString(KEY_NAME,  ""); }
    public String getEmail()  { return prefs.getString(KEY_EMAIL, ""); }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public void saveActiveOrder(String orderId, String tableId, String tableName) {
        editor.putString(KEY_ACTIVE_ORDER_ID,   orderId   != null ? orderId   : "");
        editor.putString(KEY_ACTIVE_TABLE_ID,   tableId   != null ? tableId   : "");
        editor.putString(KEY_ACTIVE_TABLE_NAME, tableName != null ? tableName : "");
        editor.apply();
    }

    public void clearActiveOrder() {
        editor.remove(KEY_ACTIVE_ORDER_ID);
        editor.remove(KEY_ACTIVE_TABLE_ID);
        editor.remove(KEY_ACTIVE_TABLE_NAME);
        editor.apply();
    }

    public String getActiveOrderId() {
        return prefs.getString(KEY_ACTIVE_ORDER_ID, null);
    }

    public String getActiveTableId() {
        return prefs.getString(KEY_ACTIVE_TABLE_ID, null);
    }

    public String getActiveTableName() {
        return prefs.getString(KEY_ACTIVE_TABLE_NAME, null);
    }

    public boolean hasActiveOrder() {
        String oid = prefs.getString(KEY_ACTIVE_ORDER_ID, null);
        return oid != null && !oid.isEmpty();
    }
}
