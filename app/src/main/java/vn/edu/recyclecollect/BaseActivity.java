package vn.edu.recyclecollect;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.view_screen_chrome);
        FrameLayout host = findViewById(R.id.screen_content);
        LayoutInflater.from(this).inflate(layoutResID, host, true);

        boolean authentication = this instanceof LoginActivity || this instanceof RegisterActivity;
        ImageButton back = findViewById(R.id.screen_back);
        back.setVisibility(
                this instanceof LoginActivity || this instanceof HomeActivity
                        ? View.INVISIBLE
                        : View.VISIBLE);
        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        ((TextView) findViewById(R.id.screen_title)).setText(screenTitle());
        ImageButton more = findViewById(R.id.screen_more);
        more.setVisibility(authentication || this instanceof HomeActivity ? View.INVISIBLE : View.VISIBLE);
        more.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, more);
            menu.getMenu()
                    .add("Giới thiệu bản giao diện")
                    .setOnMenuItemClickListener(item -> {
                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                                .setTitle("Thu gom tái chế")
                                .setMessage("Bản giao diện minh họa cho bài tập lớn Android.")
                                .setPositiveButton("Đóng", null)
                                .show();
                        return true;
                    });
            if (!authentication)
                menu.getMenu()
                        .add("Đăng xuất")
                        .setOnMenuItemClickListener(item -> {
                            AppData.get(this).email = "";
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            return true;
                        });
            menu.show();
        });
        BottomNavigationView nav = findViewById(R.id.screen_navigation);
        if (authentication) {
            nav.setVisibility(View.GONE);
            return;
        }
        String role = AppData.get(this).role;
        nav.getMenu().add(0, 1, 0, "Trang chủ").setIcon(R.drawable.ic_home_nav);
        nav.getMenu()
                .add(
                        0,
                        2,
                        1,
                        role.equals("admin")
                                ? "Vật liệu"
                                : role.equals("collector") ? "Đơn nhận" : "Yêu cầu")
                .setIcon(R.drawable.ic_requests_nav);
        if (!role.equals("admin"))
            nav.getMenu()
                    .add(0, 3, 2, "Lịch sử")
                    .setIcon(R.drawable.ic_history_nav);
        nav.getMenu().add(0, 4, 3, "Hồ sơ").setIcon(R.drawable.ic_profile_nav);
        nav.setLabelVisibilityMode(
                com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED);
        nav.setItemIconSize((int) (22 * getResources().getDisplayMetrics().density));
        nav.setItemActiveIndicatorEnabled(false);
        android.content.res.ColorStateList navColors = new android.content.res.ColorStateList(
                new int[][] {new int[] {android.R.attr.state_checked}, new int[] {}},
                new int[] {getColor(R.color.green), getColor(R.color.muted)});
        nav.setItemIconTintList(navColors);
        nav.setItemTextColor(navColors);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> target =
                    id == 1
                            ? HomeActivity.class
                            : id == 2
                                    ? (role.equals("admin")
                                            ? AdminMaterialsActivity.class
                                            : MyRequestsActivity.class)
                                    : id == 3 ? HistoryActivity.class : ProfileActivity.class;
            if (!target.isInstance(this)) open(target);
            return true;
        });
        int active =
                this instanceof ProfileActivity
                        ? 4
                        : this instanceof HistoryActivity
                                ? 3
                                : this instanceof AdminMaterialsActivity
                                                || this instanceof AdminEditMaterialActivity
                                                || this instanceof MyRequestsActivity
                                                || this instanceof RequestDetailActivity
                                                || this instanceof ChatActivity
                                                || this instanceof CollectionResultActivity
                                                || this instanceof CreateRequestActivity
                                        ? 2
                                        : 1;
        nav.getMenu().findItem(active).setChecked(true);
    }

    private String screenTitle() {
        if (this instanceof LoginActivity) return "Đăng nhập";
        if (this instanceof RegisterActivity) return "Đăng ký";
        if (this instanceof HomeActivity) return "Trang chủ";
        if (this instanceof ProfileActivity) return "Hồ sơ cá nhân";
        if (this instanceof MaterialsActivity) return "Danh mục và bảng giá";
        if (this instanceof CreateRequestActivity) return "Tạo yêu cầu thu gom";
        if (this instanceof MyRequestsActivity) return "Yêu cầu của tôi";
        if (this instanceof RequestDetailActivity) return "Chi tiết yêu cầu";
        if (this instanceof ExploreRequestsActivity) return "Yêu cầu chờ nhận";
        if (this instanceof MapActivity) return "Bản đồ yêu cầu";
        if (this instanceof ChatActivity) return "Tin nhắn";
        if (this instanceof CollectionResultActivity) return "Kết quả thu gom";
        if (this instanceof HistoryActivity) return "Lịch sử và thống kê";
        if (this instanceof AdminMaterialsActivity) return "Quản lý vật liệu";
        return "Thêm / sửa vật liệu";
    }

    protected void open(Class<?> screen) {
        startActivity(new Intent(this, screen));
    }

    protected void next(int id, Class<?> screen) {
        View view = findViewById(id);
        if (view != null) view.setOnClickListener(v -> open(screen));
    }
}
