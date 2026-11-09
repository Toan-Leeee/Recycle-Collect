package vn.recyclecollect.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_chat);
        AppData data = AppData.get(this);
        Request request = data.find(getIntent().getStringExtra("request_id"));
        if (request == null
                || !(data.email.equals(request.ownerEmail)
                        || data.email.equals(request.collectorEmail))
                || !(request.status.equals("Đã nhận") || request.status.equals("Hoàn tất"))) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.chat_title))
                .setText("Hội thoại · " + request.id + " · " + request.material);
        boolean owner = data.email.equals(request.ownerEmail);
        ((TextView) findViewById(R.id.chat_partner))
                .setText((owner ? "Người thu gom: " : "Người dân: ")
                        + (owner ? request.collectorEmail : request.ownerEmail));
        LinearLayout messages = findViewById(R.id.chat_messages);
        messages.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("recycle_chat", MODE_PRIVATE);
        JSONArray saved;
        try {
            saved = new JSONArray(prefs.getString(request.id, "[]"));
        } catch (Exception e) {
            saved = new JSONArray();
        }
        for (int i = 0; i < saved.length(); i++) {
            JSONObject item = saved.optJSONObject(i);
            if (item != null) addBubble(messages, item.optString("sender"),
                    item.optString("text"), item.optLong("sentAt", 0));
        }
        TextView empty = new TextView(this);
        empty.setText("Chưa có tin nhắn. Hãy trao đổi thời gian và địa điểm thu gom.");
        empty.setTextColor(getColor(R.color.muted));
        empty.setPadding(12, 20, 12, 20);
        if (saved.length() == 0) messages.addView(empty);
        final JSONArray conversation = saved;
        EditText input = findViewById(R.id.chat_input);
        Button send = findViewById(R.id.btn_next);
        if (request.status.equals("Hoàn tất")) {
            input.setEnabled(false);
            input.setHint("Hội thoại chỉ đọc sau khi hoàn tất");
            send.setVisibility(android.view.View.GONE);
            return;
        }
        send
                .setOnClickListener(v -> {
                    String message = input.getText().toString().trim();
                    if (message.isEmpty()) {
                        input.setError("Nhập tin nhắn");
                        return;
                    }
                    try {
                        JSONObject item = new JSONObject();
                        item.put("sender", data.email);
                        item.put("text", message);
                        item.put("sentAt", System.currentTimeMillis());
                        conversation.put(item);
                        prefs.edit().putString(request.id, conversation.toString()).apply();
                        messages.removeView(empty);
                        addBubble(messages, data.email, message, item.optLong("sentAt"));
                        input.setText("");
                    } catch (Exception e) {
                        Toast.makeText(this, "Không lưu được tin nhắn", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addBubble(LinearLayout parent, String sender, String message, long sentAt) {
        TextView bubble = new TextView(this);
        String time = sentAt > 0
                ? new java.text.SimpleDateFormat("HH:mm dd/MM", java.util.Locale.getDefault())
                        .format(new java.util.Date(sentAt))
                : "";
        bubble.setText(sender + (time.isEmpty() ? "" : " · " + time) + "\n" + message);
        bubble.setTextSize(15);
        bubble.setTextColor(getColor(R.color.ink));
        int pad = (int) (14 * getResources().getDisplayMetrics().density);
        bubble.setPadding(pad, pad, pad, pad);
        boolean mine = sender.equals(AppData.get(this).email);
        bubble.setBackgroundResource(mine ? R.drawable.bg_chat_mine : R.drawable.bg_card);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.74f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, -2);
        params.gravity = mine ? android.view.Gravity.END : android.view.Gravity.START;
        params.bottomMargin = pad;
        parent.addView(bubble, params);
    }
}
