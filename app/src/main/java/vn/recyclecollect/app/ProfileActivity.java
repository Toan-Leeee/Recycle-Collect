package vn.recyclecollect.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_profile);
        AppData data = AppData.get(this);
        SharedPreferences prefs = getSharedPreferences("recycle_profile", MODE_PRIVATE);
        String key = data.email + "_";
        String displayName =
                prefs.getString(key + "name", new LocalAccounts(this).name(data.email));
        if (displayName.isEmpty()
                && FirebaseAuth.getInstance().getCurrentUser() != null) {
            displayName = FirebaseSession.name(this,
                    FirebaseAuth.getInstance().getCurrentUser());
        }
        if (displayName.isEmpty()) displayName = "Nguyễn Minh Anh";
        ((TextView) findViewById(R.id.profile_heading)).setText(displayName);
        ((TextView) findViewById(R.id.profile_role))
                .setText(
                        data.role.equals("admin")
                                ? "Quản trị viên"
                                : data.role.equals("collector") ? "Người thu gom" : "Người dân");
        ((TextView) findViewById(R.id.profile_email)).setText(data.email);
        EditText name = findViewById(R.id.profile_name),
                phone = findViewById(R.id.profile_phone),
                address = findViewById(R.id.profile_address);
        name.setText(displayName);
        phone.setText(prefs.getString(key + "phone", ""));
        address.setText(prefs.getString(key + "address", ""));
        findViewById(R.id.btn_next)
                .setOnClickListener(v -> {
                    String n = name.getText().toString().trim(),
                            p = phone.getText().toString().trim(),
                            a = address.getText().toString().trim();
                    if (n.isEmpty()) {
                        name.setError("Nhập họ tên");
                        return;
                    }
                    if (!p.isEmpty() && !p.matches("[0-9]{9,11}")) {
                        phone.setError("Số điện thoại không hợp lệ");
                        return;
                    }
                    prefs.edit()
                            .putString(key + "name", n)
                            .putString(key + "phone", p)
                            .putString(key + "address", a)
                            .apply();
                    ((TextView) findViewById(R.id.profile_heading)).setText(n);
                    Toast.makeText(this, "Đã lưu hồ sơ", Toast.LENGTH_SHORT).show();
                });
        findViewById(R.id.btn_logout)
                .setOnClickListener(v -> {
                    FirebaseSession.signOut(this);
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
    }
}
