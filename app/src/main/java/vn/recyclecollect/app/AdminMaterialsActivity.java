package vn.recyclecollect.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class AdminMaterialsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_admin_materials);
        AppData data = AppData.get(this);
        if (!data.role.equals("admin")) {
            finish();
            return;
        }
        LinearLayout list = findViewById(R.id.admin_list);
        int pad = (int) (10 * getResources().getDisplayMetrics().density);
        for (Map.Entry<String, Integer> item : data.prices.entrySet()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, pad, 0, pad);
            ImageView photo = new ImageView(this);
            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            MaterialImages.showMaterial(this, photo, item.getKey());
            LinearLayout.LayoutParams imageSize = new LinearLayout.LayoutParams(56 * pad / 10, 56 * pad / 10);
            imageSize.setMargins(0, 0, pad, 0);
            row.addView(photo, imageSize);
            TextView name = new TextView(this);
            name.setText(item.getKey());
            name.setTextColor(getColor(R.color.ink));
            name.setTextSize(13);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            TextView price = new TextView(this);
            price.setText(
                    NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(item.getValue())
                            + " đ/kg");
            price.setTextColor(getColor(R.color.ink));
            price.setTextSize(13);
            row.addView(price);
            row.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminEditMaterialActivity.class);
                intent.putExtra("material_name", item.getKey());
                startActivity(intent);
            });
            list.addView(row);
            View line = new View(this);
            line.setBackgroundColor(getColor(R.color.line));
            list.addView(line, new LinearLayout.LayoutParams(-1, 1));
        }
        next(R.id.btn_next, AdminEditMaterialActivity.class);
    }
}
