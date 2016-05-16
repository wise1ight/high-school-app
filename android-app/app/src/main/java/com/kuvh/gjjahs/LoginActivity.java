package com.kuvh.gjjahs;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends ActionBarActivity {
    TextView form_id, form_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_login);

        form_id = (TextView) findViewById(R.id.LoginIDForm);
        form_password = (TextView) findViewById(R.id.LoginPasswordForm);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        form_id.setText(prefs.getString("user_id", ""));

        findViewById(R.id.LoginButton).setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                String user_id = form_id.getText().toString();
                String password = form_password.getText().toString();

                if (user_id.length() > 0 && password.length() > 0)
                    new LoginTask().execute(user_id, password);
                else Toast.makeText(LoginActivity.this, "ID 또는 비밀번호를 입력해 주세요", Toast.LENGTH_LONG).show();
            }
        });
    }

    private class LoginTask extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("로그인 하는 중...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... args) {
            String loginResult = MainApplication.procLoginDo(LoginActivity.this, args[0], args[1]);
            return loginResult;
        }
        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            if (result == "success"){
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("user_id", form_id.getText().toString());
                editor.putString("password", form_password.getText().toString());
                editor.commit();
                finish();
            } else if (result == "network"){
                Toast.makeText(LoginActivity.this, "네트워크 상태를 확인해 주세요", Toast.LENGTH_LONG).show();
            } else if (result == "invalidID"){
                Toast.makeText(LoginActivity.this, "존재하지 않는 회원 아이디입니다", Toast.LENGTH_LONG).show();
            } else if (result == "invalidPassword"){
                Toast.makeText(LoginActivity.this, "잘못된 비밀번호입니다", Toast.LENGTH_LONG).show();
            } else if (result == "ban"){
                Toast.makeText(LoginActivity.this, "차단당한 사용자 입니다", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "로그인 할 수 없습니다. 네트워크 상태를 확인해 주세요.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_slide_out_bottom);
    }
}
