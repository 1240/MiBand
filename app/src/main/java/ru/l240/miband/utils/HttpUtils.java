package ru.l240.miband.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import retrofit2.Response;


/**
 * @author Alexander Popov created on 15.09.2015.
 */
public class HttpUtils {

    private CookieManager mCookieManager;
    static final String COOKIES_HEADER = "Set-Cookie";

    public String login(String path) throws IOException {
        BufferedReader reader = null;
        mCookieManager = new CookieManager();
        try {
            URL url = new URL(path);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setReadTimeout(10000);
            c.setRequestProperty("Content-Type",
                    "application/json");
            c.connect();

            Map<String, List<String>> headerFields = c.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    mCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder buf = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                buf.append(line + "\n");
            }
            return (buf.toString());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public String getMethod(String path) throws IOException {
        BufferedReader reader = null;
        try {
            URL url = new URL(path);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setReadTimeout(10000);
            c.setRequestProperty("Content-Type",
                    "application/json");


            if (mCookieManager.getCookieStore().getCookies().size() > 0) {
                c.setRequestProperty("Cookie",
                        TextUtils.join(";", mCookieManager.getCookieStore().getCookies()));
            }

            c.connect();
            reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder buf = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                buf.append(line + "\n");
            }
            return (buf.toString());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public String postMethod(String path, String json) throws IOException {
        BufferedReader reader = null;
        try {
            URL url = new URL(path);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setReadTimeout(10000);
            c.setRequestProperty("Content-Type",
                    "application/json");
            if (mCookieManager.getCookieStore().getCookies().size() > 0) {
                c.setRequestProperty("Cookie",
                        TextUtils.join(";", mCookieManager.getCookieStore().getCookies()));
            }


            c.connect();

            if (json != null) {
                OutputStream outputStream = c.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
                osw.write(json);
                osw.flush();
                osw.close();
            }
            if (c.getResponseCode() != 400) {
                reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(c.getErrorStream()));
            }
            StringBuilder buf = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                buf.append(line + "\n");
            }
            return (buf.toString());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public Bitmap getImage(String path) {
        HttpURLConnection c = null;
        try {
            URL url = new URL(path);
            c = (HttpURLConnection) url.openConnection();
            c.setReadTimeout(10000);
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-Type",
                    "application/json");

            if (mCookieManager.getCookieStore().getCookies().size() > 0) {
                //While joining the Cookies, use ',' or ';' as needed. Most of the server are using ';'
                c.setRequestProperty("Cookie",
                        TextUtils.join(";", mCookieManager.getCookieStore().getCookies()));
            }
            c.connect();
            int responseCode = c.getResponseCode();
            if (responseCode == 200) {
                return BitmapFactory.decodeStream(c.getInputStream());
            } else
                return null;
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }


    public Bitmap getImage(String path, String cookie) {
        HttpURLConnection c = null;
        try {
            URL url = new URL(path);
            c = (HttpURLConnection) url.openConnection();
            c.setReadTimeout(10000);
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-Type",
                    "application/json");
            c.setRequestProperty("Cookie", cookie);
            c.connect();
            int responseCode = c.getResponseCode();
            if (responseCode == 200) {
                return BitmapFactory.decodeStream(c.getInputStream());
            } else
                return null;
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }

    private static String responseToString(Response response) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(response.raw().body().byteStream()));

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        return sb.toString();
    }

    private static String errorResponseToString(Response response) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(response.errorBody().byteStream()));

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        return sb.toString();
    }

}
