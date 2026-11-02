package vn.edu.recyclecollect;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

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
                    if (!new LocalAccounts(this).register(e, p, n, role)) {
                        email.setError("Email đã tồn tại hoặc không thể lưu");
                        return;
                    }
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    open(LoginActivity.class);
                });
        findViewById(R.id.register_login).setOnClickListener(v -> open(LoginActivity.class));
    }
}
