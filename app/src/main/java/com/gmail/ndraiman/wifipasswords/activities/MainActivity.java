package com.gmail.ndraiman.wifipasswords.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.ndraiman.wifipasswords.R;
import com.gmail.ndraiman.wifipasswords.extras.L;
import com.gmail.ndraiman.wifipasswords.extras.MyApplication;
import com.gmail.ndraiman.wifipasswords.extras.TaskLoadWifiEntries;
import com.gmail.ndraiman.wifipasswords.extras.WifiListLoadedListener;
import com.gmail.ndraiman.wifipasswords.pojo.WifiEntry;
import com.gmail.ndraiman.wifipasswords.recycler.RecyclerTouchListener;
import com.gmail.ndraiman.wifipasswords.recycler.WifiListAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements WifiListLoadedListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_WIFI_ENTRIES = "state_wifi_entries";
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private WifiListAdapter mAdapter;
    private ArrayList<WifiEntry> mListWifi = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static TextView textNoData; //TODO implement in a different way

    //is this App being started for the very first time?
    private boolean mFromSavedInstanceState;

    //TODO add swipe to refresh functionality


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        textNoData = (TextView) findViewById(R.id.text_no_data);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeWifiList);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.wifiList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //TODO implement in a different way
        //Setting RecyclerView Design - Divider
//        RecyclerView.ItemDecoration itemDecoration = new
//                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
//        mRecyclerView.addItemDecoration(itemDecoration);

        //Setting RecyclerView Adapter
        mAdapter = new WifiListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        //Setting RecyclerTouchListener
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(MainActivity.this, "onClick " + position, Toast.LENGTH_SHORT).show();  //placeholder
            }

            @Override
            public void onLongClick(View view, int position) {
                Toast.makeText(MainActivity.this, "onLongClick " + position, Toast.LENGTH_SHORT).show(); //placeholder
                //TODO open overlay fragment with options:
                //copy title & pass
                //copy title
                //copy pass
                //hide
            }
        }));


        //getEntries will remove textNoData from layout (GONE)
        textNoData.setText("Getting Root Permission...");

        if (savedInstanceState != null) {
            L.m("extracting mListWifi from Parcelable");
            //if this fragment starts after a rotation or configuration change, load the existing movies from a parcelable
            mListWifi = savedInstanceState.getParcelableArrayList(STATE_WIFI_ENTRIES);

        } else {
            //if this fragment starts for the first time, load the list of movies from a database
            mListWifi = MyApplication.getWritableDatabase().getAllWifiEntries(false);
            //if the database is empty, trigger an AsycnTask to download movie list from the web
            if (mListWifi.isEmpty()) {
                L.m("executing task from onCreate");
                new TaskLoadWifiEntries(this).execute();
            }
        }

        mAdapter.setWifiList(mListWifi);
    }
    //TODO Implement "Hidden" table.

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save the wifi list to a parcelable prior to rotation or configuration change
        outState.putParcelableArrayList(STATE_WIFI_ENTRIES, mListWifi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;

        } else if (id == R.id.action_refresh_from_file) {
            new TaskLoadWifiEntries(this).execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
//        L.T(this, "Destroying Database");
//        MyApplication.getWritableDatabase().deleteAll(false);
        super.onDestroy();
    }

    @Override
    public void onWifiListLoaded(ArrayList<WifiEntry> listWifi) {

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        L.m("onWifiListLoaded");
        mAdapter.setWifiList(listWifi);
    }

    //Swipe to Refresh
    @Override
    public void onRefresh() {
        L.t(this, "onRefresh");
        //load the whole feed again on refresh.
        new TaskLoadWifiEntries(this).execute();
    }
}