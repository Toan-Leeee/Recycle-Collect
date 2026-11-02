# Thu gom tái chế — Android Java

Mở thư mục này bằng Android Studio. Dự án có 15 màn hình Activity và 15 file `activity_*.xml` tương ứng. Build bằng `gradlew.bat :app:assembleDebug`.

## Tài khoản demo

| Vai trò | Email | Mật khẩu |
| --- | --- | --- |
| Người dân | `dan@demo.vn` | `demo123` |
| Thu gom | `gom@demo.vn` | `demo123` |
| Admin | `admin@demo.vn` | `demo123` |

Có thể đăng ký tài khoản người dân hoặc người thu gom cục bộ. Luồng demo: đăng nhập người dân → tạo yêu cầu → đăng nhập thu gom → tìm yêu cầu → nhận đơn → nhập kết quả → xem lịch sử. Admin có thể thêm/sửa tên và đơn giá vật liệu.

## Giới hạn hiện tại

- Yêu cầu, tài khoản, hồ sơ và chat mới lưu trong bộ nhớ cục bộ của thiết bị. Chưa kết nối Firebase hoặc backend; chưa đồng bộ giữa nhiều thiết bị.
- Màn bản đồ dùng Leaflet 1.9.4 và ảnh bản đồ OpenStreetMap, hiển thị vị trí thiết bị và các yêu cầu chờ nhận đã lưu tọa độ. Chỉ thấy dữ liệu cục bộ trên cùng thiết bị; chưa đồng bộ người dùng giữa nhiều thiết bị. Lọc khoảng cách chưa được tính thực.
- Camera hiện hiển thị ảnh xem trước kích thước nhỏ, chưa lưu ảnh thật vào yêu cầu.
- Giá là giá tham khảo trong demo, không tích hợp thanh toán.
- Quy tắc phân quyền phía client chỉ phục vụ bản demo; sản phẩm triển khai thật cần xác thực và phân quyền phía backend.

APK debug nằm tại `app/build/outputs/apk/debug/app-debug.apk` sau khi build.
