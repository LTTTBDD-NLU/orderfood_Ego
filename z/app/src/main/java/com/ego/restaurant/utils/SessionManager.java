package com.ego.restaurant.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME  = "EgoSession";
    private static final String KEY_ID     = "user_id";
    private static final String KEY_ROLE   = "role";
    private static final String KEY_NAME   = "full_name";
    private static final String KEY_EMAIL  = "email";
    private static final String KEY_LOGGED = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(int userId, String role, String fullName, String email) {
        editor.putInt(KEY_ID,     userId);
        editor.putString(KEY_ROLE,  role);
        editor.putString(KEY_NAME,  fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_LOGGED, true);
        editor.apply();
    }

    public boolean isLoggedIn()     { return prefs.getBoolean(KEY_LOGGED, false); }
    public int    getUserId()       { return prefs.getInt(KEY_ID, -1); }
    public String getRole()         { return prefs.getString(KEY_ROLE, "GUEST"); }
    public String getFullName()     { return prefs.getString(KEY_NAME, ""); }
    public String getEmail()        { return prefs.getString(KEY_EMAIL, ""); }

    public String getUid()          { return String.valueOf(getUserId()); }

    public void clearSession() { editor.clear(); editor.apply(); }
}
