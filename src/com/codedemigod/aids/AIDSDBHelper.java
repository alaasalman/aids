package com.codedemigod.aids;

import com.codedemigod.model.ProcessStats;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AIDSDBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 3;
    private static final String STATS_TABLE_NAME = "pstats";
    private static final String STATS_ID = "id";
    private static final String STATS_PROCESS_NAME = "pname";
    private static final String STATS_RX_BYTES = "rxbytes";
    private static final String STATS_TX_BYTES = "txbytes";
    private static final String STATS_DIFF_RX_BYTES = "diffrxbytes";
    private static final String STATS_DIFF_TX_BYTES = "difftxbytes";
    private static final String STATS_TS = "timestamp";
    private static final String STATS_PROCESS_UID = "uid";
    private static final String STATS_CPU = "cpu";
    
    private static final String STATS_TABLE_CREATE =
                "CREATE TABLE " + STATS_TABLE_NAME + " (" +
                STATS_ID + " INTEGER PRIMARY KEY," +
                STATS_PROCESS_NAME + " TEXT, " +
                STATS_PROCESS_UID + " TEXT, " +
                STATS_CPU + " TEXT, " +
                STATS_RX_BYTES + " int," +
                STATS_TX_BYTES + " int," +
                STATS_DIFF_RX_BYTES + " int," +
                STATS_DIFF_TX_BYTES + " int," +
                STATS_TS + " int" +
                ");";

    private static final String GENERIC_STATS_TABLE_DROP =
            "DROP TABLE stats;";
    
    private static final String ADD_DIFF_TXBYTES = 
    		"ALTER TABLE " + STATS_TABLE_NAME +" ADD COLUMN " + STATS_DIFF_TX_BYTES + " INT;";
    
    private static final String ADD_DIFF_RXBYTES = 
    		"ALTER TABLE " + STATS_TABLE_NAME + " ADD COLUMN " + STATS_DIFF_RX_BYTES + " INT;";
    
    public AIDSDBHelper(Context context) {
        super(context, "stats.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(STATS_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//todo moving from differnet versions is not supported with this approach
		if(oldVersion == 1 && newVersion == 2){
			db.execSQL(GENERIC_STATS_TABLE_DROP);
			db.execSQL(STATS_TABLE_CREATE);
		}
		
		if(oldVersion == 2 && newVersion == 3){
			db.execSQL(ADD_DIFF_TXBYTES);
			db.execSQL(ADD_DIFF_RXBYTES);
		}
	}
	
	public boolean insertStats(ProcessStats pStats){
		SQLiteDatabase aidsDB =  this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		
		values.put(STATS_PROCESS_NAME, pStats.Name);
		values.put(STATS_PROCESS_UID, pStats.Uid);
		values.put(STATS_CPU, pStats.CPUUsage);
		values.put(STATS_RX_BYTES, pStats.RxBytes);
		values.put(STATS_TX_BYTES, pStats.TxBytes);
		values.put(STATS_TS, pStats.TimeSnapshot);
		
		long insertedID = aidsDB.insert(STATS_TABLE_NAME, null, values);
		
		aidsDB.close();
		
		if(insertedID == -1){
			return false;
		}
		
		return true;
	}
	
	public ProcessStats getLatestProcessStats(String uid){
		ProcessStats pStats = new ProcessStats();
		SQLiteDatabase aidsDB =  this.getReadableDatabase();
		
		SQLiteCursor cursor =  (SQLiteCursor) aidsDB.query(STATS_TABLE_NAME, new String[]{
				STATS_TS, STATS_PROCESS_NAME, 
				STATS_PROCESS_UID, STATS_CPU, 
				STATS_TX_BYTES, STATS_RX_BYTES
				},
				STATS_PROCESS_UID + "=?", new String[]{uid}, null, null, STATS_ID + " DESC", "1");
		
		if(cursor.moveToFirst() == false){
			cursor.close();
			aidsDB.close();
			
			return pStats;
		}
		
		pStats.TimeSnapshot = cursor.getLong(0);
		pStats.Name = cursor.getString(1);
		pStats.Uid = cursor.getString(2);
		pStats.CPUUsage = cursor.getString(3);
		pStats.TxBytes = cursor.getLong(4);
		pStats.RxBytes = cursor.getLong(5);
		
		cursor.close();
		aidsDB.close();
		
		return pStats;
	}
	
	public boolean resetAllStats(){
		SQLiteDatabase aidsDB =  this.getWritableDatabase();
		
		int numRows = aidsDB.delete(STATS_TABLE_NAME, "1", null);
		
		if(numRows <= 0){
			return false;
		}
		
		return true;
	}
}
