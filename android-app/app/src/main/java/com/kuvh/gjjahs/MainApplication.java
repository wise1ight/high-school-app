package com.kuvh.gjjahs;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Socket;
import java.security.cert.X509Certificate;

public class MainApplication extends Application {
    private static MainApplication instance = new MainApplication();
    private static HttpClient httpClient;
    public static String objectId;
    static InputStream is = null;

    public static boolean mLoginState;
    public static HashMap<String, String> member_info_maps;

    public MainApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "fAQXani3uIVBocnlWCVOwl2jzrkpiSDnt7cCJpVg", "gvDFm3A8QYuiVenSYeZArBIXiV8sdJkIs25abFhW");
        ParseInstallation.getCurrentInstallation().saveInBackground();

        if (ParseInstallation.getCurrentInstallation().getObjectId() == null) {
            ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException arg0) {
                    objectId = ParseInstallation.getCurrentInstallation().getObjectId();
                }
            });
        } else {
            objectId = ParseInstallation.getCurrentInstallation().getObjectId();
        }

        Login();
    }

    public void Login() {
        //로그인을 시도한다.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String user_id = prefs.getString("user_id", "");
        final String password = prefs.getString("password", "");
        if (user_id != "" && password != "") {
            Thread mThread = new Thread() {
                public void run() {
                    procLoginDo(getApplicationContext(), user_id, password);
                }
            };
            mThread.start();
        } else {
            mLoginState = false;
        }
    }

    public static String procLoginDo(Context context, String user_id, String password) {
        mLoginState = false;
        String reqResult = "fail";
        String url = "https://www.gjjungang.hs.kr/?act=procMemberLogin&obid=" + objectId;

        CookieSyncManager.createInstance(context);
        CookieManager.getInstance().removeAllCookie();
        CookieSyncManager.getInstance().startSync();
        CookieManager cookieManager = CookieManager.getInstance();
        httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
        nameValuePairs.add(new BasicNameValuePair("password", password));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (IOException e) {
            reqResult = "network";
            e.printStackTrace();
        }

        String result = "", line;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((line = reader.readLine()) != null)
                result += line;

            if (result.indexOf("dispMemberLogout") > 0) {
                reqResult = "success";
                mLoginState = true;
                List<Cookie> cookies = ((DefaultHttpClient) httpClient).getCookieStore().getCookies();
                if (!cookies.isEmpty()) {
                    for (int i = 0; i < cookies.size(); i++) {
                        String cookieString = cookies.get(i).getName() + "=" + cookies.get(i).getValue();
                        cookieManager.setCookie(url, cookieString);
                    }
                }

                CookieSyncManager.getInstance().sync();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {   }

                getMemberInfo();

            } else if (result.indexOf("존재하지 않는 회원 아이디입니다.") > 0) {
                reqResult = "invalidID";
            } else if (result.indexOf("잘못된 비밀번호입니다.") > 0) {
                reqResult = "invalidPassword";
            } else if (result.indexOf("입력한 아이디의 사용이 중지 되었습니다.") > 0) {
                reqResult = "ban";
            } else {
                reqResult = "fail";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CookieSyncManager.getInstance().stopSync();

        return reqResult;
    }

    public static class SFSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SFSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    private static HttpClient getHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SFSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static void getMemberInfo() {
        HttpGet httpGet = new HttpGet("https://www.gjjungang.hs.kr/?act=dispMemberInfo");

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = "", line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((line = reader.readLine()) != null)
                result += line;

            Pattern nonValidPattern = Pattern.compile("<dt>(.*?)</dt>(.*?)>(.*?)</dd>", Pattern.MULTILINE);
            Matcher matcher = nonValidPattern.matcher(result);
            member_info_maps = new HashMap<String, String>();

            while (matcher.find()) {
                member_info_maps.put(matcher.group(1), matcher.group(3));
            }

            //return mGroupList;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void procLogoutDo(Context context) {
        mLoginState = false;
        final String url = "https://www.gjjungang.hs.kr/?act=dispMemberLogout&obid=" + objectId;

        CookieSyncManager.createInstance(context);
        CookieManager.getInstance().removeAllCookie();
        CookieSyncManager.getInstance().sync();

        Thread mThread = new Thread() {
            public void run() {
                HttpGet httpGet = new HttpGet(url);

                try {
                    httpClient.execute(httpGet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mThread.start();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_id", "");
        editor.putString("password", "");
        editor.commit();
    }
}
