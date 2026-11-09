package vn.recyclecollect.app;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_forgot_password);
        EditText email = findViewById(R.id.reset_email);
        String initialEmail = getIntent().getStringExtra("email");
        if (initialEmail != null) email.setText(initialEmail);
        TextView status = findViewById(R.id.reset_status);
        Button send = findViewById(R.id.btn_send_reset);
        send.setOnClickListener(view -> {
            String address = email.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            if (!Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
                email.setError("Nhập địa chỉ email hợp lệ");
                return;
            }
            send.setEnabled(false);
            status.setText("Đang gửi...");
            FirebaseAuth.getInstance().sendPasswordResetEmail(address)
                    .addOnCompleteListener(this, task -> {
                        send.setEnabled(true);
                        status.setText(task.isSuccessful()
                                ? "Nếu email đã đăng ký, hãy kiểm tra hộp thư và thư rác để mở liên kết đặt lại mật khẩu."
                                : "Không gửi được liên kết. Kiểm tra kết nối mạng rồi thử lại.");
                    });
        });
    }
}
