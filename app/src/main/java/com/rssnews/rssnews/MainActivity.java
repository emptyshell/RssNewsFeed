package com.rssnews.rssnews;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mFeedTitleTextView;
    private TextView mFeedLinkTextView;
    private TextView mFeedDescriptionTextView;

    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;
    private String currentUrl;
    private static int MAX_POST_LIMIT = 100;
    private RssFeedModelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu m = navigationView.getMenu();
        SubMenu menuGroup = m.addSubMenu("Current RSS Links");
        final List<Link> linkList = new DatabaseLinks(this).read();
        String[] names = new String[linkList.size()];
        if(linkList.size() > 0) {
            int i = 0;
            for (Link obj : linkList) {
                menuGroup.add(Menu.NONE,obj.id,Menu.NONE,obj.link);
                names[i] = obj.link;
                i++;
            }
        }

        PostsDatabase.tablesName = names;
        navigationView.setNavigationItemSelectedListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mFeedTitleTextView = (TextView) findViewById(R.id.feedTitle);
        mFeedDescriptionTextView = (TextView) findViewById(R.id.feedDescription);
        mFeedLinkTextView = (TextView) findViewById(R.id.feedLink);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if(linkList.size() > 0) currentUrl = names[0];
        new FetchFeedTask().execute((Void) null);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        final List<Link> linkList = new DatabaseLinks(this).read();
        for (Link obj : linkList) {
            if(obj.id == id) {
                currentUrl = item.getTitle().toString();
                new FetchFeedTask().execute((Void) null);
            }
        }


        if (id == R.id.link_manager) {
            Intent intent = new Intent(MainActivity.this, LinkManager.class);
            startActivity(intent);
        } else if (id == R.id.post_manager) {
            Intent intent = new Intent(MainActivity.this, PostManager.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MainActivity", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                }


                if (isNetworkAvailable()) {
                    List<RssFeedModel> db1 = new PostsDatabase(MainActivity.this).read(currentUrl);
                    for (RssFeedModel obj : db1) {
                        if (items.size() > 0) {
                            boolean deleteStatus = new PostsDatabase(MainActivity.this).delete(obj.id, currentUrl);
                            if (deleteStatus) {
                                Log.i("postInfo", "deleted succesfully");
                            } else {
                                Log.i("postInfo", "deleted unsuccesfully");
                            }
                        } else {
                            break;
                        }
                    }
                }

                if (title != null && link != null && description != null) {
                    if(isItem) {
                        PostsDatabase db = new PostsDatabase(MainActivity.this);
                        if (db.countDB(currentUrl) < MAX_POST_LIMIT) {
                            RssFeedModel item = new RssFeedModel(title, link, description);
                            db.addData(item,currentUrl);
                        }

                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = description;
                    }

                    title = null;
                    link = null;
                    description = null;
                    isItem = false;
                }
            }
            List<RssFeedModel> tmp = new PostsDatabase(this).read(currentUrl);
            int i = MAX_POST_LIMIT;
            for(RssFeedModel obj : tmp) {
                if (i!=0) {
                    items.add(obj);
                    i--;
                }
                else break;
            }
            return items;
        } finally {
            inputStream.close();
        }
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            mFeedTitle = null;
            mFeedLink = null;
            mFeedDescription = null;
            mFeedTitleTextView.setText("Feed Title: " + mFeedTitle);
            mFeedDescriptionTextView.setText("Feed Description: " + mFeedDescription);
            mFeedLinkTextView.setText("Feed Link: " + mFeedLink);
            urlLink = currentUrl;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e("ERROR", "Error", e);
            } catch (XmlPullParserException e) {
                Log.e("ERROR", "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {
                mFeedTitleTextView.setText("Feed Title: " + mFeedTitle);
                mFeedTitleTextView.setTextColor(R.color.defaultTextViewColor);
                mFeedDescriptionTextView.setText("Feed Description: " + mFeedDescription);
                mFeedDescriptionTextView.setTextColor(R.color.defaultTextViewColor);
                mFeedLinkTextView.setText("Feed Link: " + mFeedLink);
                mFeedLinkTextView.setTextColor(R.color.defaultTextViewColor);
                // Fill RecyclerView
                adapter = new RssFeedModelAdapter(mFeedModelList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(adapter);
            } else {
                if(!isNetworkAvailable()) {
                    Toast.makeText(MainActivity.this,"No internet connection!", Toast.LENGTH_LONG).show();
                    mFeedTitleTextView.setText("NO INTERNET CONNECTION!");
                    mFeedTitleTextView.setTextColor(Color.RED);
                    mFeedDescriptionTextView.setText("READING FROM DB");
                    mFeedDescriptionTextView.setTextColor(Color.RED);
                    mFeedLinkTextView.setText("CONNECT TO INTERNET TO UPDATE RSS FEED");
                    mFeedLinkTextView.setTextColor(Color.RED);
                }

                Toast.makeText(MainActivity.this,"Enter a valid Rss feed url", Toast.LENGTH_LONG).show();
                List<RssFeedModel> tmp = null;
                if(currentUrl != null)
                    tmp = new PostsDatabase(MainActivity.this).read(currentUrl);
                if(tmp != null){
                    mFeedModelList = new ArrayList<RssFeedModel>();
                    int i = MAX_POST_LIMIT;
                    for(RssFeedModel obj : tmp) {
                        if (i!=0) {
                            mFeedModelList.add(obj);
                            i--;
                        }
                        else break;
                    }
                    adapter = new RssFeedModelAdapter(mFeedModelList);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mRecyclerView.setAdapter(adapter);
                }

            }
        }
    }

    public static void setMaxPostsAmount(int value) {
        MAX_POST_LIMIT = value;
    }
    public static int getMaxPostsAmount() {
        return MAX_POST_LIMIT;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void setMaxPostLimitFromFile(int data) throws IOException {
        try {
            FileOutputStream fou = openFileOutput("limit.txt", MODE_WORLD_READABLE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fou);
            outputStreamWriter.write(Integer.toString(data));
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public int getMaxPostLimitFromFile() throws IOException {
        FileInputStream fis;
        int n;
        fis = openFileInput("limit.txt");
        StringBuffer fileContent = new StringBuffer("");

        byte[] buffer = new byte[1024];

        while ((n = fis.read(buffer)) != -1)
        {
            fileContent.append(new String(buffer, 0, n));
        }
        Log.d("LIMIT", String.valueOf(fileContent));
//string temp contains all the data of the file.
        fis.close();
        return Integer.parseInt(String.valueOf(fileContent));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
