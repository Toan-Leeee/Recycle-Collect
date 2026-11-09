package vn.recyclecollect.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class FirebaseSession {
    private FirebaseSession() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences("firebase_profile_demo", Context.MODE_PRIVATE);
    }

    public static void remember(Context context, FirebaseUser user, String name, String role) {
        String safeRole = "collector".equals(role) ? "collector" : "resident";
        prefs(context).edit()
                .putString(user.getUid() + "_role", safeRole)
                .putString(user.getUid() + "_name", name)
                .apply();
    }

    public static String name(Context context, FirebaseUser user) {
        if (user == null) return "";
        String saved = prefs(context).getString(user.getUid() + "_name", "");
        if (!saved.isEmpty()) return saved;
        return user.getDisplayName() == null ? "" : user.getDisplayName();
    }

    public static void enter(Activity activity, FirebaseUser user, String suggestedRole) {
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) return;
        SharedPreferences saved = prefs(activity);
        String role = saved.getString(user.getUid() + "_role", "");
        if (role.isEmpty()) {
            role = "collector".equals(suggestedRole) ? "collector" : "resident";
            String name = user.getDisplayName() == null ? "" : user.getDisplayName();
            remember(activity, user, name, role);
        }
        AppData data = AppData.get(activity);
        data.email = user.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        data.role = role;
        Intent home = new Intent(activity, HomeActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(home);
    }

    public static void signOut(Activity activity) {
        FirebaseAuth.getInstance().signOut();
        AppData.get(activity).email = "";
        AppData.get(activity).role = "resident";
        CredentialManager.create(activity).clearCredentialStateAsync(
                new ClearCredentialStateRequest(),
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override public void onResult(@NonNull Void result) {}
                    @Override public void onError(@NonNull ClearCredentialException error) {}
                });
    }
}
