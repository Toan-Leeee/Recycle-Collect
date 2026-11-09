package vn.recyclecollect.app;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

final class CollectorCards {
    private CollectorCards() {}

    static View create(Activity activity, Request request, Location collectorLocation,
                       boolean showAccept, Runnable changed) {
        View card = LayoutInflater.from(activity)
                .inflate(R.layout.item_collector_request, null, false);
        MaterialImages.showCategory(
                (ImageView) card.findViewById(R.id.collector_item_image), request.material);
        ((TextView) card.findViewById(R.id.collector_item_title))
                .setText(request.id + " · " + request.material);
        float distance = distanceKm(collectorLocation, request);
        String distanceText = Float.isNaN(distance)
                ? "Chưa có khoảng cách GPS"
                : String.format(Locale.getDefault(), "%.1f km", distance);
        ((TextView) card.findViewById(R.id.collector_item_meta))
                .setText(request.estimatedKg + " kg · " + distanceText);
        ((TextView) card.findViewById(R.id.collector_item_status)).setText(request.status);
        Button action = card.findViewById(R.id.collector_item_action);
        Button chat = card.findViewById(R.id.collector_item_chat);
        if (showAccept && request.status.equals("Đang chờ")) {
            action.setText("Nhận đơn");
            action.setOnClickListener(v -> confirmAccept(activity, request, changed));
        } else {
            action.setText("Chi tiết");
            action.setOnClickListener(v -> open(activity, RequestDetailActivity.class, request.id));
        }
        if (AppData.get(activity).email.equals(request.collectorEmail)
                && (request.status.equals("Đã nhận") || request.status.equals("Hoàn tất"))) {
            chat.setVisibility(View.VISIBLE);
            chat.setOnClickListener(v -> open(activity, ChatActivity.class, request.id));
        }
        card.setOnClickListener(v -> open(activity, RequestDetailActivity.class, request.id));
        return card;
    }

    static void confirmAccept(Activity activity, Request request, Runnable changed) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Nhận yêu cầu " + request.id)
                .setMessage("Sau khi nhận đơn, bạn có thể xem địa điểm và nhắn tin với người dân.")
                .setNegativeButton("Để sau", null)
                .setPositiveButton("Nhận đơn", (dialog, which) -> {
                    if (!request.status.equals("Đang chờ")) {
                        Toast.makeText(activity, "Đơn đã được nhận", Toast.LENGTH_SHORT).show();
                        changed.run();
                        return;
                    }
                    AppData data = AppData.get(activity);
                    request.collectorEmail = data.email;
                    request.status = "Đã nhận";
                    data.save();
                    Toast.makeText(activity, "Đã nhận " + request.id, Toast.LENGTH_SHORT).show();
                    changed.run();
                }).show();
    }

    static float distanceKm(Location collectorLocation, Request request) {
        if (collectorLocation == null || request.location == null) return Float.NaN;
        String[] parts = request.location.split(",");
        if (parts.length != 2) return Float.NaN;
        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());
            if (Math.abs(lat) > 90 || Math.abs(lng) > 180) return Float.NaN;
            float[] meters = new float[1];
            Location.distanceBetween(collectorLocation.getLatitude(),
                    collectorLocation.getLongitude(), lat, lng, meters);
            return meters[0] / 1000f;
        } catch (NumberFormatException e) {
            return Float.NaN;
        }
    }

    static void open(Activity activity, Class<?> screen, String id) {
        Intent intent = new Intent(activity, screen);
        intent.putExtra("request_id", id);
        activity.startActivity(intent);
    }
}
