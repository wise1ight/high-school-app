package com.kuvh.gjjahs;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;

public class DietActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mEmpty;
    private String mTime = "2";
    private int mYear, mMonth, mDay;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.diet_list);
        mTitle = (TextView) findViewById(R.id.diet_title);
        mSubtitle = (TextView) findViewById(R.id.diet_subtitle);
        mEmptyView = (LinearLayout) findViewById(R.id.emptyView);
        mEmpty = (TextView) findViewById(R.id.emptyText);

        final Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DATE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        updateDiet();
    }

    private void updateDiet() {
        String title = String.format("%04d.%02d.%02d", mYear, mMonth + 1, mDay);
        mTitle.setText(title);

        if(mTime == "2")
            mSubtitle.setText("점심");
        else if(mTime == "3")
            mSubtitle.setText("저녁");

        progressDialog = ProgressDialog.show(this, "", "급식표를 가져오는 중...");
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    String str = NeisAPI.getDiet(DietActivity.this, mTime, String.format("%04d.%02d.%02d", mYear, mMonth + 1, mDay));
                    Bundle bundle = new Bundle();
                    bundle.putString("data", str);
                    msg.setData(bundle);
                    messageHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Bundle bundle = new Bundle();
                    bundle.putString("data", " ");
                    msg.setData(bundle);
                    messageHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    private Handler messageHandler = new Handler() {

        public void handleMessage(Message msg) {
            DietAdapter mAdapter;

            progressDialog.dismiss();
            String data = msg.getData().getString("data");
            try {
                if (!data.equals(" ")) {
                    DietItem arraydata[] = new DietItem[data.split(",").length];
                    for (int j = 0; j < data.split(",").length; j++) {
                        arraydata[j] = new DietItem(data.split(",")[j], R.drawable.ic_drawer);
                    }
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    mAdapter = new DietAdapter(arraydata);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                    mAdapter = new DietAdapter(new DietItem[]{});
                }
            } catch (Exception e) {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                mAdapter = new DietAdapter(new DietItem[]{});
            }
            mRecyclerView.setAdapter(mAdapter);
        }
    };

    public class DietItem {
        private String title;
        private int imageUrl;

        public DietItem(String title,int imageUrl){

            this.title = title;
            this.imageUrl = imageUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(int imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public class DietAdapter extends RecyclerView.Adapter<DietAdapter.ViewHolder> {
        private DietItem[] itemsData;

        public DietAdapter(DietItem[] itemsData) {
            this.itemsData = itemsData;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public DietAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View itemLayoutView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.diet_item, null);

            ViewHolder viewHolder = new ViewHolder(itemLayoutView);
            return viewHolder;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.txtViewTitle.setText(itemsData[position].getTitle());
            viewHolder.imgViewIcon.setImageResource(itemsData[position].getImageUrl());
        }

        // inner class to hold a reference to each item of RecyclerView
        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView txtViewTitle;
            public ImageView imgViewIcon;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.dietName);
                imgViewIcon = (ImageView) itemLayoutView.findViewById(R.id.countryImage);
            }
        }


        // Return the size of your itemsData (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return itemsData.length;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_diet, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_date_picker:
                DatePickerDialog datePickerDialog = new DatePickerDialog(DietActivity.this, onDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.show();
                return true;
            case R.id.action_refresh:
                updateDiet();
                return true;
            case R.id.select_lunch:
                item.setChecked(true);
                mTime = "2";
                updateDiet();
                return true;
            case R.id.select_dinner:
                item.setChecked(true);
                mTime = "3";
                updateDiet();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDiet();
                }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_slide_out_bottom);
    }

}
