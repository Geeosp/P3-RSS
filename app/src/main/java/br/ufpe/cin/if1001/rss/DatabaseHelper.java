package br.ufpe.cin.if1001.rss;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Geovane on 03/05/2015.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
   private  static final String DATABASE_NAME = "RSS";
    private static final int DATABASE_VERSION = 1;

    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE " + "FEEDS" + " (" +
                    " TITLE TEXT, DESCRIPTION TEXT, LINK TEXT, LIDO  INTEGER" +
                    ");";

      DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DICTIONARY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

