package vn.recyclecollect.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MyRequestsActivity extends BaseActivity {
    private String selectedStatus;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_my_requests);
        AppData data = AppData.get(this);
        findViewById(R.id.tab_waiting).setOnClickListener(v -> selectStatus("Đang chờ"));
        findViewById(R.id.tab_accepted).setOnClickListener(v -> selectStatus("Đã nhận"));
        findViewById(R.id.tab_done).setOnClickListener(v -> selectStatus("Hoàn tất"));
        render(null);
        Button create = findViewById(R.id.btn_next);
        if (data.role.equals("collector")) {
            create.setText("Tìm yêu cầu chờ nhận");
            create.setOnClickListener(v -> open(ExploreRequestsActivity.class));
        } else if (data.role.equals("admin")) create.setVisibility(View.GONE);
        else create.setOnClickListener(v -> open(CreateRequestActivity.class));
    }

    private void selectStatus(String status) {
        selectedStatus = status;
        render(status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (findViewById(R.id.request_list) != null) render(selectedStatus);
    }

    private void render(String status) {
        LinearLayout list = findViewById(R.id.request_list);
        list.removeAllViews();
        int count = 0;
        for (Request request : AppData.get(this).mine()) {
            if (status != null && !request.status.equals(status)) continue;
            count++;
            if (AppData.get(this).role.equals("collector")) {
                list.addView(CollectorCards.create(this, request, null,
                        false, () -> render(selectedStatus)));
                continue;
            }
            View card = LayoutInflater.from(this).inflate(R.layout.item_request_wire, list, false);
            MaterialImages.showCategory((ImageView) card.findViewById(R.id.item_image), request.material);
            ((TextView) card.findViewById(R.id.item_title))
                    .setText(request.id + " · " + request.material);
            ((TextView) card.findViewById(R.id.item_weight))
                    .setText("Khối lượng dự kiến: " + request.estimatedKg + " kg");
            ((TextView) card.findViewById(R.id.item_status)).setText(request.status);
            Button chat = card.findViewById(R.id.item_chat);
            if (request.ownerEmail.equals(AppData.get(this).email)
                    && (request.status.equals("Đã nhận")
                        || request.status.equals("Hoàn tất"))
                    && request.collectorEmail != null
                    && !request.collectorEmail.isEmpty()) {
                chat.setVisibility(View.VISIBLE);
                chat.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("request_id", request.id);
                    startActivity(intent);
                });
            }
            list.addView(card);
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, RequestDetailActivity.class);
                intent.putExtra("request_id", request.id);
                startActivity(intent);
            });
        }
        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có yêu cầu");
            empty.setPadding(18, 18, 18, 18);
            list.addView(empty);
        }
    }
}
