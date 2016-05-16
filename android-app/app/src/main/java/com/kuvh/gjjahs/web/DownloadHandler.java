package com.kuvh.gjjahs.web;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Toast;

public class DownloadHandler {

    private static final String LOGTAG = "DownloadHandler";
    private static Activity mActivity;

    public static void onDownloadStart(Activity activity, String url, String userAgent,
                                       String contentDisposition, String mimetype) {
        mActivity = activity;
        if (contentDisposition == null
                || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), mimetype);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ResolveInfo info = activity.getPackageManager().resolveActivity(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                ComponentName myName = activity.getComponentName();
                if (!myName.getPackageName().equals(info.activityInfo.packageName)
                        || !myName.getClassName().equals(info.activityInfo.name)) {
                    try {
                        activity.startActivity(intent);
                        return;
                    } catch (ActivityNotFoundException ex) {
                    }
                }
            }
        }
        onDownloadStartNoStream(activity, url, userAgent, contentDisposition, mimetype);
    }

    private static String encodePath(String path) {
        char[] chars = path.toCharArray();
        boolean needed = false;
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|') {
                needed = true;
                break;
            }
        }
        if (!needed) {
            return path;
        }
        StringBuilder sb = new StringBuilder("");
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|') {
                sb.append('%');
                sb.append(Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static void onDownloadStartNoStream(Activity activity, String url, String userAgent,
                                        String contentDisposition, String mimetype) {
        String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            String title;
            String msg;
            if (status.equals(Environment.MEDIA_SHARED)) {
                msg = "SD카드를 쓸 수 없습니다.";
                title = "오류";
            } else {
                msg = "SD카드가 없습니다." + filename;
                title = "오류";
            }
            new AlertDialog.Builder(activity).setTitle(title)
                    .setIcon(android.R.drawable.ic_dialog_alert).setMessage(msg)
                    .setPositiveButton("확인", null).show();
            return;
        }
        WebAddress webAddress;
        try {
            webAddress = new WebAddress(url);
            webAddress.setPath(encodePath(webAddress.getPath()));
        } catch (Exception e) {
            Log.e(LOGTAG, "Exception while trying to parse url '" + url + '\'', e);
            return;
        }
        String addressString = webAddress.toString();
        Uri uri = Uri.parse(addressString);
        final DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(uri);
        } catch (IllegalArgumentException e) {
            Toast.makeText(activity, "다운로드 할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        request.setMimeType(mimetype);
        String location = Environment.DIRECTORY_DOWNLOADS;
        request.setDestinationInExternalPublicDir(location, filename);
        request.allowScanningByMediaScanner();
        request.setDescription(webAddress.getHost());
        String cookies = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("cookie", cookies);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if (mimetype == null) {
            if (TextUtils.isEmpty(addressString)) {
                return;
            }
            new FetchUrlMimeType(activity, request, addressString, cookies, userAgent).start();
        } else {
            final DownloadManager manager = (DownloadManager) activity
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            new Thread("Browser download") {
                @Override
                public void run() {
                    manager.enqueue(request);
                }
            }.start();
        }
        Toast.makeText(activity, "다운로드를 시작하는중...", Toast.LENGTH_SHORT).show();
    }
}
