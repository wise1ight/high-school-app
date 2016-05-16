package com.kuvh.gjjahs.web;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.kuvh.gjjahs.R;

/**
 * Created by 현욱 on 2014-12-27.
 */
public class Common {
    private Dialog mDialog;
    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    public void webViewInit(Context context, WebView webView)
    {
        final Context finalContext = context;
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(context.getString(R.string.web_url), "app=1");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new webChromeClient(context));
        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setVerticalScrollbarOverlay(true);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, long contentLength) {
                if(finalContext instanceof Activity) {
                    DownloadHandler.onDownloadStart((Activity)finalContext, url, userAgent, contentDisposition, mimetype);
                }
            }
        });
        webView.requestFocus();
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        webView.setWebViewClient(new webViewClient(context));
        CookieSyncManager.getInstance().startSync();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else {
            mUploadMessage.onReceiveValue(null);
            return;
        }
    }

    public class webViewClient extends WebViewClient {
        private Context mContext;

        public webViewClient(Context context)
        {
            mContext = context;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            ActionBar mActionBar = ((ActionBarActivity) mContext).getSupportActionBar();
            if(url.indexOf("announce") > 0) {
                mActionBar.setTitle("공지사항");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2483c5")));
            } else if(url.indexOf("suggest") > 0) {
                mActionBar.setTitle("건의함");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0D47A1")));
            } else if(url.indexOf("singo") > 0){
                mActionBar.setTitle("학교폭력신고");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ed115f")));
            } else if(url.indexOf("career") > 0) {
                mActionBar.setTitle("진로진학");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0D47A1")));
            } else if(url.indexOf("freeboard") > 0) {
                mActionBar.setTitle("자유게시판");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0D47A1")));
            } else if(url.indexOf("gjjungang-h.gne.go.kr/m/main.jsp?SCODE=S0000000872&mnu=M001006005") > 0) {
                mActionBar.setTitle("학사일정");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0D47A1")));
            } else if(url.indexOf("council_id") > 0) {
                mActionBar.setTitle("학생회 소개");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#71bf44")));
            } else if(url.indexOf("council_notice") > 0) {
                mActionBar.setTitle("알림방");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#71bf44")));
            } else if(url.indexOf("council_pledge") > 0) {
                mActionBar.setTitle("공약이행현황");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#71bf44")));
            } else if(url.indexOf("council_minutes") > 0) {
                mActionBar.setTitle("회의록");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#71bf44")));
            } else {
                mActionBar.setTitle("LET'S GO 중앙");
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0D47A1")));
            }

            if(mDialog == null)
            {
                mDialog = ProgressDialog.show(mContext, null,
                        "잠시만 기다려 주세요");
                mDialog.setCancelable(false);

            } else if(mDialog.isShowing() == false) {
                mDialog = ProgressDialog.show(mContext, null,
                        "잠시만 기다려 주세요");
                mDialog.setCancelable(false);
            }
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
            mDialog.dismiss();
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri.Builder uribuilder = Uri.parse(url).buildUpon();
            uribuilder.appendQueryParameter("m", "1");
            uribuilder.appendQueryParameter("layout", "none");
            view.loadUrl(uribuilder.build().toString());
            return true;
        }

        public void onReceivedError(WebView view, int errorCode, String description,
                                    String failingUrl) {
            switch(errorCode) {
                case ERROR_AUTHENTICATION:               // 서버에서 사용자 인증 실패
                case ERROR_BAD_URL:                            // 잘못된 URL
                case ERROR_CONNECT:                           // 서버로 연결 실패
                case ERROR_FAILED_SSL_HANDSHAKE:     // SSL handshake 수행 실패
                case ERROR_FILE:                                   // 일반 파일 오류
                case ERROR_FILE_NOT_FOUND:                // 파일을 찾을 수 없습니다
                case ERROR_HOST_LOOKUP:            // 서버 또는 프록시 호스트 이름 조회 실패
                case ERROR_IO:                               // 서버에서 읽거나 서버로 쓰기 실패
                case ERROR_PROXY_AUTHENTICATION:    // 프록시에서 사용자 인증 실패
                case ERROR_REDIRECT_LOOP:                // 너무 많은 리디렉션
                case ERROR_TIMEOUT:                          // 연결 시간 초과
                case ERROR_TOO_MANY_REQUESTS:            // 페이지 로드중 너무 많은 요청 발생
                case ERROR_UNKNOWN:                         // 일반 오류
                case ERROR_UNSUPPORTED_AUTH_SCHEME:  // 지원되지 않는 인증 체계
                case ERROR_UNSUPPORTED_SCHEME:
                    view.loadData("", "text/html", "UTF-8");
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("오류");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(mContext instanceof Activity) {
                                ((Activity)mContext).finish();
                            }
                        }
                    });
                    builder.setMessage("네트워크 상태가 원활하지 않습니다. 잠시 후 다시 시도해 주세요.");
                    builder.show();
                    break;
            }
        }
    }

    public class webChromeClient extends WebChromeClient {
        private Context mContext;

        public webChromeClient(Context context)
        {
            mContext = context;
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            if(mContext instanceof Activity) {
                ((Activity)mContext).startActivityForResult(
                        Intent.createChooser(i, "파일 첨부"),
                        FILECHOOSER_RESULTCODE);
            }
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, "", "");
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "", "");
        }

        @Override
        public boolean onJsAlert(final WebView view,final String url, final String message, JsResult result) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            result.confirm();
            return true;
        }
    }
}
