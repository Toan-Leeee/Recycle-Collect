package vn.recyclecollect.app;

public class Request {
    public final String id;
    public String ownerEmail, material, address, status, collectorEmail, photoPath, location;
    public double estimatedKg, actualKg;
    public int pricePerKg;
    public long completedAt;

    public Request(String id, String ownerEmail, String material, double estimatedKg) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.material = material;
        this.estimatedKg = estimatedKg;
        this.status = "Đang chờ";
    }
}
