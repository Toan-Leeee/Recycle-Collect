package vn.recyclecollect.app;

import android.app.Activity;
import android.os.CancellationSignal;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public final class GoogleLogin {
    private GoogleLogin() {}

    public static void start(Activity activity, String selectedRole) {
        GetGoogleIdOption google = new GetGoogleIdOption.Builder()
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(google)
                .build();
        CredentialManager.create(activity).getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(@NonNull GetCredentialResponse result) {
                        Credential value = result.getCredential();
                        if (!(value instanceof CustomCredential)
                                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                        .equals(value.getType())) {
                            Toast.makeText(activity, "Không nhận được tài khoản Google",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            String token = GoogleIdTokenCredential.createFrom(
                                    ((CustomCredential) value).getData()).getIdToken();
                            FirebaseAuth.getInstance()
                                    .signInWithCredential(GoogleAuthProvider.getCredential(token, null))
                                    .addOnCompleteListener(activity, task -> {
                                        if (task.isSuccessful()) {
                                            FirebaseSession.enter(activity,
                                                    FirebaseAuth.getInstance().getCurrentUser(),
                                                    selectedRole);
                                        } else {
                                            Toast.makeText(activity,
                                                    "Đăng nhập Google thất bại. Kiểm tra cấu hình SHA-1 và mạng.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } catch (Exception error) {
                            Toast.makeText(activity, "Không đọc được tài khoản Google",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException error) {
                        Toast.makeText(activity, "Đã hủy hoặc không thể mở đăng nhập Google",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
