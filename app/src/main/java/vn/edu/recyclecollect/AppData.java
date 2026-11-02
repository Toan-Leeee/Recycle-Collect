package vn.edu.recyclecollect;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AppData {
    private static AppData instance;
    public String email = "";
    public String role = "resident";
    public final List<Request> requests = new ArrayList<>();
    public final Map<String, Integer> prices = new LinkedHashMap<>();
    private final SharedPreferences prefs;
    private int nextId = 5;

    private AppData(Context context) {
        prefs =
                context.getApplicationContext()
                        .getSharedPreferences("recycle_demo", Context.MODE_PRIVATE);
        prices.put("Giấy / carton", 3000);
        prices.put("Nhựa", 5000);
        prices.put("Kim loại", 10000);
        prices.put("Thủy tinh", 1000);
        prices.put("Thiết bị điện tử", 15000);
        Request a = new Request("TG001", "dan@demo.vn", "Giấy / carton", 5);
        a.pricePerKg = prices.get(a.material);
        requests.add(a);
        Request b = new Request("TG002", "dan@demo.vn", "Nhựa", 3);
        b.pricePerKg = prices.get(b.material);
        b.status = "Đã nhận";
        b.collectorEmail = "gom@demo.vn";
        requests.add(b);
        Request c = new Request("TG003", "dan@demo.vn", "Kim loại", 2);
        c.pricePerKg = prices.get(c.material);
        c.actualKg = 2;
        c.status = "Hoàn tất";
        requests.add(c);
        Request d = new Request("TG004", "dan@demo.vn", "Giấy / carton", 4.5);
        d.pricePerKg = prices.get(d.material);
        d.actualKg = 4.5;
        d.status = "Hoàn tất";
        requests.add(d);
        try {
            JSONObject savedPrices = new JSONObject(prefs.getString("prices", "{}"));
            java.util.Iterator<String> keys = savedPrices.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                prices.put(key, savedPrices.getInt(key));
            }
            JSONArray saved = new JSONArray(prefs.getString("requests", "[]"));
            if (saved.length() > 0) {
                requests.clear();
                for (int i = 0; i < saved.length(); i++) {
                    JSONObject j = saved.getJSONObject(i);
                    Request r =
                            new Request(
                                    j.getString("id"),
                                    j.getString("owner"),
                                    j.getString("material"),
                                    j.getDouble("estimated"));
                    r.status = j.optString("status", "Đang chờ");
                    r.collectorEmail = j.optString("collector", "");
                    r.actualKg = j.optDouble("actual", 0);
                    r.location = j.optString("location", "");
                    r.photoPath = j.optString("photo", "");
                    r.pricePerKg = j.optInt("price", 0);
                    r.completedAt = j.optLong("completedAt", 0);
                    requests.add(r);
                }
            }
            nextId = prefs.getInt("nextId", 5);
        } catch (Exception ignored) {
        }
    }

    public static synchronized AppData get(Context c) {
        if (instance == null) instance = new AppData(c);
        return instance;
    }

    public synchronized Request addRequest(String material, double kg, String owner) {
        Request r =
                new Request(
                        String.format(java.util.Locale.US, "TG%03d", nextId++),
                        owner,
                        material,
                        kg);
        r.pricePerKg = prices.containsKey(material) ? prices.get(material) : 0;
        requests.add(0, r);
        save();
        return r;
    }

    public Request find(String id) {
        for (Request r : requests) if (r.id.equals(id)) return r;
        return null;
    }

    public List<Request> mine() {
        List<Request> out = new ArrayList<>();
        for (Request r : requests)
            if (r.ownerEmail.equals(email) || email.equals(r.collectorEmail)) out.add(r);
        return out;
    }

    public synchronized void save() {
        JSONArray items = new JSONArray();
        try {
            for (Request r : requests) {
                JSONObject j = new JSONObject();
                j.put("id", r.id);
                j.put("owner", r.ownerEmail);
                j.put("material", r.material);
                j.put("estimated", r.estimatedKg);
                j.put("status", r.status);
                j.put("collector", r.collectorEmail == null ? "" : r.collectorEmail);
                j.put("actual", r.actualKg);
                j.put("location", r.location == null ? "" : r.location);
                j.put("photo", r.photoPath == null ? "" : r.photoPath);
                j.put("price", r.pricePerKg);
                j.put("completedAt", r.completedAt);
                items.put(j);
            }
        } catch (Exception ignored) {
        }
        prefs.edit()
                .putString("requests", items.toString())
                .putString("prices", new JSONObject(prices).toString())
                .putInt("nextId", nextId)
                .apply();
    }
}
