package vn.recyclecollect.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class MaterialsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_materials);
        AppData data = AppData.get(this);
        LinearLayout list = findViewById(R.id.material_list);
        float density = getResources().getDisplayMetrics().density;
        int pad = (int) (12 * density);
        NumberFormat number = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        for (Map.Entry<String, Integer> material : data.prices.entrySet()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, pad, 0, pad);

            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            MaterialImages.showMaterial(this, image, material.getKey());
            LinearLayout.LayoutParams imageParams =
                    new LinearLayout.LayoutParams((int) (56 * density), (int) (56 * density));
            imageParams.setMargins(0, 0, pad, 0);
            row.addView(image, imageParams);

            TextView name = new TextView(this);
            name.setText(material.getKey());
            name.setTextColor(getColor(R.color.ink));
            name.setTextSize(14);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));

            TextView price = new TextView(this);
            price.setText(number.format(material.getValue()) + " đ/kg");
            price.setTextColor(getColor(R.color.ink));
            price.setTextSize(13);
            row.addView(price);
            list.addView(row);

            View line = new View(this);
            line.setBackgroundColor(getColor(R.color.line));
            list.addView(line, new LinearLayout.LayoutParams(-1, Math.max(1, (int) density)));
        }

        if (data.role.equals("resident")) {
            next(R.id.btn_next, CreateRequestActivity.class);
        } else {
            findViewById(R.id.btn_next).setVisibility(View.GONE);
        }
    }
}
