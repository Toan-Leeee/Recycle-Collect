package vn.recyclecollect.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

public class CollectionResultActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_collection_result);
        AppData data = AppData.get(this);
        Request request = data.find(getIntent().getStringExtra("request_id"));
        boolean collector = request != null && data.email.equals(request.collectorEmail);
        boolean owner = request != null && data.email.equals(request.ownerEmail);
        if (request == null || !(collector || owner)
                || !(request.status.equals("Đã nhận") || request.status.equals("Hoàn tất"))) {
            Toast.makeText(this, "Không thể xem kết quả đơn này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        TextView total = findViewById(R.id.result_total);
        ((TextView) findViewById(R.id.result_request))
                .setText(request.id + " · " + request.material);
        ((TextView) findViewById(R.id.result_price)).setText(money(request.pricePerKg) + " / kg");
        EditText weight = findViewById(R.id.actual_weight);
        Button action = findViewById(R.id.btn_next);
        TextView stateText = findViewById(R.id.result_state);
        if (request.actualKg > 0) {
            weight.setText(String.valueOf(request.actualKg));
            total.setText(money(Math.round(request.actualKg * request.pricePerKg)));
        } else {
            total.setText("—");
        }
        if (request.status.equals("Hoàn tất")) {
            weight.setEnabled(false);
            stateText.setText("Trạng thái: Hoàn tất");
            action.setVisibility(android.view.View.GONE);
        } else if (owner) {
            weight.setEnabled(false);
            if (request.actualKg <= 0) {
                stateText.setText("Người thu gom chưa gửi kết quả");
                action.setVisibility(android.view.View.GONE);
            } else {
                stateText.setText("Chờ bạn xác nhận kết quả thu gom");
                action.setText("Xác nhận hoàn tất");
                action.setOnClickListener(v -> new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Xác nhận thu gom")
                        .setMessage("Bạn đồng ý với khối lượng và tổng tiền đã hiển thị?")
                        .setNegativeButton("Xem lại", null)
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            if (!request.status.equals("Đã nhận") || request.actualKg <= 0) return;
                            request.status = "Hoàn tất";
                            request.completedAt = System.currentTimeMillis();
                            data.save();
                            Toast.makeText(this, "Đã xác nhận hoàn tất", Toast.LENGTH_SHORT).show();
                            open(HistoryActivity.class);
                            finish();
                        }).show());
            }
        } else if (request.actualKg > 0) {
            weight.setEnabled(false);
            stateText.setText("Đã gửi kết quả · chờ người dân xác nhận");
            action.setVisibility(android.view.View.GONE);
        } else {
            stateText.setText("Trạng thái: Đã nhận");
            action.setOnClickListener(v -> {
                try {
                    double kg = Double.parseDouble(weight.getText().toString());
                    if (kg <= 0) throw new NumberFormatException();
                    request.actualKg = kg;
                    data.save();
                    Toast.makeText(this, "Đã gửi kết quả, chờ người dân xác nhận", Toast.LENGTH_SHORT).show();
                    recreate();
                } catch (NumberFormatException e) {
                    weight.setError("Nhập khối lượng lớn hơn 0");
                }
            });
        }
        weight.addTextChangedListener(
                new TextWatcher() {
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            total.setText(
                                    money(
                                            Math.round(
                                                    Double.parseDouble(s.toString())
                                                            * request.pricePerKg)));
                        } catch (Exception e) {
                            total.setText("—");
                        }
                    }

                    public void afterTextChanged(Editable e) {}
                });
    }

    private String money(long value) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " đ";
    }
}
