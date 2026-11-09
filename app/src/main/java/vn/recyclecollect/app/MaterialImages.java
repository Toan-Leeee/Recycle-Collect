package vn.recyclecollect.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;

public final class MaterialImages {
    private MaterialImages() {}

    public static int illustration(String material) {
        if (material == null) return R.drawable.material_mixed;
        String name = material.toLowerCase(java.util.Locale.ROOT);
        if (name.contains("nhựa")) return R.drawable.material_plastic;
        if (name.contains("kim loại")) return R.drawable.material_metal;
        if (name.contains("thủy tinh")) return R.drawable.material_glass;
        if (name.contains("điện tử")) return R.drawable.material_electronics;
        if (name.contains("giấy") || name.contains("carton")) return R.drawable.material_paper;
        return R.drawable.material_mixed;
    }

    public static void showCategory(ImageView view, String material) {
        view.setImageResource(illustration(material));
        view.setContentDescription("Ảnh minh họa loại vật liệu: " + material);
    }

    public static void showMaterial(Context context, ImageView view, String material) {
        String photo = AppData.get(context).materialPhotos.get(material);
        if (photo != null && !photo.isEmpty()) {
            File file = new File(context.getFilesDir(), photo);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                view.setContentDescription("Ảnh vật liệu: " + material);
                return;
            }
        }
        showCategory(view, material);
    }

    public static boolean showRequestPhoto(Context context, ImageView view, Request request) {
        if (request.photoPath == null || request.photoPath.isEmpty()) return false;
        File file = new File(context.getFilesDir(), request.photoPath);
        if (!file.isFile()) return false;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) return false;
        view.setImageBitmap(bitmap);
        view.setContentDescription("Ảnh vật liệu của yêu cầu " + request.id);
        return true;
    }
}
