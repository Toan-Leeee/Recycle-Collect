package vn.edu.recyclecollect;

import android.content.Intent;
import android.hardware.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends BaseActivity implements SensorEventListener {
    private SensorManager sensors;
    private Sensor accelerometer, light;
    private TextView sensorText;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_home);
        AppData d = AppData.get(this);
        String name = new LocalAccounts(this).name(d.email);
        if (name.isEmpty())
            name =
                    d.role.equals("resident")
                            ? "Minh Anh"
                            : d.role.equals("collector") ? "Người thu gom" : "Admin";
        ((TextView) findViewById(R.id.title)).setText("Xin chào, " + name);
        ((TextView) findViewById(R.id.role))
                .setText(
                        d.role.equals("collector")
                                ? "Người thu gom"
                                : d.role.equals("admin") ? "Quản trị viên" : "Người dân");
        showRecentRequests(d);
        sensorText = findViewById(R.id.sensor_status);
        sensors = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        light = sensors.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorText.setText(
                "Cảm biến: gia tốc "
                        + (accelerometer != null ? "sẵn sàng" : "không có")
                        + " · ánh sáng "
                        + (light != null ? "sẵn sàng" : "không có"));
        next(R.id.btn_materials, MaterialsActivity.class);
        next(R.id.btn_create, CreateRequestActivity.class);
        next(R.id.btn_requests, MyRequestsActivity.class);
        next(R.id.btn_profile, ProfileActivity.class);
        next(R.id.btn_explore, ExploreRequestsActivity.class);
        next(R.id.btn_history, HistoryActivity.class);
        if (d.role.equals("admin")) {
            findViewById(R.id.btn_admin).setVisibility(View.VISIBLE);
            next(R.id.btn_admin, AdminMaterialsActivity.class);
        }
        if (!d.role.equals("resident")) findViewById(R.id.btn_create).setVisibility(View.GONE);
        if (!d.role.equals("collector")) findViewById(R.id.btn_explore).setVisibility(View.GONE);
        if (d.role.equals("admin")) {
            findViewById(R.id.btn_requests).setVisibility(View.GONE);
            findViewById(R.id.btn_history).setVisibility(View.GONE);
            findViewById(R.id.recent_heading).setVisibility(View.GONE);
            findViewById(R.id.recent_list).setVisibility(View.GONE);
        }
        if (d.role.equals("collector")) {
            findViewById(R.id.btn_explore).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.recent_heading)).setText("Đơn đã nhận gần đây");
        }
    }

    private void showRecentRequests(AppData data) {
        LinearLayout list = findViewById(R.id.recent_list);
        int count = 0;
        for (Request request : data.mine()) {
            if (count++ >= 2) break;
            View card = LayoutInflater.from(this).inflate(R.layout.item_request_wire, list, false);
            MaterialImages.showCategory((ImageView) card.findViewById(R.id.item_image), request.material);
            ((TextView) card.findViewById(R.id.item_title))
                    .setText(request.id + " · " + request.material);
            ((TextView) card.findViewById(R.id.item_weight))
                    .setText("Khối lượng dự kiến: " + request.estimatedKg + " kg");
            ((TextView) card.findViewById(R.id.item_status)).setText(request.status);
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, RequestDetailActivity.class);
                intent.putExtra("request_id", request.id);
                startActivity(intent);
            });
            list.addView(card);
        }
        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có yêu cầu gần đây");
            list.addView(empty);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensors.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (light != null) sensors.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensors != null) sensors.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            sensorText.setText(
                    "Cảm biến hoạt động · gia tốc: "
                            + String.format(java.util.Locale.US, "%.1f", e.values[0])
                            + " m/s² · ánh sáng: "
                            + (light != null ? "sẵn sàng" : "không có"));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
