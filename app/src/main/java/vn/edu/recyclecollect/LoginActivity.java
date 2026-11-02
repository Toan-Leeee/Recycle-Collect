package vn.edu.recyclecollect;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);
        EditText e = findViewById(R.id.email), p = findViewById(R.id.password);
        if ((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                && getIntent().hasExtra("preview_email")) {
            e.setText(getIntent().getStringExtra("preview_email"));
        }
        findViewById(R.id.btn_login)
                .setOnClickListener(v -> {
                    String email = e.getText().toString().trim().toLowerCase();
                    String pass = p.getText().toString();
                    String role = new LocalAccounts(this).login(email, pass);
                    if (TextUtils.isEmpty(email) || role == null) {
                        Toast.makeText(this, "Email hoặc mật khẩu chưa đúng", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    AppData d = AppData.get(this);
                    d.email = email;
                    d.role = role;
                    open(HomeActivity.class);
                });
        findViewById(R.id.btn_register).setOnClickListener(v -> open(RegisterActivity.class));
    }
}
