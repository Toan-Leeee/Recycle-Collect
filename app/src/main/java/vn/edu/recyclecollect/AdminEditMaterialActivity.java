package vn.edu.recyclecollect;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class AdminEditMaterialActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_admin_edit_material);
        AppData data = AppData.get(this);
        if (!data.role.equals("admin")) {
            finish();
            return;
        }
        EditText name = findViewById(R.id.material_name), price = findViewById(R.id.material_price);
        String oldName = getIntent().getStringExtra("material_name");
        if (oldName != null) {
            name.setText(oldName);
            price.setText(String.valueOf(data.prices.get(oldName)));
        }
        findViewById(R.id.btn_next)
                .setOnClickListener(v -> {
                    String value = name.getText().toString().trim();
                    int amount;
                    try {
                        amount = Integer.parseInt(price.getText().toString());
                    } catch (NumberFormatException e) {
                        price.setError("Nhập đơn giá hợp lệ");
                        return;
                    }
                    if (value.isEmpty()) {
                        name.setError("Nhập tên vật liệu");
                        return;
                    }
                    if (amount <= 0) {
                        price.setError("Đơn giá phải lớn hơn 0");
                        return;
                    }
                    if (oldName != null && !oldName.equals(value)) data.prices.remove(oldName);
                    data.prices.put(value, amount);
                    data.save();
                    Toast.makeText(this, "Đã lưu vật liệu", Toast.LENGTH_SHORT).show();
                    open(AdminMaterialsActivity.class);
                });
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }
}
