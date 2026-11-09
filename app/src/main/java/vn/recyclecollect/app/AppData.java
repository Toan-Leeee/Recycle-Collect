package vn.recyclecollect.app;

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
    public final Map<String, String> materialPhotos = new LinkedHashMap<>();
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
        a.location = "21.0285, 105.8542";
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
            JSONObject savedPhotos = new JSONObject(prefs.getString("materialPhotos", "{}"));
            java.util.Iterator<String> photoKeys = savedPhotos.keys();
            while (photoKeys.hasNext()) {
                String key = photoKeys.next();
                materialPhotos.put(key, savedPhotos.optString(key, ""));
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
                    if (r.id.equals("TG001")
                            && r.ownerEmail.equals("dan@demo.vn")
                            && r.location.isEmpty()) {
                        r.location = "21.0285, 105.8542";
                    }
                    r.photoPath = j.optString("photo", "");
                    r.pricePerKg = j.optInt("price", 0);
                    r.completedAt = j.optLong("completedAt", 0);
                    requests.add(r);
                }
            }
            nextId = prefs.getInt("nextId", 5);
        } catch (Exception ignored) {
        }
        if (prefs.getInt("demoSeedVersion", 0) < 1) {
            seedMoreDemoRequests();
            save();
            prefs.edit().putInt("demoSeedVersion", 1).apply();
        }
    }

    private void seedMoreDemoRequests() {
        // Tọa độ và khối lượng dưới đây chỉ phục vụ bản demo, chưa có ảnh vật liệu thực tế.
        addDemo("TG101", "Giấy / carton", 6.5, "Đang chờ", "21.0318, 105.8479", 0);
        addDemo("TG102", "Nhựa", 2.8, "Đang chờ", "21.0242, 105.8584", 0);
        addDemo("TG103", "Kim loại", 4.0, "Đang chờ", "21.0362, 105.8531", 0);
        addDemo("TG104", "Thủy tinh", 3.2, "Đang chờ", "21.0224, 105.8486", 0);
        addDemo("TG105", "Thiết bị điện tử", 1.5, "Đang chờ", "21.0291, 105.8632", 0);
        addDemo("TG106", "Giấy / carton", 8.0, "Đã nhận", "21.0346, 105.8421", 0);
        addDemo("TG107", "Nhựa", 5.5, "Đã nhận", "21.0189, 105.8558", 0);
        addDemo("TG108", "Kim loại", 2.4, "Đã nhận", "21.0264, 105.8375", 2.2);
        addDemo("TG109", "Thủy tinh", 4.0, "Hoàn tất", "21.0381, 105.8599", 3.8);
        addDemo("TG110", "Thiết bị điện tử", 1.2, "Hoàn tất", "21.0207, 105.8429", 1.1);
    }

    private void addDemo(String id, String material, double estimatedKg,
                         String status, String location, double actualKg) {
        if (find(id) != null) return;
        Request request = new Request(id, "dan@demo.vn", material, estimatedKg);
        request.status = status;
        request.location = location;
        request.pricePerKg = prices.getOrDefault(material, 0);
        request.actualKg = actualKg;
        if (!status.equals("Đang chờ")) request.collectorEmail = "gom@demo.vn";
        if (status.equals("Hoàn tất")) request.completedAt = System.currentTimeMillis();
        requests.add(request);
    }

    public static synchronized AppData get(Context c) {
        if (instance == null) instance = new AppData(c);
        return instance;
    }

    public synchronized Request addRequest(String material, double kg, String owner) {
        while (find(String.format(java.util.Locale.US, "TG%03d", nextId)) != null) nextId++;
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
                .putString("materialPhotos", new JSONObject(materialPhotos).toString())
                .putInt("nextId", nextId)
                .apply();
    }
}
