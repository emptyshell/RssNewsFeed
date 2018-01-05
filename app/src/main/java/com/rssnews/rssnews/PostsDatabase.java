package com.rssnews.rssnews;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by valentin on 13.12.2017.
 */

public class PostsDatabase extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 3;
    private static  final String DATABASE_NAME = "postDB";
    private static final int tablesCount = 0;
    public static String[] tablesName;
    //rssfeedlinks table content
    private static final String KEY_ID = "id";
    private static final String TITLE = "titlu";
    private static final String LINK = "link";
    private static final String DESC = "description";


    public PostsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //context.deleteDatabase(DATABASE_NAME);
    }

    public PostsDatabase(Context context,boolean reset) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if(reset) context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(int i = 0; i<tablesName.length; i++) {
            String create = "CREATE TABLE IF NOT EXISTS "+getDomainName(tablesName[i])+" ("+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+TITLE+" TEXT, "+LINK+" TEXT, "+DESC+" TEXT)";
            Log.i("TABLE","Created: "+Integer.toString(i));
            db.execSQL(create);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i = 0; i<tablesName.length; i++) {
            db.execSQL(" DROP TABLE IF EXISTS " + getDomainName(tablesName[i]));
            onCreate(db);
        }
    }

    public boolean addData (RssFeedModel feed, String TABLE_NAME) {
        boolean createSuccesful = false;

        ContentValues values = new ContentValues();

        values.put(TITLE,feed.title);
        values.put(LINK,feed.link);
        values.put(DESC,feed.description);

        SQLiteDatabase db = this.getWritableDatabase();

        createSuccesful = db.insert(getDomainName(TABLE_NAME), null, values)>0;
        db.close();

        return createSuccesful;
    }

    public int countDB (String TABLE_NAME) {
        SQLiteDatabase db = this.getWritableDatabase();
        int recordCount = 0;
        if (TABLE_NAME != null) {
            String sql = "SELECT * FROM "+getDomainName(TABLE_NAME);
            recordCount = db.rawQuery(sql,null).getCount();
        }

        db.close();

        return  recordCount;
    }

    public List<RssFeedModel> read(String TABLE_NAME) {
        List<RssFeedModel> feedList = new ArrayList<RssFeedModel>();

        String sql = "SELECT * FROM "+getDomainName(TABLE_NAME)+" ORDER BY id ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql,null);

        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                String title = cursor.getString(cursor.getColumnIndex(TITLE));
                String link = cursor.getString(cursor.getColumnIndex(LINK));
                String desc = cursor.getString(cursor.getColumnIndex(DESC));

                RssFeedModel feed = new RssFeedModel();
                feed.id = id;
                feed.title = title;
                feed.link = link;
                feed.description = desc;
                feedList.add(feed);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return feedList;
    }

    public boolean delete (int id, String TABLE_NAME) {
        boolean deleteSuccesful = false;

        SQLiteDatabase db = this.getWritableDatabase();
        deleteSuccesful = db.delete(getDomainName(TABLE_NAME),"id= '" + id +"'",null)>0;
        db.close();
        return deleteSuccesful;
    }

    public static String getDomainName(String str) {
        if (str != null) {
            if(!str.startsWith("http://www.")) {
                str = str.replace("http://","http://www.");
            }
            if (!str.startsWith("https://www.")) {
                str = str.replace("https://","https://www.");
            }
            String pre,post;
            pre = str.substring(0,str.indexOf(".")+1);
            //Log.d("PRE",pre);
            str = str.replace(pre, "");
            //Log.d("STR",str);
            post = str.substring(str.indexOf("."),str.length());
            //Log.d("POST",post);
            str = str.replace(post,"");
            //Log.d("STR",str);
            return str;
        }
        return null;
    }

    public int updateDatabaseVersion() {
        //MainActivity.setDatabaseVersionFromFile(DATABASE_VERSION+1);
        return DATABASE_VERSION+1;
    }

    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    public void getDatabaseVersion(int version) {
        DATABASE_VERSION = version;
        return;
    }
}
