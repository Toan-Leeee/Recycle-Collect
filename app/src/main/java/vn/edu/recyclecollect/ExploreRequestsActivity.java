package vn.edu.recyclecollect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ExploreRequestsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_explore_requests);
        AppData data = AppData.get(this);
        if (!data.role.equals("collector")) {
            finish();
            return;
        }
        Spinner filter = findViewById(R.id.material_filter);
        List<String> choices = new ArrayList<>();
        choices.add("Tất cả vật liệu");
        choices.addAll(data.prices.keySet());
        filter.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices));
        LinearLayout list = findViewById(R.id.explore_list);
        Runnable update =
                () -> {
                    list.removeAllViews();
                    int count = 0;
                    for (Request request : data.requests)
                        if (request.status.equals("Đang chờ")
                                && (filter.getSelectedItemPosition() == 0
                                        || request.material.equals(
                                                filter.getSelectedItem().toString()))) {
                            count++;
                            View card =
                                    LayoutInflater.from(this)
                                            .inflate(R.layout.item_request_wire, list, false);
                            MaterialImages.showCategory((ImageView) card.findViewById(R.id.item_image), request.material);
                            ((TextView) card.findViewById(R.id.item_title))
                                    .setText(request.id + " · " + request.material);
                            ((TextView) card.findViewById(R.id.item_weight))
                                    .setText(request.estimatedKg + " kg");
                            ((TextView) card.findViewById(R.id.item_status))
                                    .setText(request.status);
                            list.addView(card);
                            card.setOnClickListener(v -> {
                                Intent intent = new Intent(this, RequestDetailActivity.class);
                                intent.putExtra("request_id", request.id);
                                startActivity(intent);
                            });
                        }
                    if (count == 0) {
                        TextView empty = new TextView(this);
                        empty.setText("Không có yêu cầu phù hợp");
                        list.addView(empty);
                    }
                };
        filter.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {
                        update.run();
                    }

                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
        SeekBar distance = findViewById(R.id.distance_filter);
        distance.setEnabled(false);
        findViewById(R.id.btn_filter).setOnClickListener(v -> update.run());
        next(R.id.btn_next, MapActivity.class);
    }
}
