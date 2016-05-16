package com.kuvh.gjjahs;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kuvh.gjjahs.web.Common;

public class CouncilActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;
    private ArrayAdapter<String> navigationDrawerAdapter;
    private String[] leftSliderData = {"학생회 소개", "알림방", "공약이행현황", "회의록", "홈 메뉴"};
    private WebView webView;
    private Common common;
    private int pos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_council);

        leftDrawerList = (ListView) findViewById(R.id.left_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
        navigationDrawerAdapter=new ArrayAdapter<String>(CouncilActivity.this, android.R.layout.simple_list_item_1, leftSliderData) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(CouncilActivity.this.getResources().getColor(android.R.color.white));
                return textView;
            }
        };
        leftDrawerList.setAdapter(navigationDrawerAdapter);
        leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }


            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        webView = (WebView) findViewById(R.id.webView);
        common = new Common();
        common.webViewInit(this, webView);

        if (savedInstanceState == null) {
            SelectItem(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        common.onActivityResult(requestCode, resultCode, intent);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
                SelectItem(position);
        }
    }

    public void SelectItem(int position) {
        if(position != pos) {
            webView.clearHistory();

            switch (position) {
                case 0:
                    webView.loadUrl(getString(R.string.web_url) + "/council_id?m=1&layout=none");
                    break;
                case 1:
                    webView.loadUrl(getString(R.string.web_url) + "/council_notice?m=1&layout=none");
                    break;
                case 2:
                    webView.loadUrl(getString(R.string.web_url) + "/council_pledge?m=1&layout=none");
                    break;
                case 3:
                    webView.loadUrl(getString(R.string.web_url) + "/council_minutes?m=1&layout=none");
                    break;
                case 4:
                    finish();
                    break;
                default:
                    break;
            }
            leftDrawerList.setItemChecked(position, true);
            pos = position;
        }
        drawerLayout.closeDrawer(leftDrawerList);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_slide_out_bottom);
    }
}
