package vn.edu.recyclecollect;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

public class MaterialsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_materials);
        AppData data = AppData.get(this);
        NumberFormat number = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        ((TextView) findViewById(R.id.price_paper))
                .setText(number.format(data.prices.getOrDefault("Giấy / carton", 0)) + " đ / kg");
        ((TextView) findViewById(R.id.price_plastic))
                .setText(number.format(data.prices.getOrDefault("Nhựa", 0)) + " đ / kg");
        ((TextView) findViewById(R.id.price_metal))
                .setText(number.format(data.prices.getOrDefault("Kim loại", 0)) + " đ / kg");
        if (data.role.equals("resident")) next(R.id.btn_next, CreateRequestActivity.class);
        else findViewById(R.id.btn_next).setVisibility(View.GONE);
    }
}
