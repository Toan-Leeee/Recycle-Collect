# Thu gom tái chế — Android Java

Mở thư mục này bằng Android Studio. Build bằng `gradlew.bat :app:assembleDebug`. Tên gói Android là `vn.recyclecollect.app`.

## Xác thực Firebase

- Tệp `app/google-services.json` phải thuộc ứng dụng Android có tên gói `vn.recyclecollect.app` và chứa OAuth client cho Google Sign-In.
- Tài khoản mới đăng ký bằng email/mật khẩu qua Firebase Authentication. Trên màn đăng nhập có nút Google và link Quên mật khẩu; Firebase gửi **liên kết** đặt lại mật khẩu đến email.
- Vai trò người dân/người thu gom của tài khoản Firebase đang lưu theo UID trong `SharedPreferences` trên thiết bị. Tài khoản Google đăng nhập lần đầu ở màn Đăng nhập mặc định là người dân; ở màn Đăng ký có thể chọn người thu gom. Vai trò và dữ liệu đơn hàng chưa đồng bộ giữa thiết bị.
- Các tài khoản demo dưới đây vẫn xử lý cục bộ để xem luồng mẫu; chúng không nằm trong Firebase và không thể dùng email đặt lại mật khẩu.

## Tài khoản demo

| Vai trò | Email | Mật khẩu |
| --- | --- | --- |
| Người dân | `dan@demo.vn` | `demo123` |
| Thu gom | `gom@demo.vn` | `demo123` |
| Admin | `admin@demo.vn` | `demo123` |

Luồng demo: đăng nhập người dân → tạo yêu cầu → đăng nhập thu gom → tìm yêu cầu → nhận đơn → nhập kết quả → xem lịch sử. Admin có thể thêm/sửa tên, đơn giá và ảnh minh họa vật liệu.

## Giới hạn hiện tại

- Firebase Authentication chỉ xử lý tài khoản mới. Yêu cầu, hồ sơ, vai trò và chat vẫn lưu cục bộ; chưa đồng bộ giữa nhiều thiết bị. Cần cơ sở dữ liệu và quy tắc bảo mật phía máy chủ để hoàn thiện phân quyền.
- Màn bản đồ dùng Leaflet 1.9.4 và ảnh bản đồ OpenStreetMap, hiển thị vị trí thiết bị và các yêu cầu chờ nhận đã lưu tọa độ. Chỉ thấy dữ liệu cục bộ trên cùng thiết bị; chưa đồng bộ người dùng giữa nhiều thiết bị. Lọc khoảng cách chưa được tính thực.
- Ảnh vật liệu của yêu cầu và ảnh danh mục được lưu cục bộ trong app; chưa tải lên cloud.
- Giá là giá tham khảo trong demo, không tích hợp thanh toán.
- Quy tắc phân quyền phía client chỉ phục vụ bản demo; sản phẩm triển khai thật cần xác thực và phân quyền phía backend.

APK debug nằm tại `app/build/outputs/apk/debug/app-debug.apk` sau khi build.
