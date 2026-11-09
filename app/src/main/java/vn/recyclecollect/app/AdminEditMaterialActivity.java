package vn.recyclecollect.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AdminEditMaterialActivity extends BaseActivity {
    private static final int PICK_PHOTO = 41;
    private Bitmap selectedPhoto;
    private ImageView photoPreview;

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
        photoPreview = findViewById(R.id.material_photo_preview);
        String oldName = getIntent().getStringExtra("material_name");
        if (oldName != null) {
            name.setText(oldName);
            price.setText(String.valueOf(data.prices.get(oldName)));
        }
        MaterialImages.showMaterial(this, photoPreview, oldName == null ? "" : oldName);
        findViewById(R.id.btn_material_photo).setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
            pick.setType("image/*");
            startActivityForResult(Intent.createChooser(pick, "Chọn ảnh vật liệu"), PICK_PHOTO);
        });
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
                    String photoName = null;
                    if (selectedPhoto != null) {
                        photoName = savePhoto(selectedPhoto);
                        if (photoName == null) {
                            Toast.makeText(this, "Không lưu được ảnh", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    String previousPhoto = oldName == null ? null : data.materialPhotos.remove(oldName);
                    if (oldName != null && !oldName.equals(value)) data.prices.remove(oldName);
                    data.prices.put(value, amount);
                    if (photoName != null) data.materialPhotos.put(value, photoName);
                    else if (previousPhoto != null) data.materialPhotos.put(value, previousPhoto);
                    data.save();
                    Toast.makeText(this, "Đã lưu vật liệu", Toast.LENGTH_SHORT).show();
                    open(AdminMaterialsActivity.class);
                });
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PHOTO || resultCode != RESULT_OK || data == null
                || data.getData() == null) return;
        try (InputStream stream = getContentResolver().openInputStream(data.getData())) {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, bounds);
            int sample = 1;
            while (bounds.outWidth / sample > 1200 || bounds.outHeight / sample > 1200) {
                sample *= 2;
            }
            try (InputStream imageStream = getContentResolver().openInputStream(data.getData())) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = sample;
                selectedPhoto = BitmapFactory.decodeStream(imageStream, null, options);
            }
            if (selectedPhoto == null) throw new IllegalArgumentException("Invalid image");
            photoPreview.setImageBitmap(selectedPhoto);
        } catch (Exception e) {
            Toast.makeText(this, "Không đọc được ảnh đã chọn", Toast.LENGTH_SHORT).show();
        }
    }

    private String savePhoto(Bitmap image) {
        String fileName = "material_" + System.currentTimeMillis() + ".jpg";
        File file = new File(getFilesDir(), fileName);
        try (FileOutputStream output = new FileOutputStream(file)) {
            if (image.compress(Bitmap.CompressFormat.JPEG, 85, output)) return fileName;
        } catch (Exception ignored) {
        }
        file.delete();
        return null;
    }
}
