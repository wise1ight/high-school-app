package com.kuvh.gjjahs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.kuvh.gjjahs.web.WebActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends ActionBarActivity {
    // 뒤로가기 버튼
    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //액션바 초기화
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_custom);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("tab_menu").setIndicator(createTabView(R.drawable.tab_menu_icon, "메뉴")),
                TabMenuFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab_my").setIndicator(createTabView(R.drawable.tab_my_icon, "MY 중앙")),
                TabMyFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab_settings").setIndicator(createTabView(R.drawable.tab_settings_icon, "설정")),
                SettingsFragment.class, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment frag : fragments)
                frag.onActivityResult(requestCode, resultCode, data);
        }
    }

    private View createTabView(final int id, final String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.tabs_item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        imageView.setImageDrawable(getResources().getDrawable(id));
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        textView.setText(text);
        return view;
    }

    public static class TabMenuFragment extends Fragment {

        public TabMenuFragment() {
        }

        private SliderLayout mSlider;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);

            //배너 초기화
            mSlider = (SliderLayout) rootView.findViewById(R.id.slider);
            mSlider.setPresetTransformer(SliderLayout.Transformer.Default);
            mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
            mSlider.setDuration(5000);
            new ReadJSON().execute("http://www.gjjungang.hs.kr/api/slider.json");

            //버튼 초기화
            RelativeLayout tmAnnounce;
            tmAnnounce = (RelativeLayout) rootView.findViewById(R.id.tileMenuAnnounce);
            tmAnnounce.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MenuRun(1);
                }
            });

            RelativeLayout tmDiet;
            tmDiet = (RelativeLayout) rootView.findViewById(R.id.tileMenuDiet);
            tmDiet.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MenuRun(2);
                }
            });

            RelativeLayout tmSchedule;
            tmSchedule = (RelativeLayout) rootView.findViewById(R.id.tileMenuSchedule);
            tmSchedule.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MenuRun(3);
                }
            });

            RelativeLayout tmCareer;
            tmCareer = (RelativeLayout) rootView.findViewById(R.id.tileMenuCareer);
            tmCareer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MenuRun(6);
                }
            });

            RelativeLayout tmSingo;
            tmSingo = (RelativeLayout) rootView.findViewById(R.id.tileMenuSingo);
            tmSingo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MenuRun(5);
                }
            });

            RelativeLayout tmSuggestion;
            tmSuggestion = (RelativeLayout) rootView.findViewById(R.id.tileMenuSuggestion);
            tmSuggestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MenuRun(4);
                }
            });

            RelativeLayout tmFreeboard;
            tmFreeboard = (RelativeLayout) rootView.findViewById(R.id.tileMenuFreeboard);
            tmFreeboard.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MenuRun(7);
                }
            });

            RelativeLayout tmClub;
            tmClub = (RelativeLayout) rootView.findViewById(R.id.tileMenuClub);
            tmClub.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MenuRun(8);
                }
            });

            RelativeLayout tmAssociation;
            tmAssociation = (RelativeLayout) rootView.findViewById(R.id.tileMenuAssociation);
            tmAssociation.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MenuRun(9);
                }
            });

            RelativeLayout tmCloud;
            tmCloud = (RelativeLayout) rootView.findViewById(R.id.tileMenuCloud);
            tmCloud.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MenuRun(10);
                }
            });

            return rootView;
        }

        private class ReadJSON extends AsyncTask<String, String, String> {
            protected void onPreExecute() {}

            @Override
            protected String doInBackground(String... urls) {
                HttpClient httpclient = new DefaultHttpClient();
                StringBuilder builder = new StringBuilder();
                HttpPost httppost = new HttpPost(urls[0]);
                try {
                    HttpResponse response = httpclient.execute(httppost);
                    StatusLine statusLine = response.getStatusLine();
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        InputStream content = entity.getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return builder.toString();
            }

            protected void onPostExecute(String result) {
                try{
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i =0 ; i<jsonArray.length();i++) {
                        JSONObject jObject = jsonArray.getJSONObject(i);

                        TextSliderView textSliderView = new TextSliderView(getActivity());
                        textSliderView
                                .description(jObject.getString("description"))
                                .image(jObject.getString("image"))
                                .setScaleType(BaseSliderView.ScaleType.Fit)
                                .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                    @Override
                                    public void onSliderClick(BaseSliderView slider) {
                                        if (!slider.getBundle().get("extra").equals("null")) {
                                            Intent intent = new Intent(getActivity(), WebActivity.class);
                                            intent.putExtra("url", slider.getBundle().get("extra").toString());
                                            startActivity(intent);
                                            getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
                                        }
                                    }
                                });

                        textSliderView.getBundle()
                                .putString("extra", jObject.getString("url"));

                        mSlider.addSlider(textSliderView);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    mSlider.setBackgroundResource(android.R.drawable.ic_dialog_alert);
                }
            }
        }

        private void MenuRun(int function)
        {
            try {
                if (function == 1 || function == 4 || function == 6 || function == 7 || function == 9) {
                    if (MainApplication.member_info_maps.get("회원 그룹").equals("학생") || MainApplication.member_info_maps.get("회원 그룹").equals("선생님") || MainApplication.member_info_maps.get("회원 그룹").equals("학부모"))
                        MenuRunDo(function);
                    else {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
                    }
                } else if (function == 5) {
                    if (MainApplication.member_info_maps.get("회원 그룹").equals("학생") || MainApplication.member_info_maps.get("회원 그룹").equals("선생님") || MainApplication.member_info_maps.get("회원 그룹").equals("학부모"))
                        MenuRunDo(function);
                    else {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
                    }
                } else {
                    MenuRunDo(function);
                }
            } catch (Exception e) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
            }
        }

        private void MenuRunDo(int function)
        {
            Intent intent = null;
            //어쩌피 액티비티를 출력해야 하기 때문에 인텐트를 생성해주자.
            switch (function) {
                case 1:
                    //공지사항을 출력한다.
                    intent = new Intent(getActivity(), WebActivity.class);
                    intent.putExtra("url", getString(R.string.web_url) + "/announce");
                    break;
                case 2:
                    //식단표를 출력한다.
                    intent = new Intent(getActivity(), DietActivity.class);
                    break;
                case 3:
                    //학사일정을 출력한다.
                    intent = new Intent(getActivity(), WebActivity.class);
                    intent.putExtra("url", "http://gjjungang-h.gne.go.kr/m/main.jsp?SCODE=S0000000872&mnu=M001006005");
                    break;
                case 4:
                    //건의함을 출력한다.
                    intent = new Intent(getActivity(), WebActivity.class);
                    intent.putExtra("url", getString(R.string.web_url) + "/suggest");
                    break;
                case 5:
                    //학교폭력신고함을 출력한다.
                    intent = new Intent(getActivity(), WebActivity.class);
                    intent.putExtra("url", getString(R.string.web_url) + "/singo?act=dispBoardWrite");
                    break;
                case 6:
                    //진로진학 게시판을 출력한다.
                    intent = new Intent(getActivity(), WebActivity.class);
                    intent.putExtra("url", getString(R.string.web_url) + "/career");
                    break;
                case 7:
                    //자유게시판을 출력한다.
                    intent = new Intent(getActivity(), WebActivity.class);
                    intent.putExtra("url", getString(R.string.web_url) + "/freeboard");
                    break;
                case 8:
                    //동아리를 출력한다.
                    Toast.makeText(getActivity(), "준비중인 기능입니다.", Toast.LENGTH_SHORT).show();
                    break;
                case 9:
                    //학생회를 출력한다.
                    intent = new Intent(getActivity(), CouncilActivity.class);
                    break;
                case 10:
                    //클라우드를 출력한다.
                    Toast.makeText(getActivity(), "준비중인 기능입니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
            //공통적으로 할 일
            //결정적으로 액티비티를 출력함
            if(function == 8 || function == 10)
            {
                //아무일도 하지 않을까?
            } else {
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
            }
        }
    }

    public static class TabMyFragment extends Fragment {
        TextView mNameView;
        TextView mNicknameView;
        TextView mClassView;
        TextView mTypeView;
        ImageView mBarcodeBitmapView;
        ImageView mProfileView;
        TextView mBarcodeTextView;
        View mBarcodeCardView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_my, container, false);
            mNameView = (TextView) rootView.findViewById(R.id.MemberName);
            mNicknameView = (TextView) rootView.findViewById(R.id.MemberNick);
            mClassView = (TextView) rootView.findViewById(R.id.MemberClass);
            mTypeView = (TextView) rootView.findViewById(R.id.MemberType);
            mProfileView = (ImageView) rootView.findViewById(R.id.ProfileImg);
            mBarcodeBitmapView = (ImageView) rootView.findViewById(R.id.BarcodeView);
            mBarcodeTextView = (TextView) rootView.findViewById(R.id.BarcodeText);
            mBarcodeCardView = rootView.findViewById(R.id.Barcode_Card);

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            Login();
        }

        private void Login() {
            if (MainApplication.mLoginState)
            {
                getActivity().findViewById(R.id.LoginButton).setVisibility(View.GONE);
                getActivity().findViewById(R.id.JoinButton).setVisibility(View.GONE);
                mNameView.setVisibility(View.VISIBLE);
                mNameView.setText(MainApplication.member_info_maps.get("<em>*</em> 이름"));
                mNicknameView.setVisibility(View.VISIBLE);
                mNicknameView.setText(MainApplication.member_info_maps.get("<em>*</em> 닉네임"));
                mTypeView.setText("(" + MainApplication.member_info_maps.get("회원 그룹") + " 사용자)");

                if(MainApplication.member_info_maps.get("회원 그룹").equals("학생"))
                {
                    mClassView.setText(MainApplication.member_info_maps.get(" 학반").split("-")[0] + "학년 " + MainApplication.member_info_maps.get(" 학반").split("-")[1] + "반 " + MainApplication.member_info_maps.get(" 번호") + "번");
                    mClassView.setVisibility(View.VISIBLE);
                } else {
                    mClassView.setVisibility(View.GONE);
                }

                if(MainApplication.member_info_maps.get(" 프로필 사진").equals("&hellip;")) {
                    mProfileView.setImageResource(R.drawable.default_profile);
                } else {
                    String imgsrc = "";
                    Pattern pattern = Pattern.compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>");
                    Matcher matcher = pattern.matcher(MainApplication.member_info_maps.get(" 프로필 사진"));

                    while (matcher.find()) {
                        imgsrc = matcher.group(1);
                    }
                    new LoadImage().execute(imgsrc);
                }

                //바코드 출력 부분
                String mBarcodeData = MainApplication.member_info_maps.get(" 학생증 코드");
                if(MainApplication.member_info_maps.get("회원 그룹").equals("학생") && !mBarcodeData.equals("&hellip;"))
                {
                    mBarcodeCardView.setVisibility(View.VISIBLE);
                    try {
                        Bitmap bitmap = encodeAsBitmap(mBarcodeData, BarcodeFormat.CODE_128, 900, 200);
                        mBarcodeBitmapView.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    mBarcodeTextView.setText(mBarcodeData);
                } else {
                    mBarcodeCardView.setVisibility(View.GONE);
                }

                getActivity().findViewById(R.id.LogoutButton).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.LogoutButton).setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        MainApplication.procLogoutDo(getActivity());
                        Login();
                    }
                });
                getActivity().findViewById(R.id.InfoEditButton).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.InfoEditButton).setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), WebActivity.class);
                        intent.putExtra("url", getString(R.string.web_url_ssl) + "/?act=dispMemberModifyInfo");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
                    }
                });
            } else {
                //뷰 세팅
                mBarcodeCardView.setVisibility(View.GONE);
                mNameView.setVisibility(View.GONE);
                mTypeView.setText("로그인이 필요합니다");
                mNicknameView.setVisibility(View.GONE);
                mClassView.setVisibility(View.GONE);
                mProfileView.setImageResource(R.drawable.default_profile);
                getActivity().findViewById(R.id.LogoutButton).setVisibility(View.GONE);
                getActivity().findViewById(R.id.InfoEditButton).setVisibility(View.GONE);
                getActivity().findViewById(R.id.LoginButton).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.LoginButton).setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
                    }
                });
                getActivity().findViewById(R.id.JoinButton).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.JoinButton).setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), WebActivity.class);
                        intent.putExtra("url", getString(R.string.web_url) + "/?act=dispMemberSignUpForm");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
                    }
                });
            }
        }

        private static final int WHITE = 0x00FFFFFF;
        private static final int BLACK = 0xFF000000;

        Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
            String contentsToEncode = contents;
            if (contentsToEncode == null) {
                return null;
            }
            Map<EncodeHintType, Object> hints = null;
            String encoding = guessAppropriateEncoding(contentsToEncode);
            if (encoding != null) {
                hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
                hints.put(EncodeHintType.CHARACTER_SET, encoding);
            }
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix result;
            try {
                result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
            } catch (IllegalArgumentException iae) {
                // Unsupported format
                return null;
            }
            int width = result.getWidth();
            int height = result.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        }

        ProgressDialog pDialog;
        Bitmap bitmap;

        private class LoadImage extends AsyncTask<String, String, Bitmap> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage("프로필 사진을 가져오는중...");
                pDialog.show();
            }
            protected Bitmap doInBackground(String... args) {
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }
            protected void onPostExecute(Bitmap image) {
                if(image != null){
                    mProfileView.setImageBitmap(image);
                    pDialog.dismiss();
                }else{
                    pDialog.dismiss();
                }
            }
        }

        private static String guessAppropriateEncoding(CharSequence contents) {
            // Very crude at the moment
            for (int i = 0; i < contents.length(); i++) {
                if (contents.charAt(i) > 0xFF) {
                    return "UTF-8";
                }
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("종료")
                .setMessage("정말 종료하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        finish();
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
    }
}