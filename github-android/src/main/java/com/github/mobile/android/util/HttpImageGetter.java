package com.github.mobile.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import java.io.File;
import java.io.IOException;

import roboguice.util.RoboAsyncTask;

/**
 * Getter for an image
 */
public class HttpImageGetter implements ImageGetter {

    private final Context context;

    private final File dir;

    private final int width;

    /**
     * Create image getter for context
     *
     * @param context
     */
    public HttpImageGetter(Context context) {
        this.context = context;
        dir = context.getCacheDir();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        width = point.x;
    }

    /**
     * Bind text view to HTML string
     *
     * @param view
     * @param html
     * @return this image getter
     */
    public HttpImageGetter bind(final TextView view, final String html) {
        view.setText(Html.encode(html));
        new RoboAsyncTask<CharSequence>(context) {

            public CharSequence call() throws Exception {
                return Html.encode(html, HttpImageGetter.this);
            }

            protected void onSuccess(CharSequence html) throws Exception {
                view.setText(html);
            }
        }.execute();
        return this;
    }

    public Drawable getDrawable(String source) {
        File output = null;
        try {
            output = File.createTempFile("image", ".jpg", dir);
            synchronized (this) {
                HttpRequest request = HttpRequest.get(source);
                if (!request.ok())
                    return null;
                request.receive(output).disconnect();
            }
            Bitmap bitmap = Image.getBitmap(output, width, Integer.MAX_VALUE);
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            return drawable;
        } catch (IOException e) {
            return null;
        } catch (HttpRequestException e) {
            return null;
        } finally {
            if (output != null)
                output.delete();
        }
    }

}