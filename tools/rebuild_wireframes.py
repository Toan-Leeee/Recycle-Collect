"""Rebuild the 14 remaining XML screens from the Chapter 3 wireframes.

Run from the project root: python tools/rebuild_wireframes.py
"""
from pathlib import Path
from lxml import etree as ET

A = "http://schemas.android.com/apk/res/android"
OUT = Path(__file__).resolve().parents[1] / "app/src/main/res/layout"


def el(parent, tag, *, id=None, text=None, w="match_parent", h="wrap_content", **attrs):
    node = ET.SubElement(parent, tag) if parent is not None else ET.Element(tag, nsmap={"android": A})
    node.set(f"{{{A}}}layout_width", w)
    node.set(f"{{{A}}}layout_height", h)
    if id:
        node.set(f"{{{A}}}id", "@+id/" + id)
    if text is not None:
        node.set(f"{{{A}}}text", text)
    for key, value in attrs.items():
        node.set(f"{{{A}}}{key}", str(value))
    return node


def screen(pad="20dp", flexible=False):
    if flexible:
        root = el(None, "LinearLayout", orientation="vertical", background="@color/surface", padding=pad, h="match_parent")
        return root, root
    root = el(None, "ScrollView", h="match_parent", background="@color/surface", fillViewport="true")
    body = el(root, "LinearLayout", orientation="vertical", padding=pad)
    return root, body


def text(p, value, size="14sp", bold=False, top="0dp", color="@color/ink", id=None):
    return el(p, "TextView", id=id, text=value, textSize=size,
              textStyle="bold" if bold else "normal", textColor=color, layout_marginTop=top)


def label(p, value, top="16dp"):
    return text(p, value, "12sp", True, top, "@color/green_dark")


def field(p, hint, id, *, top="5dp", typ="text", value=None):
    node = el(p, "EditText", id=id, h="46dp", hint=hint, inputType=typ,
              background="@drawable/bg_card", paddingStart="10dp", paddingEnd="10dp",
              textSize="14sp", textColor="@color/ink", layout_marginTop=top)
    if value is not None:
        node.set(f"{{{A}}}text", value)
    return node


def button(p, value, id, top="14dp", w="match_parent"):
    node = el(p, "Button", id=id, text=value, w=w, h="46dp", layout_marginTop=top)
    if value in {"Đăng xuất", "Hủy", "Hủy yêu cầu", "Chỉnh sửa yêu cầu",
                 "Chụp ảnh vật liệu", "Lấy lại vị trí GPS", "Vị trí của tôi",
                 "Xem danh sách yêu cầu", "Xem bản đồ", "Tạo yêu cầu thu gom"}:
        node.set("style", "@style/SecondaryButton")
    return node


def line(p, top="10dp"):
    return el(p, "View", h="1dp", background="@color/line", layout_marginTop=top)


def card(p, name="Giấy và bìa carton", detail="Khối lượng dự kiến: 5 kg", state="Chờ nhận"):
    row = el(p, "LinearLayout", h="82dp", orientation="horizontal", gravity="center_vertical",
             background="@drawable/bg_card", padding="8dp", layout_marginTop="10dp")
    pic = el(row, "TextView", text="Ảnh", w="56dp", h="56dp", gravity="center",
             background="@drawable/bg_card", textColor="@color/muted", textSize="11sp")
    content = el(row, "LinearLayout", w="0dp", layout_weight="1", orientation="vertical", layout_marginStart="10dp")
    text(content, name, "13sp", True)
    text(content, detail, "11sp", top="4dp")
    text(content, state, "11sp", True, "4dp")
    return row


def save(name, root):
    path = OUT / f"activity_{name}.xml"
    path.write_bytes(ET.tostring(root, encoding="utf-8", xml_declaration=True, pretty_print=True))


# 3.2 Login
r, p = screen("22dp")
logo = el(p, "ImageView", w="76dp", h="76dp", src="@drawable/ic_recycle",
          contentDescription="Logo tái chế", layout_gravity="center_horizontal", layout_marginTop="28dp")
text(p, "THU GOM TÁI CHẾ", "14sp", True, "12dp", "@color/green").set(f"{{{A}}}gravity", "center")
text(p, "Đăng nhập", "24sp", True, "18dp").set(f"{{{A}}}gravity", "center")
label(p, "Email", "34dp")
field(p, "Nhập email", "email", typ="textEmailAddress")
label(p, "Mật khẩu")
field(p, "Nhập mật khẩu", "password", typ="textPassword")
button(p, "Đăng nhập", "btn_login", "28dp")
text(p, "Chưa có tài khoản? Đăng ký ngay", "13sp", True, "18dp", id="btn_register").set(f"{{{A}}}gravity", "center")
save("login", r)

# 3.3 Register
r, p = screen()
el(p, "ImageView", w="76dp", h="76dp", src="@drawable/ic_recycle", contentDescription="Logo tái chế", layout_gravity="center_horizontal", layout_marginTop="18dp")
label(p, "Họ và tên", "24dp")
field(p, "Nhập họ và tên", "register_name")
label(p, "Email")
field(p, "Nhập email", "register_email", typ="textEmailAddress")
label(p, "Mật khẩu")
field(p, "Tạo mật khẩu", "register_password", typ="textPassword")
label(p, "Xác nhận mật khẩu")
field(p, "Nhập lại mật khẩu", "register_confirm", typ="textPassword")
label(p, "Vai trò", "24dp")
roles = el(p, "RadioGroup", id="register_role", orientation="horizontal", h="48dp")
el(roles, "RadioButton", id="role_resident", text="Người dân", w="0dp", h="match_parent", layout_weight="1", checked="true", buttonTint="@color/ink")
el(roles, "RadioButton", id="role_collector", text="Người thu gom", w="0dp", h="match_parent", layout_weight="1", buttonTint="@color/ink")
button(p, "Đăng ký", "btn_next", "25dp")
text(p, "Đã có tài khoản? Đăng nhập", "13sp", True, "18dp", id="register_login").set(f"{{{A}}}gravity", "center")
save("register", r)

# 3.5 Profile
r, p = screen()
text(p, "Nguyễn Minh Anh", "18sp", True, "12dp", id="profile_heading")
text(p, "Người dân", "12sp", top="4dp", id="profile_role")
label(p, "Email", "24dp")
text(p, "minhanh@example.com", "14sp", top="5dp", id="profile_email")
label(p, "Họ và tên")
field(p, "Họ và tên", "profile_name")
label(p, "Số điện thoại")
field(p, "Nhập số điện thoại", "profile_phone", typ="phone")
label(p, "Địa chỉ")
field(p, "Nhập địa chỉ liên hệ", "profile_address", typ="textPostalAddress")
button(p, "Lưu thay đổi", "btn_next", "30dp")
button(p, "Đăng xuất", "btn_logout", "8dp")
save("profile", r)

# 3.6 Materials
r, p = screen()
head = el(p, "LinearLayout", orientation="horizontal", layout_marginTop="20dp")
first = text(head, "Vật liệu", "13sp", True)
first.set(f"{{{A}}}layout_width", "0dp")
first.set(f"{{{A}}}layout_weight", "1")
text(head, "Đơn giá tham khảo", "13sp", True).set(f"{{{A}}}layout_width", "wrap_content")
for material, price, price_id in [
    ("Giấy / carton", "3.000 đ/kg", "price_paper"),
    ("Nhựa PET", "5.000 đ/kg", "price_plastic"),
    ("Kim loại", "10.000 đ/kg", "price_metal"),
    ("Thủy tinh", "1.000 đ/kg", None),
    ("Điện tử", "15.000 đ/kg", None),
]:
    row = el(p, "LinearLayout", orientation="horizontal", layout_marginTop="18dp")
    first = text(row, material, "13sp")
    first.set(f"{{{A}}}layout_width", "0dp")
    first.set(f"{{{A}}}layout_weight", "1")
    text(row, price, "13sp", id=price_id).set(f"{{{A}}}layout_width", "wrap_content")
    line(p, "8dp")
text(p, "Đơn vị tính: đồng / kg", "12sp", top="26dp")
button(p, "Tạo yêu cầu thu gom", "btn_next", "48dp")
save("materials", r)

# 3.7 Create / edit request
r, p = screen()
label(p, "Loại vật liệu", "12dp")
el(p, "Spinner", id="material", h="46dp", background="@drawable/bg_card", layout_marginTop="5dp")
label(p, "Khối lượng dự kiến (kg)")
field(p, "5", "weight", typ="numberDecimal")
photo = el(p, "FrameLayout", h="114dp", background="@drawable/bg_card", layout_marginTop="16dp")
el(photo, "TextView", id="photo_hint", text="Chưa có ảnh vật liệu", h="match_parent", gravity="center", textColor="@color/muted", textSize="13sp")
el(photo, "ImageView", id="photo_preview", h="match_parent", visibility="gone", scaleType="centerCrop", contentDescription="Ảnh vật liệu")
button(p, "Chụp ảnh vật liệu", "btn_camera", "10dp")
button(p, "Chọn ảnh từ máy", "btn_gallery", "8dp")
label(p, "Địa điểm thu gom", "18dp")
el(p, "TextView", id="location_text", text="Địa chỉ từ vị trí GPS", h="46dp",
   gravity="center_vertical", background="@drawable/bg_card", paddingStart="10dp", textSize="13sp")
button(p, "Lấy lại vị trí GPS", "btn_location", "8dp")
button(p, "Gửi yêu cầu", "btn_next", "24dp")
save("create_request", r)

# 3.8 My requests
r, p = screen(flexible=True)
tabs = el(p, "LinearLayout", orientation="horizontal", h="45dp")
for value, id in [("Chờ nhận", "tab_waiting"), ("Đã nhận", "tab_accepted"), ("Hoàn tất", "tab_done")]:
    el(tabs, "TextView", id=id, text=value, w="0dp", h="match_parent", layout_weight="1",
       gravity="center", background="@drawable/bg_card", textColor="@color/ink", textSize="12sp")
sc = el(p, "ScrollView", h="0dp", layout_weight="1", layout_marginTop="12dp")
el(sc, "LinearLayout", id="request_list", orientation="vertical")
button(p, "Tạo yêu cầu mới", "btn_next", "10dp")
save("my_requests", r)

# 3.9 Request details
r, p = screen()
detail_photo = el(p, "FrameLayout", h="180dp", background="@drawable/bg_card", layout_marginTop="10dp")
el(detail_photo, "ImageView", id="detail_photo", h="match_parent", scaleType="centerCrop", contentDescription="Ảnh vật liệu")
el(detail_photo, "TextView", id="detail_photo_note", text="Ảnh minh họa · yêu cầu chưa có ảnh chụp", h="wrap_content", layout_gravity="bottom", background="@color/mint", padding="6dp", textColor="@color/ink", textSize="11sp")
text(p, "TG001 · Giấy carton", "17sp", True, "18dp", id="detail_material")
text(p, "TG001", "12sp", id="detail_id").set(f"{{{A}}}visibility", "gone")
text(p, "Trạng thái: Chờ nhận", "13sp", top="15dp", id="detail_status")
line(p)
text(p, "Khối lượng dự kiến: 5 kg", "13sp", top="10dp", id="detail_weight")
line(p)
text(p, "Địa điểm: Cầu Giấy, Hà Nội", "13sp", top="10dp", id="detail_location")
line(p)
text(p, "Người thu gom: Chưa có", "13sp", top="10dp", id="detail_collector")
button(p, "Nhận yêu cầu", "btn_next", "65dp")
chat_button = button(p, "Tin nhắn", "btn_chat", "8dp")
chat_button.set(f"{{{A}}}visibility", "gone")
chat_button.set(f"{{{A}}}drawableStart", "@drawable/ic_message")
button(p, "Chỉnh sửa yêu cầu", "btn_edit", "8dp")
button(p, "Hủy yêu cầu", "btn_cancel", "8dp")
save("request_detail", r)

# 3.10 Explore requests
r, p = screen(flexible=True)
label(p, "Loại vật liệu", "6dp")
el(p, "Spinner", id="material_filter", h="46dp", background="@drawable/bg_card")
text(p, "Khoảng cách tối đa: 5 km", "13sp", True, "20dp", id="distance_label")
el(p, "SeekBar", id="distance_filter", h="40dp", max="10", progress="5")
row = el(p, "LinearLayout", orientation="horizontal")
first = text(row, "1 km", "11sp")
first.set(f"{{{A}}}layout_width", "0dp")
first.set(f"{{{A}}}layout_weight", "1")
text(row, "10 km", "11sp").set(f"{{{A}}}layout_width", "wrap_content")
buttons = el(p, "LinearLayout", orientation="horizontal", layout_marginTop="8dp")
button(buttons, "Áp dụng bộ lọc", "btn_filter", "0dp", "0dp").set(f"{{{A}}}layout_weight", "1")
button(buttons, "Xem bản đồ", "btn_next", "0dp", "0dp").set(f"{{{A}}}layout_weight", "1")
sc = el(p, "ScrollView", h="0dp", layout_weight="1", layout_marginTop="15dp")
el(sc, "LinearLayout", id="explore_list", orientation="vertical")
save("explore_requests", r)

# 3.11 Map (keep real Leaflet WebView)
r, p = screen(flexible=True)
text(p, "Giấy / carton · Trong phạm vi 5 km", "12sp", top="5dp", id="map_status")
el(p, "WebView", id="request_map", h="0dp", layout_weight="1", layout_marginTop="15dp")
text(p, "Yêu cầu trong khu vực", "13sp", True, "10dp")
button(p, "Vị trí của tôi", "btn_my_location", "10dp")
el(p, "LinearLayout", id="map_preview_list", orientation="vertical", layout_marginTop="10dp")
button(p, "Xem danh sách yêu cầu", "btn_next", "8dp")
save("map", r)

# 3.12 Chat
r, p = screen(flexible=True)
text(p, "Trần Văn Bình · TG001", "13sp", True, "8dp", id="chat_title")
text(p, "Đang trò chuyện với người thu gom", "12sp", top="5dp", color="@color/muted", id="chat_partner")
line(p)
sc = el(p, "ScrollView", h="0dp", layout_weight="1", layout_marginTop="12dp")
el(sc, "LinearLayout", id="chat_messages", orientation="vertical")
row = el(p, "LinearLayout", orientation="horizontal", layout_marginTop="8dp")
field(row, "Nhập tin nhắn...", "chat_input", top="0dp").set(f"{{{A}}}layout_weight", "1")
row[-1].set(f"{{{A}}}layout_width", "0dp")
button(row, "Gửi", "btn_next", "0dp", "88dp")
save("chat", r)

# 3.13 Collection result
r, p = screen()
text(p, "TG001 · Giấy / carton", "15sp", True, "18dp", id="result_request")
label(p, "Khối lượng thực tế (kg)", "20dp")
field(p, "4,5", "actual_weight", typ="numberDecimal")
label(p, "Đơn giá theo bảng giá", "28dp")
text(p, "3.000 đ/kg", "15sp", True, "12dp", id="result_price").set(f"{{{A}}}gravity", "end")
line(p)
label(p, "Tổng tiền", "18dp")
text(p, "13.500 đ", "18sp", True, "12dp", id="result_total").set(f"{{{A}}}gravity", "end")
text(p, "Trạng thái: Đã nhận", "12sp", top="24dp", id="result_state")
button(p, "Gửi kết quả thu gom", "btn_next", "45dp")
save("collection_result", r)

# 3.14 History
r, p = screen()
label(p, "Tổng khối lượng đã thu gom", "14dp")
text(p, "45 kg", "20sp", True, "8dp", id="history_weight")
label(p, "Tổng tiền giao dịch hoàn tất", "25dp")
text(p, "210.000 đ", "20sp", True, "8dp", id="history_total")
line(p, "16dp")
text(p, "Giao dịch gần đây", "15sp", True, "16dp", id="history_count")
el(p, "LinearLayout", id="history_list", orientation="vertical", layout_marginTop="12dp")
save("history", r)

# 3.15 Admin materials
r, p = screen()
button(p, "Thêm vật liệu", "btn_next", "14dp")
head = el(p, "LinearLayout", orientation="horizontal", layout_marginTop="30dp")
first = text(head, "Vật liệu", "13sp", True)
first.set(f"{{{A}}}layout_width", "0dp")
first.set(f"{{{A}}}layout_weight", "1")
text(head, "Đơn giá tham khảo", "13sp", True).set(f"{{{A}}}layout_width", "wrap_content")
el(p, "LinearLayout", id="admin_list", orientation="vertical", layout_marginTop="18dp")
text(p, "Chọn vật liệu để chỉnh sửa.", "12sp", top="80dp")
save("admin_materials", r)

# 3.16 Add/edit material
r, p = screen()
label(p, "Tên vật liệu", "15dp")
field(p, "Giấy / carton", "material_name")
label(p, "Đơn giá tham khảo (đ/kg)", "20dp")
field(p, "3.000", "material_price", typ="number")
button(p, "Lưu thay đổi", "btn_next", "80dp")
button(p, "Hủy", "btn_cancel", "8dp")
save("admin_edit_material", r)

print("Generated 14 wireframe layouts")
