package com.hornetincorporation.beatroute;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeoFenceLocalDB extends SQLiteOpenHelper{

    public GeoFenceLocalDB(Context context) {
        super(context, Constants.GEOFENCE_LOCAL_DB.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + Constants.GEOFENCE_LOCAL_DB.TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + Constants.GEOFENCE_LOCAL_DB.COL_2 + " TEXT, " + Constants.GEOFENCE_LOCAL_DB.COL_3 + " TEXT, " + Constants.GEOFENCE_LOCAL_DB.COL_4 + " TEXT, " + Constants.GEOFENCE_LOCAL_DB.COL_5 + " TEXT, " + Constants.GEOFENCE_LOCAL_DB.COL_6 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.GEOFENCE_LOCAL_DB.TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String geofenceReqID, String latitude, String longitude, String beatroute, String beatpoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_2,geofenceReqID);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_3,latitude);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_4,longitude);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_5,beatroute);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_6,beatpoint);
        long result = db.insert(Constants.GEOFENCE_LOCAL_DB.TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + Constants.GEOFENCE_LOCAL_DB.TABLE_NAME,null);
        return res;
    }

    public Cursor checkTableExists() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor restab = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='" + Constants.GEOFENCE_LOCAL_DB.TABLE_NAME + "'",null);
        return restab;
    }

    public Integer deleteAllData () {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Constants.GEOFENCE_LOCAL_DB.TABLE_NAME, "1", null);
    }

    public boolean updateData(String id, String geofenceReqID, String latitude, String longitude, String beatroute, String beatpoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_1,id);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_2,geofenceReqID);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_3,latitude);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_4,longitude);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_5,beatroute);
        contentValues.put(Constants.GEOFENCE_LOCAL_DB.COL_6,beatpoint);
        db.update(Constants.GEOFENCE_LOCAL_DB.TABLE_NAME, contentValues, "ID = ?", new String[] { id });
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Constants.GEOFENCE_LOCAL_DB.TABLE_NAME, "ID = ?",new String[] {id});
    }
}