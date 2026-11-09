package vn.recyclecollect.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RequestDetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_request_detail);
        AppData data = AppData.get(this);
        Request request = data.find(getIntent().getStringExtra("request_id"));
        if (request == null) {
            Toast.makeText(this, "Không tìm thấy yêu cầu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        boolean allowed = data.email.equals(request.ownerEmail)
                || data.email.equals(request.collectorEmail)
                || (data.role.equals("collector") && request.status.equals("Đang chờ"));
        if (!allowed) {
            Toast.makeText(this, "Bạn không có quyền xem yêu cầu này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ImageView photo = findViewById(R.id.detail_photo);
        if (MaterialImages.showRequestPhoto(this, photo, request)) {
            findViewById(R.id.detail_photo_note).setVisibility(View.GONE);
        } else {
            MaterialImages.showCategory(photo, request.material);
        }
        ((TextView) findViewById(R.id.detail_material))
                .setText(request.id + " · " + request.material);
        ((TextView) findViewById(R.id.detail_id)).setText(request.id);
        ((TextView) findViewById(R.id.detail_status)).setText(
                "Trạng thái: " + (request.status.equals("Đã nhận") && request.actualKg > 0
                        ? "Đã nhận · chờ người dân xác nhận" : request.status));
        ((TextView) findViewById(R.id.detail_weight))
                .setText("Khối lượng dự kiến       " + request.estimatedKg + " kg");
        boolean owner = request.ownerEmail.equals(data.email),
                collector = data.role.equals("collector");
        ((TextView) findViewById(R.id.detail_location))
                .setText(
                        "Địa điểm                 "
                                + (!owner && collector && !data.email.equals(request.collectorEmail)
                                        ? "Chỉ hiện sau khi nhận đơn"
                                        : request.location == null || request.location.isEmpty()
                                        ? "Chưa cung cấp"
                                        : request.location));
        ((TextView) findViewById(R.id.detail_collector))
                .setText(
                        "Người thu gom           "
                                + (request.collectorEmail == null
                                                || request.collectorEmail.isEmpty()
                                        ? "Chưa có"
                                        : request.collectorEmail));
        Button action = findViewById(R.id.btn_next),
                cancel = findViewById(R.id.btn_cancel),
                edit = findViewById(R.id.btn_edit);
        Button chat = findViewById(R.id.btn_chat);
        if ((owner || data.email.equals(request.collectorEmail))
                && (request.status.equals("Đã nhận") || request.status.equals("Hoàn tất"))) {
            chat.setVisibility(View.VISIBLE);
            chat.setText(request.status.equals("Hoàn tất")
                    ? "Xem hội thoại" : "Tin nhắn");
            chat.setOnClickListener(v -> openRequest(ChatActivity.class, request.id));
        }
        if (collector && request.status.equals("Đang chờ")) {
            action.setText("Nhận yêu cầu");
            action.setOnClickListener(v -> new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Nhận yêu cầu " + request.id)
                    .setMessage("Bạn sẽ phụ trách thu gom đơn này.")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Nhận đơn", (dialog, which) -> {
                        if (!request.status.equals("Đang chờ")) return;
                        request.status = "Đã nhận";
                        request.collectorEmail = data.email;
                        data.save();
                        Toast.makeText(this, "Đã nhận " + request.id, Toast.LENGTH_SHORT).show();
                        recreate();
                    }).show());
        } else if (collector
                && data.email.equals(request.collectorEmail)
                && request.status.equals("Đã nhận")) {
            action.setText(request.actualKg > 0 ? "Xem kết quả đã gửi" : "Nhập kết quả thu gom");
            action.setOnClickListener(v -> openRequest(CollectionResultActivity.class, request.id));
        } else if ((owner || data.email.equals(request.collectorEmail))
                && (request.status.equals("Đã nhận") || request.status.equals("Hoàn tất"))) {
            action.setText(request.status.equals("Hoàn tất") ? "Xem hội thoại" : "Mở trò chuyện");
            action.setOnClickListener(v -> openRequest(ChatActivity.class, request.id));
        } else action.setVisibility(View.GONE);
        if (owner && request.status.equals("Đang chờ")) {
            cancel.setOnClickListener(v -> new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Hủy yêu cầu " + request.id)
                    .setMessage("Bạn có chắc muốn hủy yêu cầu đang chờ?")
                    .setNegativeButton("Giữ lại", null)
                    .setPositiveButton("Hủy yêu cầu", (dialog, which) -> {
                        if (!request.status.equals("Đang chờ")) return;
                        request.status = "Đã hủy";
                        data.save();
                        Toast.makeText(this, "Đã hủy yêu cầu", Toast.LENGTH_SHORT).show();
                        recreate();
                    }).show());
            edit.setOnClickListener(v -> openRequest(CreateRequestActivity.class, request.id));
        } else if (owner && request.status.equals("Đã nhận")) {
            cancel.setVisibility(View.GONE);
            edit.setText(request.actualKg > 0 ? "Xem và xác nhận kết quả" : "Chờ kết quả thu gom");
            edit.setEnabled(request.actualKg > 0);
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener(v -> openRequest(CollectionResultActivity.class, request.id));
        } else {
            cancel.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        }
    }

    private void openRequest(Class<?> screen, String id) {
        Intent intent = new Intent(this, screen);
        intent.putExtra("request_id", id);
        startActivity(intent);
    }
}
