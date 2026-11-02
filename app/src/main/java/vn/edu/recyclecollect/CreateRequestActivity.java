package vn.edu.recyclecollect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CreateRequestActivity extends BaseActivity {
    private TextView locationText;
    private String location = "";
    private Bitmap selectedPhoto;
    private Request editing;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_create_request);
        AppData data = AppData.get(this);
        if (!data.role.equals("resident")) {
            finish();
            return;
        }
        Spinner spinner = findViewById(R.id.material);
        ArrayList<String> names = new ArrayList<>(data.prices.keySet());
        spinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
        locationText = findViewById(R.id.location_text);
        editing = data.find(getIntent().getStringExtra("request_id"));
        if (editing != null) {
            if (!editing.ownerEmail.equals(data.email) || !editing.status.equals("Đang chờ")) {
                finish();
                return;
            }
            spinner.setSelection(Math.max(0, names.indexOf(editing.material)));
            ((EditText) findViewById(R.id.weight)).setText(String.valueOf(editing.estimatedKg));
            location = editing.location == null ? "" : editing.location;
            if (!location.isEmpty()) locationText.setText("Vị trí: " + location);
            ((android.widget.Button) findViewById(R.id.btn_next)).setText("Lưu thay đổi");
            ImageView preview = findViewById(R.id.photo_preview);
            if (MaterialImages.showRequestPhoto(this, preview, editing)) {
                preview.setVisibility(View.VISIBLE);
                findViewById(R.id.photo_hint).setVisibility(View.GONE);
            }
        }
        findViewById(R.id.btn_camera).setOnClickListener(v -> takePhoto());
        findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("image/*");
            startActivityForResult(Intent.createChooser(pick, "Chọn ảnh vật liệu"), 23);
        });
        findViewById(R.id.btn_location).setOnClickListener(v -> getLocation());
        findViewById(R.id.btn_next)
                .setOnClickListener(v -> {
                    EditText weight = findViewById(R.id.weight);
                    double kg;
                    try {
                        kg = Double.parseDouble(weight.getText().toString());
                        if (kg <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        weight.setError("Nhập khối lượng lớn hơn 0");
                        return;
                    }
                    Request request =
                            editing == null
                                    ? data.addRequest(
                                            spinner.getSelectedItem().toString(), kg, data.email)
                                    : editing;
                    request.material = spinner.getSelectedItem().toString();
                    request.estimatedKg = kg;
                    request.location = location;
                    request.pricePerKg = data.prices.getOrDefault(request.material, 0);
                    if (selectedPhoto != null) {
                        String saved = savePhoto(request.id, selectedPhoto);
                        if (saved == null) {
                            Toast.makeText(this, "Không lưu được ảnh. Hãy thử lại", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        request.photoPath = saved;
                    }
                    data.save();
                    Toast.makeText(
                                    this,
                                    (editing == null ? "Đã tạo " : "Đã sửa ") + request.id,
                                    Toast.LENGTH_SHORT)
                            .show();
                    open(MyRequestsActivity.class);
                });
    }

    private void takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 20);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) startActivityForResult(intent, 21);
        else Toast.makeText(this, "Thiết bị chưa có ứng dụng camera", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        if (requestCode == 21 && data.getExtras() != null) {
            Object photo = data.getExtras().get("data");
            if (photo instanceof Bitmap) showPhoto((Bitmap) photo);
        } else if (requestCode == 23 && data.getData() != null) {
            try (InputStream stream = getContentResolver().openInputStream(data.getData())) {
                Bitmap image = BitmapFactory.decodeStream(stream);
                if (image != null) showPhoto(image);
                else Toast.makeText(this, "Không đọc được ảnh đã chọn", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Không đọc được ảnh đã chọn", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPhoto(Bitmap image) {
        int max = Math.max(image.getWidth(), image.getHeight());
        if (max > 1200) {
            float scale = 1200f / max;
            image = Bitmap.createScaledBitmap(image,
                    Math.round(image.getWidth() * scale),
                    Math.round(image.getHeight() * scale), true);
        }
        selectedPhoto = image;
        ImageView preview = findViewById(R.id.photo_preview);
        preview.setImageBitmap(image);
        preview.setVisibility(View.VISIBLE);
        findViewById(R.id.photo_hint).setVisibility(View.GONE);
    }

    private String savePhoto(String requestId, Bitmap image) {
        String name = "request_" + requestId + ".jpg";
        File file = new File(getFilesDir(), name);
        try (FileOutputStream output = new FileOutputStream(file)) {
            if (!image.compress(Bitmap.CompressFormat.JPEG, 85, output)) return null;
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            if (code == 20) takePhoto();
            else if (code == 22) getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 22);
            return;
        }
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location last = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (last != null) {
            setLocation(last);
            return;
        }
        locationText.setText("Đang lấy vị trí GPS...");
        try {
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    new LocationListener() {
                        public void onLocationChanged(Location value) {
                            setLocation(value);
                            manager.removeUpdates(this);
                        }
                    });
        } catch (Exception e) {
            locationText.setText("Không lấy được vị trí. Kiểm tra GPS.");
        }
    }

    private void setLocation(Location value) {
        location = value.getLatitude() + ", " + value.getLongitude();
        locationText.setText("Vị trí: " + location);
    }
}
