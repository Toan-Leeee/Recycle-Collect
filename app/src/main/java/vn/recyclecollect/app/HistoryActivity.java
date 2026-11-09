package vn.recyclecollect.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

public class HistoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_history);
        AppData data = AppData.get(this);
        double kg = 0;
        long total = 0;
        int count = 0;
        LinearLayout list = findViewById(R.id.history_list);
        for (Request request : data.mine())
            if (request.status.equals("Hoàn tất")) {
                count++;
                kg += request.actualKg;
                long money = Math.round(request.actualKg * request.pricePerKg);
                total += money;
                View card =
                        LayoutInflater.from(this).inflate(R.layout.item_request_wire, list, false);
                MaterialImages.showCategory(
                        (ImageView) card.findViewById(R.id.item_image), request.material);
                ((TextView) card.findViewById(R.id.item_title))
                        .setText(request.id + " · " + request.material);
                ((TextView) card.findViewById(R.id.item_weight))
                        .setText(request.actualKg + " kg · " + money(money));
                String date = request.completedAt > 0
                        ? " · " + new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(new java.util.Date(request.completedAt))
                        : "";
                ((TextView) card.findViewById(R.id.item_status)).setText("Hoàn tất" + date);
                card.setOnClickListener(v -> {
                    Intent intent = new Intent(this, RequestDetailActivity.class);
                    intent.putExtra("request_id", request.id);
                    startActivity(intent);
                });
                list.addView(card);
            }
        ((TextView) findViewById(R.id.history_weight))
                .setText(String.format(new Locale("vi", "VN"), "%.1f kg", kg));
        ((TextView) findViewById(R.id.history_total)).setText(money(total));
        ((TextView) findViewById(R.id.history_count)).setText("Giao dịch gần đây (" + count + ")");
        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có giao dịch hoàn tất");
            list.addView(empty);
        }
    }

    private String money(long value) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " đ";
    }
}
