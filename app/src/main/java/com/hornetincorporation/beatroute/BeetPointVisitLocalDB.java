package com.hornetincorporation.beatroute;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BeetPointVisitLocalDB extends SQLiteOpenHelper{

    public BeetPointVisitLocalDB(Context context) {
        super(context, Constants.BEETPOINTVISIT_LOCAL_DB.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + Constants.BEETPOINTVISIT_LOCAL_DB.COL_2 + " TEXT, " + Constants.BEETPOINTVISIT_LOCAL_DB.COL_3 + " TEXT, " + Constants.BEETPOINTVISIT_LOCAL_DB.COL_4 + " TEXT, " + Constants.BEETPOINTVISIT_LOCAL_DB.COL_5 + " TEXT, " + Constants.BEETPOINTVISIT_LOCAL_DB.COL_6 + " TEXT, " + Constants.BEETPOINTVISIT_LOCAL_DB.COL_7 + " TEXT, " + Constants.BEETPOINTVISIT_LOCAL_DB.COL_8 + " TEXT, " + Constants.BEETPOINTVISIT_LOCAL_DB.COL_9 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String UserID, String BPVTransition, String BPVBeetPointID, String BPVBeetRouteNPoint, String BPVLocation, String BPVPoint, String BPVRoute, String BPVDTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_2,UserID);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_3,BPVTransition);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_4,BPVBeetPointID);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_5,BPVBeetRouteNPoint);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_6,BPVLocation);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_7,BPVPoint);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_8,BPVRoute);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_9,BPVDTime);
        long result = db.insert(Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME,null);
        return res;
    }

    public Cursor checkTableExists() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor restab = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='" + Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME + "'",null);
        return restab;
    }

    public Integer deleteAllData () {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME, "1", null);
    }

    public boolean updateData(String id, String UserID, String BPVTransition, String BPVBeetPointID, String BPVBeetRouteNPoint, String BPVLocation, String BPVPoint, String BPVRoute, String BPVDTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_1,id);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_2,UserID);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_3,BPVTransition);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_4,BPVBeetPointID);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_5,BPVBeetRouteNPoint);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_6,BPVLocation);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_7,BPVPoint);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_8,BPVRoute);
        contentValues.put(Constants.BEETPOINTVISIT_LOCAL_DB.COL_9,BPVDTime);
        db.update(Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME, contentValues, "ID = ?", new String[] { id });
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Constants.BEETPOINTVISIT_LOCAL_DB.TABLE_NAME, "ID = ?",new String[] {id});
    }
}