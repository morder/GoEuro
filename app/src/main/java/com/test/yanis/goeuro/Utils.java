package com.test.yanis.goeuro;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yanis on 6/10/15.
 */
public class Utils {

    public static boolean simCardExist(Application application){
        TelephonyManager telMgr = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_READY:
                return true;
            default:
                return false;
        }
    }

    public static int hash(int h) {
        return hash((long)h);
    }

    public static int hash(long h) {
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return (int) (h ^ (h >>> 16));
    }

    public static void hideSoftKeyboard(View view, Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager)  context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_HIDDEN);
        inputMethodManager.hideSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.RESULT_HIDDEN);
    }

    public static void hideSoftKeyboard2(Activity activity) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null && inputManager != null) {
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                inputManager.hideSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public static void hideSoftKeyboard2(View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null){
            View focus = activity.getCurrentFocus();
            if (focus != null){
                InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.RESULT_HIDDEN);
            }
        }
    }

    public static boolean isKeyboardOpened(Activity activity){
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            //return imm.isAcceptingText();
            return imm.isActive();
        }
        return false;
    }

    public static int dpToPx(int dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static byte[] makeDarooRequest(String urlString, RequestParams params){
        return makeDarooRequest(urlString, params.getMap());
    }

    public static byte[] makeDarooRequest(String urlString, Map<String, String> params){
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlString);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setReadTimeout(60000);
            urlConnection.setConnectTimeout(60000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);

            Uri.Builder builder = new Uri.Builder();
            for (Map.Entry<String, String> entry: params.entrySet()){
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
            String query = builder.build().getEncodedQuery();

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            urlConnection.connect();
            int status = urlConnection.getResponseCode();
            if (status != 200){
                return null;
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            byte[] bytes = new byte[1000];
            ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
            int numRead;
            while ((numRead = in.read(bytes, 0, 1000)) >= 0) {
                out_stream.write(bytes, 0, numRead);
            }

            return out_stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    public static class RequestParams{
        private HashMap<String, String> mMap = new HashMap<>();
        public RequestParams add(String ket, String value){
            mMap.put(ket, value);
            return this;
        }

        public Map<String, String> getMap(){
            return mMap;
        }
    }

    public static RequestParams makeRequestParams(){
        return new RequestParams();
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int getDiffYears(Calendar first, Calendar last) {
        Calendar a = first;
        Calendar b = last;
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    public static boolean isPhone(String phone){
        return Patterns.PHONE.matcher(phone).matches();
    }

    public static int getScreenWidth(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        return width;
    }

    public static String convert(long x) {
        return new BigInteger(1, new byte[] { (byte) (x >> 56),
                (byte) (x >> 48), (byte) (x >> 40), (byte) (x >> 32),
                (byte) (x >> 24), (byte) (x >> 16), (byte) (x >> 8),
                (byte) (x >> 0) }).toString();
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getCropSquaredBitmap(Bitmap bitmap){
        int dimension;
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            dimension = bitmap.getHeight();
        } else {
            dimension = bitmap.getWidth();
        }
        return ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
    }

    private static SimpleDateFormat sdf1 = new SimpleDateFormat("dd LLLL", Locale.US);

    public static String formatDate_short(Calendar calendar){
        return sdf1.format(calendar.getTime());
    }

    public static void openBrowser(Context context, String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static long getTimeUTC(){
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        return ((int)(now / 1000));
    }

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static Uri getDevicePhoto(Application application){
        Cursor c = application.getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI, new String[]{ContactsContract.Contacts.PHOTO_URI}, null, null, null
        );
        if (c != null) {
            int count = c.getCount();
            c.moveToFirst();
            int position = c.getPosition();
            try {
                if (count == 1 && position == 0) {
                    String path = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                    if (path != null) {
                        return Uri.parse(path);
                    }
                }
            } finally {
                c.close();
            }
        }
        return null;
    }

    public static String calculateMD5(String string) {
        InputStream stream = new ByteArrayInputStream(string.getBytes(Charset.forName("utf-8")));
        return calculateMD5(stream);
    }

    public static String calculateMD5(InputStream is) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e("Utils", "Exception while getting digest", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("Utils", "Exception on closing MD5 input stream", e);
            }
        }
    }

    @Nullable
    public static String calculateMD5(File updateFile) {
        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e("Utils", "Exception while getting FileInputStream", e);
            return null;
        }

        return calculateMD5(is);
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int maxSize) {
        if (bm.getWidth() <= maxSize && bm.getHeight() <= maxSize){
            return bm;
        }
        int outWidth;
        int outHeight;
        int inWidth = bm.getWidth();
        int inHeight = bm.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
    }

    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        return os.toByteArray();
    }

    public static <T> T unserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(is);
        return (T) oin.readObject();
    }
}
