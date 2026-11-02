package vn.edu.recyclecollect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapActivity extends BaseActivity {
    private static final int LOCATION_PERMISSION = 31;
    private WebView map;
    private TextView status;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_map);
        AppData data = AppData.get(this);
        if (!data.role.equals("collector")) {
            finish();
            return;
        }

        status = findViewById(R.id.map_status);
        map = findViewById(R.id.request_map);
        map.getSettings().setJavaScriptEnabled(true);
        map.getSettings().setAllowFileAccess(true);
        map.getSettings()
                .setUserAgentString(
                        map.getSettings().getUserAgentString() + " RecycleCollectJava/1.0");
        map.addJavascriptInterface(new MapBridge(), "Android");
        map.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view, WebResourceRequest request) {
                        if (request.getUrl().toString().equals("file:///android_asset/map.html"))
                            return false;
                        if (request.getUrl().getScheme().equals("https")) {
                            startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                        }
                        return true;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (url.equals("file:///android_asset/map.html")) showRequests();
                    }
                });
        map.loadUrl("file:///android_asset/map.html");
        findViewById(R.id.btn_my_location).setOnClickListener(v -> locateMe());
        next(R.id.btn_next, ExploreRequestsActivity.class);
    }

    private void showRequests() {
        JSONArray points = new JSONArray();
        for (Request request : AppData.get(this).requests) {
            if (!request.status.equals("Đang chờ") || request.location == null) continue;
            String[] parts = request.location.split(",");
            if (parts.length != 2) continue;
            try {
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());
                if (Math.abs(lat) > 90 || Math.abs(lng) > 180) continue;
                JSONObject point = new JSONObject();
                point.put("id", request.id);
                point.put("material", request.material);
                point.put("kg", request.estimatedKg);
                point.put("status", request.status);
                point.put("lat", Math.round(lat * 1000d) / 1000d);
                point.put("lng", Math.round(lng * 1000d) / 1000d);
                points.put(point);
            } catch (Exception ignored) {
                // A request without valid coordinates cannot be placed on the map.
            }
        }
        map.evaluateJavascript("renderRequests(" + points + ")", null);
        status.setText(points.length() + " yêu cầu chờ nhận có tọa độ");
    }

    private void locateMe() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            String provider =
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                            ? LocationManager.GPS_PROVIDER
                            : LocationManager.NETWORK_PROVIDER;
            if (!locationManager.isProviderEnabled(provider)) {
                status.setText("Hãy bật GPS hoặc dịch vụ vị trí");
                return;
            }
            Location last = locationManager.getLastKnownLocation(provider);
            if (last != null) showMyLocation(last);
            locationListener =
                    location -> {
                        showMyLocation(location);
                        if (locationManager != null)
                            locationManager.removeUpdates(locationListener);
                    };
            locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);
            if (last == null) status.setText("Đang tìm vị trí của bạn...");
        } catch (SecurityException e) {
            status.setText("Chưa có quyền truy cập vị trí");
        }
    }

    private void showMyLocation(Location location) {
        map.evaluateJavascript(
                "showMyLocation(" + location.getLatitude() + "," + location.getLongitude() + ")",
                null);
        status.setText(
                "Vị trí của tôi: " + location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == LOCATION_PERMISSION
                && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) locateMe();
    }

    @Override
    protected void onStop() {
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (map != null) {
            map.removeJavascriptInterface("Android");
            map.destroy();
        }
        super.onDestroy();
    }

    private final class MapBridge {
        @JavascriptInterface
        public void openRequest(String id) {
            runOnUiThread(() -> {
                Request request = AppData.get(MapActivity.this).find(id);
                if (request == null || !request.status.equals("Đang chờ")) {
                    Toast.makeText(
                                    MapActivity.this,
                                    "Yêu cầu không còn khả dụng",
                                    Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                Intent intent = new Intent(MapActivity.this, RequestDetailActivity.class);
                intent.putExtra("request_id", request.id);
                startActivity(intent);
            });
        }
    }
}
