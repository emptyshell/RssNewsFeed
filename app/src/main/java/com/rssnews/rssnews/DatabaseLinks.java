package com.rssnews.rssnews;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by valentin on 12.12.2017.
 */

public class DatabaseLinks extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static  final String DATABASE_NAME = "database";
    private static final String TABLE_NAME = "rssfeedlinks";
    //rssfeedlinks table content
    private static final String KEY_ID = "id";
    private static final String LINK = "link";


    public DatabaseLinks(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+TABLE_NAME+"( "+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+LINK+" TEXT) ";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean addData (String link) {
        boolean createSuccesful = false;

        ContentValues values = new ContentValues();

        values.put(LINK,link);

        SQLiteDatabase db = this.getWritableDatabase();

        createSuccesful = db.insert(TABLE_NAME, null, values)>0;
        db.close();

        return createSuccesful;
    }

    public int countDB () {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT * FROM "+TABLE_NAME;
        int recordCount = db.rawQuery(sql,null).getCount();
        db.close();

        return  recordCount;
    }

    public List<Link> read() {
        List<Link> linkList = new ArrayList<Link>();

        String sql = "SELECT * FROM rssfeedlinks ORDER BY id ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql,null);

        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                String link = cursor.getString(cursor.getColumnIndex(LINK));

                Link lnk = new Link();
                lnk.id = id;
                lnk.link = link;
                linkList.add(lnk);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return linkList;
    }

    public  Link readSingleRecord (int ID) {
        Link link = new Link();
        String sql = "SELECT * FROM rssfeedlinks WHERE id="+Integer.toString(ID);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.moveToFirst()) {
            link.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID)));
            link.link = cursor.getString(cursor.getColumnIndex(LINK));
        }
        cursor.close();
        db.close();
        return link;
    }

    public boolean delete (int id) {
        boolean deleteSuccesful = false;

        SQLiteDatabase db = this.getWritableDatabase();
        deleteSuccesful = db.delete(TABLE_NAME,"id= '" + id +"'",null)>0;
        db.close();
        return deleteSuccesful;
    }

    public int getID(String link) {
        int id = -1;
        Link link1 = new Link();
        String sql = "SELECT * FROM rssfeedlinks WHERE link LIKE '%"+getDomainName (link)+"%'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.moveToFirst()) {
            link1.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID)));
            link1.link = cursor.getString(cursor.getColumnIndex(LINK));
        }
        cursor.close();
        db.close();
        id = link1.id;
        return id;
    }

    public String getDomainName (String str) {

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

        return DATABASE_VERSION+1;
    }
}
