package com.hornetincorporation.beatroute;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BeetRootLocalDB  extends SQLiteOpenHelper{

    public BeetRootLocalDB(Context context) {
        super(context, Constants.BEETROOT_LOCAL_DB.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + Constants.BEETROOT_LOCAL_DB.TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + Constants.BEETROOT_LOCAL_DB.COL_2 + " TEXT, " + Constants.BEETROOT_LOCAL_DB.COL_3 + " TEXT, " + Constants.BEETROOT_LOCAL_DB.COL_4 + " TEXT, " + Constants.BEETROOT_LOCAL_DB.COL_5 + " TEXT, " + Constants.BEETROOT_LOCAL_DB.COL_6 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.BEETROOT_LOCAL_DB.TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String UserID, String UserName, String Location, String DTime, String PhotoURL) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_2,UserID);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_3,UserName);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_4,Location);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_5,DTime);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_6,PhotoURL);
        long result = db.insert(Constants.BEETROOT_LOCAL_DB.TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + Constants.BEETROOT_LOCAL_DB.TABLE_NAME,null);
        return res;
    }

    public Cursor checkTableExists() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor restab = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='" + Constants.BEETROOT_LOCAL_DB.TABLE_NAME + "'",null);
        return restab;
    }

    public Integer deleteAllData () {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Constants.BEETROOT_LOCAL_DB.TABLE_NAME, "1", null);
    }

    public boolean updateData(String id, String UserID, String UserName, String Location, String DTime, String PhotoURL) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_1,id);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_2,UserID);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_3,UserName);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_4,Location);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_5,DTime);
        contentValues.put(Constants.BEETROOT_LOCAL_DB.COL_6,PhotoURL);
        db.update(Constants.BEETROOT_LOCAL_DB.TABLE_NAME, contentValues, "ID = ?", new String[] { id });
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Constants.BEETROOT_LOCAL_DB.TABLE_NAME, "ID = ?",new String[] {id});
    }
}