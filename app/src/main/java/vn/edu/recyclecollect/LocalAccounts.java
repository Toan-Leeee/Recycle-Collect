package vn.edu.recyclecollect;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Tài khoản cục bộ để chạy bản demo. Bản phát hành cần Firebase Auth và phân quyền từ máy chủ.
public final class LocalAccounts {
    private final SharedPreferences prefs;

    public LocalAccounts(Context context) {
        prefs =
                context.getApplicationContext()
                        .getSharedPreferences("recycle_accounts", Context.MODE_PRIVATE);
    }

    public boolean exists(String email) {
        return prefs.contains(email.toLowerCase());
    }

    public boolean register(String email, String password, String name, String role) {
        email = email.trim().toLowerCase();
        if (exists(email)
                || email.equals("dan@demo.vn")
                || email.equals("gom@demo.vn")
                || email.equals("admin@demo.vn")) return false;
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            JSONObject user = new JSONObject();
            user.put("salt", Base64.getEncoder().encodeToString(salt));
            user.put("hash", hash(password, salt));
            user.put("name", name);
            user.put("role", role);
            return prefs.edit().putString(email, user.toString()).commit();
        } catch (Exception e) {
            return false;
        }
    }

    public String login(String email, String password) {
        email = email.trim().toLowerCase();
        if (email.equals("dan@demo.vn")
                || email.equals("gom@demo.vn")
                || email.equals("admin@demo.vn")) {
            if (!password.equals("demo123")) return null;
            return email.startsWith("admin")
                    ? "admin"
                    : email.startsWith("gom") ? "collector" : "resident";
        }
        try {
            JSONObject user = new JSONObject(prefs.getString(email, ""));
            byte[] salt = Base64.getDecoder().decode(user.getString("salt"));
            String expected = user.getString("hash");
            String actual = hash(password, salt);
            return java.security.MessageDigest.isEqual(
                            Base64.getDecoder().decode(expected),
                            Base64.getDecoder().decode(actual))
                    ? user.getString("role")
                    : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String name(String email) {
        try {
            return new JSONObject(prefs.getString(email.toLowerCase(), "{}")).optString("name", "");
        } catch (Exception e) {
            return "";
        }
    }

    private String hash(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, 256);
        try {
            return Base64.getEncoder()
                    .encodeToString(
                            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                                    .generateSecret(spec)
                                    .getEncoded());
        } finally {
            spec.clearPassword();
        }
    }
}
