package com.codedemigod.aids;

import com.codedemigod.model.MobileAttrib;
import com.codedemigod.model.ProcessStats;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AIDSDBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 5;
    private static final String STATS_TABLE_NAME = "pstats";
    private static final String STATS_ID = "id";
    private static final String STATS_PROCESS_NAME = "pname";
    private static final String STATS_RX_BYTES = "rxbytes";
    private static final String STATS_TX_BYTES = "txbytes";
    private static final String STATS_DIFF_RX_BYTES = "diffrxbytes";
    private static final String STATS_DIFF_TX_BYTES = "difftxbytes";
    private static final String COLUMN_TS = "timestamp";
    private static final String STATS_PROCESS_UID = "uid";
    private static final String STATS_CPU = "cpu";
    private static final String STATS_PSS = "pss";
    private static final String STATS_SHARED = "shared";
    private static final String STATS_PRIVATE = "private";
    private static final String STATS_DIFF_PSS = "diffpss";
    private static final String STATS_DIFF_SHARED = "diffshared";
    private static final String STATS_DIFF_PRIVATE = "diffprivate";
    private static final String MOBILEATTRIB_TABLE_NAME = "mobile_attrib";
    private static final String MOBILEATTRIB_ID = "id";
    private static final String MOBILEATTRIB_SCREENLOCK = "isscreenlocked";
    
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
                COLUMN_TS + " int," +
                STATS_PSS + " int," +
                STATS_SHARED + " int," +
                STATS_PRIVATE + " int," +
                STATS_DIFF_PSS + " int," +
                STATS_DIFF_SHARED + " int," +
                STATS_DIFF_PRIVATE + " int," +
                ");";
    
    private static final String MOBILEATTRIB_TABLE_CREATE = 
				"CREATE TABLE " + MOBILEATTRIB_TABLE_NAME + " (" +
				MOBILEATTRIB_ID + " INTEGER PRIMARY KEY," +
				COLUMN_TS + " int," +
				MOBILEATTRIB_SCREENLOCK + " int" +
                ");";

    private static final String GENERIC_STATS_TABLE_DROP =
            "DROP TABLE stats;";
    
    private static final String ADD_DIFF_TXBYTES = 
    		"ALTER TABLE " + STATS_TABLE_NAME +" ADD COLUMN " + STATS_DIFF_TX_BYTES + " INT;";
    
    private static final String ADD_DIFF_RXBYTES = 
    		"ALTER TABLE " + STATS_TABLE_NAME + " ADD COLUMN " + STATS_DIFF_RX_BYTES + " INT;";
    
    private static final String ADD_PSS = 
    		"ALTER TABLE " + STATS_TABLE_NAME + " ADD COLUMN " + STATS_PSS + " INT;";
    
    private static final String ADD_SHARED = 
    		"ALTER TABLE " + STATS_TABLE_NAME + " ADD COLUMN " + STATS_SHARED + " INT;";
    
    private static final String ADD_PRIVATE = 
    		"ALTER TABLE " + STATS_TABLE_NAME + " ADD COLUMN " + STATS_PRIVATE + " INT;";
    
    private static final String ADD_DIFF_PSS = 
    		"ALTER TABLE " + STATS_TABLE_NAME + " ADD COLUMN " + STATS_DIFF_PSS + " INT;";
    
    private static final String ADD_DIFF_SHARED = 
    		"ALTER TABLE " + STATS_TABLE_NAME + " ADD COLUMN " + STATS_DIFF_SHARED + " INT;";
    
    private static final String ADD_DIFF_PRIVATE = 
    		"ALTER TABLE " + STATS_TABLE_NAME + " ADD COLUMN " + STATS_DIFF_PRIVATE + " INT;";
    
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
		
		if(oldVersion == 3 && newVersion == 4){
			db.execSQL(ADD_PSS);
			db.execSQL(ADD_SHARED);
			db.execSQL(ADD_PRIVATE);
			db.execSQL(ADD_DIFF_PSS);
			db.execSQL(ADD_DIFF_SHARED);
			db.execSQL(ADD_DIFF_PRIVATE);
		}
		
		if(oldVersion == 4 && newVersion == 5){
			db.execSQL(MOBILEATTRIB_TABLE_CREATE);
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
		values.put(STATS_DIFF_RX_BYTES, pStats.DiffRxBytes);
		values.put(STATS_DIFF_TX_BYTES, pStats.DiffTxBytes);
		values.put(COLUMN_TS, pStats.TimeSnapshot);
		values.put(STATS_PSS, pStats.PSSMemory);
		values.put(STATS_SHARED, pStats.SharedMemory);
		values.put(STATS_PRIVATE, pStats.PrivateMemory);
		values.put(STATS_DIFF_PSS, pStats.DiffPSSMemory);
		values.put(STATS_DIFF_SHARED, pStats.DiffSharedMemory);
		values.put(STATS_DIFF_PRIVATE, pStats.DiffPrivateMemory);
		
		long insertedID = aidsDB.insert(STATS_TABLE_NAME, null, values);
		
		aidsDB.close();
		
		if(insertedID == -1){
			return false;
		}
		
		return true;
	}
	
	public boolean insertMobileAttrib(MobileAttrib mb){
		SQLiteDatabase aidsDB =  this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(COLUMN_TS, mb.TimeSnapshot);
		values.put(MOBILEATTRIB_SCREENLOCK, mb.IsScreenLocked);
		
		long insertedID = aidsDB.insert(MOBILEATTRIB_TABLE_NAME, null, values);
		
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
				COLUMN_TS, STATS_PROCESS_NAME, 
				STATS_PROCESS_UID, STATS_CPU, 
				STATS_TX_BYTES, STATS_RX_BYTES,
				STATS_PSS, STATS_SHARED,
				STATS_PRIVATE, STATS_DIFF_PSS,
				STATS_DIFF_SHARED, STATS_DIFF_PRIVATE,
				STATS_DIFF_TX_BYTES, STATS_DIFF_RX_BYTES
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
		pStats.PSSMemory = cursor.getInt(6);
		pStats.SharedMemory = cursor.getInt(7);
		pStats.PrivateMemory = cursor.getInt(8);
		pStats.DiffPSSMemory = cursor.getInt(9);
		pStats.DiffSharedMemory = cursor.getInt(10);
		pStats.DiffPrivateMemory = cursor.getInt(11);
		pStats.DiffTxBytes = cursor.getInt(12);
		pStats.DiffRxBytes = cursor.getInt(13);
		
		
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
