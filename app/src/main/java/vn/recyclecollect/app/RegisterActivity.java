package vn.recyclecollect.app;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_register);
        EditText name = findViewById(R.id.register_name),
                email = findViewById(R.id.register_email),
                password = findViewById(R.id.register_password),
                confirm = findViewById(R.id.register_confirm);
        RadioGroup roles = findViewById(R.id.register_role);
        findViewById(R.id.register_google).setOnClickListener(v -> {
            String role = roles.getCheckedRadioButtonId() == R.id.role_collector
                    ? "collector" : "resident";
            GoogleLogin.start(this, role);
        });
        findViewById(R.id.btn_next)
                .setOnClickListener(v -> {
                    String n = name.getText().toString().trim(),
                            e = email.getText().toString().trim().toLowerCase(),
                            p = password.getText().toString();
                    if (n.isEmpty()) {
                        name.setError("Nhập họ tên");
                        return;
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                        email.setError("Email không hợp lệ");
                        return;
                    }
                    if (p.length() < 6) {
                        password.setError("Mật khẩu tối thiểu 6 ký tự");
                        return;
                    }
                    if (!p.equals(confirm.getText().toString())) {
                        confirm.setError("Mật khẩu xác nhận không khớp");
                        return;
                    }
                    String role =
                            roles.getCheckedRadioButtonId() == R.id.role_collector
                                    ? "collector"
                                    : "resident";
                    if (LocalAccounts.isDemo(e)) {
                        email.setError("Email này dành cho tài khoản demo");
                        return;
                    }
                    findViewById(R.id.btn_next).setEnabled(false);
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(e, p)
                            .addOnCompleteListener(this, task -> {
                                findViewById(R.id.btn_next).setEnabled(true);
                                if (!task.isSuccessful()) {
                                    Toast.makeText(this,
                                            "Không đăng ký được. Kiểm tra email, mật khẩu và mạng.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user == null) return;
                                FirebaseSession.remember(this, user, n, role);
                                user.updateProfile(new UserProfileChangeRequest.Builder()
                                                .setDisplayName(n).build())
                                        .addOnCompleteListener(this, ignored -> {
                                            FirebaseAuth.getInstance().signOut();
                                            Toast.makeText(this, "Đăng ký thành công. Hãy đăng nhập.",
                                                    Toast.LENGTH_SHORT).show();
                                            open(LoginActivity.class);
                                            finish();
                                        });
                            });
                });
        findViewById(R.id.register_login).setOnClickListener(v -> open(LoginActivity.class));
    }
}
