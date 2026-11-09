package vn.recyclecollect.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);
        EditText e = findViewById(R.id.email), p = findViewById(R.id.password);
        Button login = findViewById(R.id.btn_login);
        if ((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                && getIntent().hasExtra("preview_email")) {
            e.setText(getIntent().getStringExtra("preview_email"));
        }
        login.setOnClickListener(v -> {
                    String email = e.getText().toString().trim()
                            .toLowerCase(java.util.Locale.ROOT);
                    String pass = p.getText().toString();
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        e.setError("Email không hợp lệ");
                        return;
                    }
                    if (pass.isEmpty()) {
                        p.setError("Nhập mật khẩu");
                        return;
                    }
                    if (LocalAccounts.isDemo(email)) {
                        String role = new LocalAccounts(this).login(email, pass);
                        if (role == null) {
                            Toast.makeText(this, "Email hoặc mật khẩu chưa đúng",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FirebaseAuth.getInstance().signOut();
                        AppData data = AppData.get(this);
                        data.email = email;
                        data.role = role;
                        open(HomeActivity.class);
                        return;
                    }
                    login.setEnabled(false);
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(this, task -> {
                                login.setEnabled(true);
                                if (task.isSuccessful()) {
                                    FirebaseSession.enter(this,
                                            FirebaseAuth.getInstance().getCurrentUser(),
                                            "resident");
                                } else {
                                    Toast.makeText(this, "Email hoặc mật khẩu chưa đúng, hoặc chưa có mạng",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
        findViewById(R.id.btn_register).setOnClickListener(v -> open(RegisterActivity.class));
        findViewById(R.id.btn_google).setOnClickListener(v -> GoogleLogin.start(this, "resident"));
        findViewById(R.id.btn_forgot_password).setOnClickListener(v -> {
            Intent reset = new Intent(this, ForgotPasswordActivity.class);
            reset.putExtra("email", e.getText().toString().trim());
            startActivity(reset);
        });
    }
}
