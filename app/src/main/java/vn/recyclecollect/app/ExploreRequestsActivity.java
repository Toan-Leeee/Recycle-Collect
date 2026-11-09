package vn.recyclecollect.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class ExploreRequestsActivity extends BaseActivity {
    private Spinner materialFilter;
    private SeekBar distanceFilter;
    private LinearLayout requests;
    private TextView distanceLabel;
    private Location collectorLocation;
    private boolean radiusApplied;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_explore_requests);
        if (!AppData.get(this).role.equals("collector")) {
            finish();
            return;
        }
        materialFilter = findViewById(R.id.material_filter);
        distanceFilter = findViewById(R.id.distance_filter);
        distanceLabel = findViewById(R.id.distance_label);
        requests = findViewById(R.id.explore_list);

        List<String> choices = new ArrayList<>();
        choices.add("Tất cả vật liệu");
        choices.addAll(AppData.get(this).prices.keySet());
        materialFilter.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, choices));
        String selected = getIntent().getStringExtra("material_filter");
        if (selected != null) materialFilter.setSelection(Math.max(0, choices.indexOf(selected)));

        int km = Math.max(1, Math.min(10, getIntent().getIntExtra("radius_km", 5)));
        distanceFilter.setMax(9);
        distanceFilter.setProgress(km - 1);
        distanceLabel.setText("Khoảng cách tối đa: " + km + " km");
        distanceFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                distanceLabel.setText("Khoảng cách tối đa: " + (progress + 1) + " km");
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        findViewById(R.id.btn_filter).setOnClickListener(v -> applyFilters());
        findViewById(R.id.btn_next).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("material_filter", selectedMaterial());
            intent.putExtra("radius_km", distanceFilter.getProgress() + 1);
            intent.putExtra("radius_applied", radiusApplied);
            startActivity(intent);
        });
        collectorLocation = lastKnownLocation();
        radiusApplied = getIntent().getBooleanExtra("radius_applied", false)
                && collectorLocation != null;
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requests != null) render();
    }

    private String selectedMaterial() {
        return materialFilter.getSelectedItemPosition() == 0
                ? "" : materialFilter.getSelectedItem().toString();
    }

    private void applyFilters() {
        collectorLocation = lastKnownLocation();
        radiusApplied = collectorLocation != null;
        if (!radiusApplied) {
            Toast.makeText(this, "Chưa có vị trí GPS; đang lọc theo vật liệu",
                    Toast.LENGTH_LONG).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 41);
            }
        }
        render();
    }

    private void render() {
        if (requests == null || materialFilter.getSelectedItem() == null) return;
        requests.removeAllViews();
        int count = 0;
        for (Request request : AppData.get(this).requests) {
            if (!request.status.equals("Đang chờ")) continue;
            if (!selectedMaterial().isEmpty()
                    && !request.material.equals(selectedMaterial())) continue;
            if (radiusApplied) {
                float km = CollectorCards.distanceKm(collectorLocation, request);
                if (Float.isNaN(km) || km > distanceFilter.getProgress() + 1) continue;
            }
            requests.addView(CollectorCards.create(this, request, collectorLocation,
                    true, this::render));
            count++;
        }
        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText(radiusApplied
                    ? "Không có yêu cầu có vị trí GPS trong phạm vi đã chọn"
                    : "Không có yêu cầu phù hợp");
            empty.setPadding(18, 18, 18, 18);
            requests.addView(empty);
        }
    }

    private Location lastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return null;
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            Location gps = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location network = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return gps != null ? gps : network;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == 41 && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) applyFilters();
    }
}
