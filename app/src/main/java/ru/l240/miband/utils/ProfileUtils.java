package ru.l240.miband.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;

import ru.fors.remsmed.R;

public class ProfileUtils {

    public static void setLoginPassword(Context baseContext, String login, String password) {

        SharedPreferences preferences = baseContext.getSharedPreferences("LOGIN", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MedUtils.LOGIN_PREF, login);
        editor.putString(MedUtils.PASS_PREF, password);
        editor.commit();
    }

    public static Bitmap getRoundedImage(Context context, Bitmap icon, int borderSize) {
        Bitmap output = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, (float) borderSize,
                context.getResources().getDisplayMetrics());
        final int cornerSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 80,
                context.getResources().getDisplayMetrics());
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, icon.getWidth(), icon.getHeight());
        final RectF rectF = new RectF(rect);

        // prepare canvas for transfer
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(icon, rect, rect, paint);

        // draw border
        paint.setColor(context.getResources().getColor(R.color.main_color_dark));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) borderSizePx);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        return output;
    }
}
