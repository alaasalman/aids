package com.codedemigod.aids;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.codedemigod.model.APackage;
import com.codedemigod.model.Alert;
import com.codedemigod.model.CPUUsage;
import com.codedemigod.model.Comm;
import com.codedemigod.model.Event;
import com.codedemigod.model.IEModel;
import com.codedemigod.model.MemoryUsage;
import com.codedemigod.model.NetworkUsage;
import com.codedemigod.model.Process;

public final class AIDSDBHelper extends SQLiteOpenHelper {
	private final String TAG = AIDSDBHelper.class.getName();

	private static final int DATABASE_VERSION = 4;
	private static final String DATABASE_NAME = "stats.db";

	// process table, collecting process name and timestamp
	private static final String PROCESS_TABLE_NAME = "process";
	private static final String ID = "id";
	private static final String TIMESTAMP = "timestamp";

	private static final String PROCESS_UID = "process_uid";
	private static final String PROCESS_PID = "process_pid";
	private static final String PROCESS_NAME = "process_name";

	private static final String PROCESS_TABLE_CREATE = "CREATE TABLE "
			+ PROCESS_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
			+ TIMESTAMP + " int," + PROCESS_UID + " TEXT, " + PROCESS_PID
			+ " TEXT, " + PROCESS_NAME + " TEXT " + ");";

	// cpu usage table, collecting cpu usage and pid
	private static final String CPUUSAGE_TABLE_NAME = "cpuusage";
	private static final String CPUUSAGE_CPU = "cpu";

	// TODO could it be that the PID would disappear before running the CPUUsage
	// collector? So 2 processes from same UID but different pids from
	// above.maybe just use name?
	private static final String CPUUSAGE_TABLE_CREATE = "CREATE TABLE "
			+ CPUUSAGE_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
			+ TIMESTAMP + " int," + PROCESS_PID + " TEXT, " + CPUUSAGE_CPU
			+ " TEXT " + ");";

	// network usage table, collecting recvd and transd bytes timestamped per
	// uid
	private static final String NETWORKUSAGE_TABLE_NAME = "networkusage";
	private static final String NETWORKUSAGE_RX_BYTES = "rxbytes";
	private static final String NETWORKUSAGE_RX_PACKETS = "rxpackets";
	private static final String NETWORKUSAGE_RX_TCP_PACKETS = "rxtcppackets";
	private static final String NETWORKUSAGE_RX_UDP_PACKETS = "rxudppackets";
	private static final String NETWORKUSAGE_TX_BYTES = "txbytes";
	private static final String NETWORKUSAGE_TX_PACKETS = "txpackets";
	private static final String NETWORKUSAGE_TX_TCP_PACKETS = "txtcppackets";
	private static final String NETWORKUSAGE_TX_UDP_PACKETS = "txudppackets";
	private static final String NETWORKUSAGE_DIFF_RX_BYTES = "diffrxbytes";
	private static final String NETWORKUSAGE_DIFF_TX_BYTES = "difftxbytes";

	private static final String NETWORKUSAGE_TABLE_CREATE = "CREATE TABLE "
			+ NETWORKUSAGE_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
			+ TIMESTAMP + " int," + PROCESS_UID + " TEXT, "
			+ NETWORKUSAGE_RX_BYTES + " int," + NETWORKUSAGE_TX_BYTES + " int,"
			+ NETWORKUSAGE_DIFF_RX_BYTES + " int," + NETWORKUSAGE_DIFF_TX_BYTES
			+ " int," + NETWORKUSAGE_RX_PACKETS + " int,"
			+ NETWORKUSAGE_RX_TCP_PACKETS + " int,"
			+ NETWORKUSAGE_RX_UDP_PACKETS + " int," + NETWORKUSAGE_TX_PACKETS
			+ " int," + NETWORKUSAGE_TX_TCP_PACKETS + " int,"
			+ NETWORKUSAGE_TX_UDP_PACKETS + " int" + ");";

	// memory usage table, collecting PSS&private&shared memory timestamped per
	// process
	private static final String MEMORYUSAGE_TABLE_NAME = "memoryusage";
	private static final String MEMORYUSAGE_PSS = "pss";
	private static final String MEMORYUSAGE_SHARED = "shared";
	private static final String MEMORYUSAGE_PRIVATE = "private";
	private static final String MEMORYUSAGE_DIFF_PSS = "diffpss";
	private static final String MEMORYUSAGE_DIFF_SHARED = "diffshared";
	private static final String MEMORYUSAGE_DIFF_PRIVATE = "diffprivate";

	private static final String MEMORYUSAGE_TABLE_CREATE = "CREATE TABLE "
			+ MEMORYUSAGE_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
			+ TIMESTAMP + " int," + PROCESS_PID + " TEXT, " + MEMORYUSAGE_PSS
			+ " int," + MEMORYUSAGE_SHARED + " int," + MEMORYUSAGE_PRIVATE
			+ " int," + MEMORYUSAGE_DIFF_PSS + " int,"
			+ MEMORYUSAGE_DIFF_SHARED + " int," + MEMORYUSAGE_DIFF_PRIVATE
			+ " int" + ");";

	// events table, collecting platform events such as screen on/off toggle and
	// app install plus extra metadata depending on event type
	private static final String EVENT_TABLE_NAME = "event";
	private static final String EVENT_TYPE = "type";
	private static final String EVENT_MORE = "more";

	private static final String EVENT_TABLE_CREATE = "CREATE TABLE "
			+ EVENT_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
			+ TIMESTAMP + " int," + EVENT_TYPE + " int, " + EVENT_MORE
			+ " TEXT " + ");";

	// packages table, collects package information from the system
	private static final String PACKAGE_TABLE_NAME = "package";
	private static final String PACKAGE_NAME = "name";
	private static final String PACKAGE_INSTALL_TIME = "instaltime";
	private static final String PACKAGE_UPDATE_TIME = "lastupdatetime";
	private static final String PACKAGE_REQ_PERMISSIONS = "reqpermissions";
	private static final String PACKAGE_REQ_FEATURES = "reqfeatures";
	private static final String PACKAGE_FIRST_SEEN = "firstseen";
	private static final String PACKAGE_THREAT = "threat";
	private static final String PACKAGE_NUMERIC_THREAT = "numthreat";

	private static final String PACKAGE_TABLE_CREATE = "CREATE TABLE "
			+ PACKAGE_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
			+ TIMESTAMP + " int," + PACKAGE_NAME + " TEXT, "
			+ PACKAGE_INSTALL_TIME + " int, " + PACKAGE_UPDATE_TIME + " int, "
			+ PACKAGE_REQ_PERMISSIONS + " TEXT, " + PACKAGE_REQ_FEATURES
			+ " TEXT, " + PACKAGE_FIRST_SEEN + " int, " + PACKAGE_THREAT
			+ " int, " + PACKAGE_NUMERIC_THREAT + " REAL, " + PROCESS_NAME
			+ " TEXT, " + PROCESS_UID + " TEXT" + ");";

	// comm table, collects ip and ports on ipv4 and 6 open and maps them to uid
	private static final String COMM_TABLE_NAME = "comm";
	private static final String COMM_LOCAL_IP_PORT = "local";
	private static final String COMM_REMOTE_IP_PORT = "remote";

	private static final String COMM_TABLE_CREATE = "CREATE TABLE "
			+ COMM_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY," + TIMESTAMP
			+ " int," + COMM_LOCAL_IP_PORT + " TEXT, " + COMM_REMOTE_IP_PORT
			+ " TEXT, " + PROCESS_UID + " TEXT" + ");";

	// TODO better description
	// table for IEModel, to hold cumulative resources
	// fromts, tots, processname, lowcpu, midcpu, highcpu
	private static final String IEMODEL_TABLE_NAME = "iemodel";
	private static final String IEMODEL_FROM_TS = "fromts";
	private static final String IEMODEL_TO_TS = "tots";
	private static final String IEMODEL_AGE = "age";
	private static final String IEMODEL_CPU_LOW = "cpu_low";
	private static final String IEMODEL_CPU_MID = "cpu_mid";
	private static final String IEMODEL_CPU_HIGH = "cpu_high";
	private static final String IEMODEL_CPU_COUNTER = "cpu_counter";

	private static final String IEMODEL_TABLE_CREATE = "CREATE TABLE "
			+ IEMODEL_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
			+ IEMODEL_FROM_TS + " int," + IEMODEL_TO_TS + " int, "
			+ PROCESS_NAME + " TEXT, " + IEMODEL_CPU_LOW + " int, "
			+ IEMODEL_CPU_MID + " int, " + IEMODEL_CPU_HIGH + " int, "
			+ IEMODEL_CPU_COUNTER + " int, " + IEMODEL_AGE + " int, "
			+ NETWORKUSAGE_RX_BYTES + " int, " + NETWORKUSAGE_TX_BYTES + " int"
			+ ");";

	// threat alerts table
	private static final String THREATALERT_TABLE_NAME = "alert";
	private static final String THREATALERT_NOTES = "notes";
	private static final String THREATALERT_TABLE_CREATE = "CREATE TABLE "
			+ THREATALERT_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY,"
			+ TIMESTAMP + " long," + THREATALERT_NOTES + " TEXT " + ");";

	private static final String LOG_TABLE_NAME = "log";
	private static final String LOG_DESC = "desc";

	private static final String LOG_TABLE_CREATE = "CREATE TABLE "
			+ LOG_TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY," + TIMESTAMP
			+ " long," + LOG_DESC + " TEXT " + ");";

	private static final String[] tables = new String[] { PROCESS_TABLE_NAME,
			CPUUSAGE_TABLE_NAME, NETWORKUSAGE_TABLE_NAME,
			MEMORYUSAGE_TABLE_NAME, EVENT_TABLE_NAME, PACKAGE_TABLE_NAME,
			IEMODEL_TABLE_NAME, THREATALERT_TABLE_NAME, LOG_TABLE_NAME, COMM_TABLE_NAME };

	private AIDSDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(PACKAGE_TABLE_CREATE);
		db.execSQL(PROCESS_TABLE_CREATE);
		db.execSQL(CPUUSAGE_TABLE_CREATE);
		db.execSQL(NETWORKUSAGE_TABLE_CREATE);
		db.execSQL(MEMORYUSAGE_TABLE_CREATE);
		db.execSQL(EVENT_TABLE_CREATE);
		db.execSQL(IEMODEL_TABLE_CREATE);
		db.execSQL(THREATALERT_TABLE_CREATE);
		db.execSQL(LOG_TABLE_CREATE);
		db.execSQL(COMM_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// this will be reconsidered but on upgrade conserve old data just in
		// case...
		for (String tabname : tables) {
			try {
				db.execSQL(String.format("ALTER TABLE %s RENAME TO %s",
						tabname, tabname + oldVersion));
			} catch (Exception e) {
				// table probably doesnt exist
				continue;
			}
		}

		onCreate(db);
	}

	private static AIDSDBHelper mDBHelper;

	public synchronized static AIDSDBHelper getInstance(Context context) {

		if (mDBHelper == null) {
			mDBHelper = new AIDSDBHelper(context);
		}

		return mDBHelper;
	}

	public boolean insertProcess(Process p) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, p.TimeStamp);
		values.put(PROCESS_PID, p.Pid);
		values.put(PROCESS_NAME, p.Name);
		values.put(PROCESS_UID, p.Uid);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(PROCESS_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}

		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	public boolean insertCPUUsage(CPUUsage cu) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, cu.TimeStamp);
		values.put(PROCESS_PID, cu.Pid);
		values.put(CPUUSAGE_CPU, cu.CPUUsage);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(CPUUSAGE_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}

		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	public boolean insertNetworkUsage(NetworkUsage nu) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, nu.TimeStamp);
		values.put(PROCESS_UID, nu.Uid);
		values.put(NETWORKUSAGE_RX_BYTES, nu.RxBytes);
		values.put(NETWORKUSAGE_RX_PACKETS, nu.RxPackets);
		values.put(NETWORKUSAGE_RX_TCP_PACKETS, nu.RxTcpPackets);
		values.put(NETWORKUSAGE_RX_UDP_PACKETS, nu.RxUdpPackets);
		values.put(NETWORKUSAGE_TX_BYTES, nu.TxBytes);
		values.put(NETWORKUSAGE_TX_PACKETS, nu.TxPackets);
		values.put(NETWORKUSAGE_TX_TCP_PACKETS, nu.TxTcpPackets);
		values.put(NETWORKUSAGE_TX_UDP_PACKETS, nu.TxUdpPackets);
		values.put(NETWORKUSAGE_DIFF_RX_BYTES, nu.DiffRxBytes);
		values.put(NETWORKUSAGE_DIFF_TX_BYTES, nu.DiffTxBytes);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(NETWORKUSAGE_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}

		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	public boolean insertMemoryUsage(MemoryUsage mu) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, mu.TimeStamp);
		values.put(PROCESS_PID, mu.Pid);
		values.put(MEMORYUSAGE_PSS, mu.PSSMemory);
		values.put(MEMORYUSAGE_PRIVATE, mu.PrivateMemory);
		values.put(MEMORYUSAGE_SHARED, mu.SharedMemory);
		values.put(MEMORYUSAGE_DIFF_PSS, mu.DiffPSSMemory);
		values.put(MEMORYUSAGE_DIFF_PRIVATE, mu.DiffPrivateMemory);
		values.put(MEMORYUSAGE_DIFF_SHARED, mu.DiffSharedMemory);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(MEMORYUSAGE_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}

		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	public boolean insertEvent(Event ev) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, ev.TimeStamp);
		values.put(EVENT_TYPE, ev.Type.ordinal());
		values.put(EVENT_MORE, ev.More);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(EVENT_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}
		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	public boolean insertPackage(APackage p) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		StringBuilder sbPermissions = new StringBuilder();

		if (p.RequestedPermissions != null) {
			for (String perm : p.RequestedPermissions) {
				sbPermissions.append(perm);
				sbPermissions.append(";");
			}
		}

		StringBuilder sbFeatures = new StringBuilder();

		if (p.RequestedFeatures != null) {
			for (String feat : p.RequestedFeatures) {
				sbFeatures.append(feat);
				sbFeatures.append(";");
			}
		}

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, p.TimeStamp);
		values.put(PACKAGE_NAME, p.Name);
		values.put(PACKAGE_INSTALL_TIME, p.InstallTime);
		values.put(PACKAGE_UPDATE_TIME, p.UpdateTime);
		values.put(PACKAGE_REQ_PERMISSIONS, sbPermissions.toString());
		values.put(PACKAGE_REQ_FEATURES, sbFeatures.toString());
		values.put(PACKAGE_FIRST_SEEN, p.FirstSeen);
		values.put(PACKAGE_THREAT, p.Threat.ordinal());
		values.put(PACKAGE_NUMERIC_THREAT, p.Threat_Numeric);
		values.put(PROCESS_NAME, p.ProcessName);
		values.put(PROCESS_UID, p.Uid);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(PACKAGE_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}
		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	public boolean insertAlert(Alert al) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, al.TimeStamp);
		values.put(THREATALERT_NOTES, al.Notes);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(THREATALERT_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}

		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	public boolean insertLog(String logText) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, Calendar.getInstance().getTimeInMillis());
		values.put(LOG_DESC, logText);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(LOG_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}

		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	public List<Process> getProcesses(long fromTS, long toTS) {
		SQLiteDatabase aidsDB = this.getReadableDatabase();
		ArrayList<Process> pList = new ArrayList<Process>();

		SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(true,
				PROCESS_TABLE_NAME, new String[] { PROCESS_NAME, PROCESS_PID,
						PROCESS_UID }, TIMESTAMP + " between ? and ?",
				new String[] { String.valueOf(fromTS), String.valueOf(toTS) },
				null, null, null, null);

		if (cursor.getCount() == 0) {
			return pList;
		}

		while (cursor.moveToNext()) {
			Process p = new Process();
			p.Name = cursor.getString(0);
			p.Pid = cursor.getString(1);
			p.Uid = cursor.getString(2);

			pList.add(p);
		}

		return pList;
	}

	public List<NetworkUsage> getNetworkUsage(long fromTS, long toTS) {
		SQLiteDatabase aidsDB = this.getReadableDatabase();
		ArrayList<NetworkUsage> nuList = new ArrayList<NetworkUsage>();

		SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(
				NETWORKUSAGE_TABLE_NAME, new String[] { PROCESS_UID,
						NETWORKUSAGE_RX_BYTES, NETWORKUSAGE_TX_BYTES },
				TIMESTAMP + " between ? and ?",
				new String[] { String.valueOf(fromTS), String.valueOf(toTS) },
				null, null, null, null);

		if (cursor.getCount() == 0) {
			return nuList;
		}

		while (cursor.moveToNext()) {
			NetworkUsage nu = new NetworkUsage();

			nu.Uid = cursor.getString(0);
			nu.RxBytes = cursor.getLong(1);
			nu.TxBytes = cursor.getLong(2);

			nuList.add(nu);
		}

		return nuList;
	}

	/*
	 * Gets recv'd and tx'd bytes for uid and time period. This returns a
	 * hashmap of rx, tx values.
	 */
	public HashMap<String, String> getBandwidthUsage(String uid, long fromTS,
			long toTS) {
		SQLiteDatabase aidsDB = this.getReadableDatabase();
		HashMap<String, String> netUse = new HashMap<String, String>();

		netUse.put("rx", "0");
		netUse.put("tx", "0");
		// TODO optimize and consider that during this time period, a reset
		// might have happened
		// get last record for uid by id order
		SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(
				NETWORKUSAGE_TABLE_NAME, new String[] { NETWORKUSAGE_RX_BYTES,
						NETWORKUSAGE_TX_BYTES }, TIMESTAMP
						+ " between ? and ? and " + PROCESS_UID + "=?",
				new String[] { String.valueOf(fromTS), String.valueOf(toTS),
						uid }, null, null, "id desc", "1");

		if (cursor.getCount() == 0) {
			return netUse;
		}

		cursor.moveToFirst();
		int maxRx = cursor.getInt(0);
		int maxTx = cursor.getInt(1);

		// get first record for uid by id order
		cursor = (SQLiteCursor) aidsDB.query(NETWORKUSAGE_TABLE_NAME,
				new String[] { NETWORKUSAGE_RX_BYTES, NETWORKUSAGE_TX_BYTES },
				TIMESTAMP + " between ? and ? and " + PROCESS_UID + "=?",
				new String[] { String.valueOf(fromTS), String.valueOf(toTS),
						uid }, null, null, "id asc", "1");

		cursor.moveToFirst();
		int minRx = cursor.getInt(0);
		int minTx = cursor.getInt(1);

		cursor.close();

		netUse.put("rx", String.valueOf(maxRx - minRx));
		netUse.put("tx", String.valueOf(maxTx - minTx));

		return netUse;
	}

	public List<CPUUsage> getCPUUsage(String pid, long fromTS, long toTS) {
		SQLiteDatabase aidsDB = this.getReadableDatabase();
		ArrayList<CPUUsage> cpuList = new ArrayList<CPUUsage>();

		SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(CPUUSAGE_TABLE_NAME,
				new String[] { CPUUSAGE_CPU }, TIMESTAMP
						+ " between ? and ? and " + PROCESS_PID + "=?",
				new String[] { String.valueOf(fromTS), String.valueOf(toTS),
						pid }, null, null, null);

		if (cursor.getCount() == 0) {
			return cpuList;
		}

		while (cursor.moveToNext()) {
			CPUUsage cpu = new CPUUsage();
			cpu.CPUUsage = cursor.getString(0);

			cpuList.add(cpu);
		}

		cursor.close();

		return cpuList;
	}

	// get IEModel for process
	public IEModel getIEModel(String pName) {
		SQLiteDatabase aidsDB = this.getReadableDatabase();

		SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(IEMODEL_TABLE_NAME,
				new String[] { IEMODEL_FROM_TS, IEMODEL_TO_TS, IEMODEL_CPU_LOW,
						IEMODEL_CPU_MID, IEMODEL_CPU_HIGH, ID,
						IEMODEL_CPU_COUNTER, IEMODEL_AGE,
						NETWORKUSAGE_RX_BYTES, NETWORKUSAGE_TX_BYTES },
				PROCESS_NAME + "=?", new String[] { pName }, null, null, null);

		if (cursor.getCount() == 0) {
			return null;
		}

		cursor.moveToFirst(); // only one model for process is expected

		IEModel iem = new IEModel();
		iem.ProcessName = pName;
		iem.FromTimeStamp = cursor.getLong(0);
		iem.ToTimeStamp = cursor.getLong(1);
		iem.CPULow = cursor.getInt(2);
		iem.CPUMid = cursor.getInt(3);
		iem.CPUHigh = cursor.getInt(4);
		iem.ID = cursor.getInt(5);
		iem.CPUCounter = cursor.getInt(6);
		iem.Age = cursor.getInt(7);
		iem.RxBytes = cursor.getInt(8);
		iem.TxBytes = cursor.getInt(9);

		cursor.close();

		return iem;
	}

	// get all IEModels
	public List<IEModel> getIEModel() {
		SQLiteDatabase aidsDB = this.getReadableDatabase();
		ArrayList<IEModel> ieModelList = new ArrayList<IEModel>();

		SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(IEMODEL_TABLE_NAME,
				new String[] { IEMODEL_FROM_TS, IEMODEL_TO_TS, IEMODEL_CPU_LOW,
						IEMODEL_CPU_MID, IEMODEL_CPU_HIGH, ID,
						IEMODEL_CPU_COUNTER, PROCESS_NAME, IEMODEL_AGE,
						NETWORKUSAGE_RX_BYTES, NETWORKUSAGE_TX_BYTES }, null,
				null, null, null, null);

		if (cursor.getCount() == 0) {
			return ieModelList;
		}

		while (cursor.moveToNext()) {
			IEModel iem = new IEModel();
			iem.FromTimeStamp = cursor.getLong(0);
			iem.ToTimeStamp = cursor.getLong(1);
			iem.CPULow = cursor.getInt(2);
			iem.CPUMid = cursor.getInt(3);
			iem.CPUHigh = cursor.getInt(4);
			iem.ID = cursor.getInt(5);
			iem.CPUCounter = cursor.getInt(6);
			iem.ProcessName = cursor.getString(7);
			iem.Age = cursor.getInt(8);
			iem.RxBytes = cursor.getInt(9);
			iem.TxBytes = cursor.getInt(10);

			ieModelList.add(iem);
		}

		cursor.close();

		return ieModelList;
	}

	public boolean updateIEModel(IEModel iem) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(IEMODEL_FROM_TS, iem.FromTimeStamp);
		values.put(IEMODEL_TO_TS, iem.ToTimeStamp);
		values.put(PROCESS_NAME, iem.ProcessName);
		values.put(IEMODEL_CPU_LOW, iem.CPULow);
		values.put(IEMODEL_CPU_MID, iem.CPUMid);
		values.put(IEMODEL_CPU_HIGH, iem.CPUHigh);
		values.put(IEMODEL_CPU_COUNTER, iem.CPUCounter);
		values.put(IEMODEL_AGE, iem.Age);
		values.put(NETWORKUSAGE_RX_BYTES, iem.RxBytes);
		values.put(NETWORKUSAGE_TX_BYTES, iem.TxBytes);

		int affectedRows = aidsDB.update(IEMODEL_TABLE_NAME, values, ID + "=?",
				new String[] { String.valueOf(iem.ID) });

		if (affectedRows != 1) {
			return false;
		}

		return true;
	}

	public boolean insertIEModel(IEModel iem) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(IEMODEL_FROM_TS, iem.FromTimeStamp);
		values.put(IEMODEL_TO_TS, iem.ToTimeStamp);
		values.put(PROCESS_NAME, iem.ProcessName);
		values.put(IEMODEL_CPU_LOW, iem.CPULow);
		values.put(IEMODEL_CPU_MID, iem.CPUMid);
		values.put(IEMODEL_CPU_HIGH, iem.CPUHigh);
		values.put(IEMODEL_CPU_COUNTER, iem.CPUCounter);
		values.put(IEMODEL_AGE, iem.Age);
		values.put(NETWORKUSAGE_RX_BYTES, iem.RxBytes);
		values.put(NETWORKUSAGE_TX_BYTES, iem.TxBytes);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(IEMODEL_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}

		if (insertedID == -1) {
			return false;
		}

		return true;
	}

	// TODO what to do with the different timestamps?
	// returns listing of all packages
	public List<APackage> getPackage() {
		SQLiteDatabase aidsDB = this.getReadableDatabase();
		ArrayList<APackage> pkgList = new ArrayList<APackage>();

		SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(PACKAGE_TABLE_NAME,
				new String[] { PACKAGE_NAME, PACKAGE_INSTALL_TIME,
						PACKAGE_UPDATE_TIME, PACKAGE_NUMERIC_THREAT,
						PROCESS_NAME, PROCESS_UID, ID }, null, null, null,
				null, TIMESTAMP + " DESC");

		if (cursor.getCount() == 0) {
			return pkgList;
		}

		while (cursor.moveToNext()) {
			APackage pkg = new APackage();

			pkg.Name = cursor.getString(0);
			pkg.InstallTime = cursor.getLong(1);
			pkg.UpdateTime = cursor.getLong(2);
			pkg.Threat_Numeric = cursor.getFloat(3);
			pkg.ProcessName = cursor.getString(4);
			pkg.Uid = cursor.getString(5);
			pkg.ID = cursor.getInt(6);

			pkgList.add(pkg);
		}

		cursor.close();

		return pkgList;
	}

	// returns firts package with specified name
	public APackage getPackage(String name) {
		SQLiteDatabase aidsDB = this.getReadableDatabase();

		SQLiteCursor cursor = (SQLiteCursor) aidsDB.query(PACKAGE_TABLE_NAME,
				new String[] { PACKAGE_NAME, PACKAGE_INSTALL_TIME,
						PACKAGE_UPDATE_TIME, PACKAGE_NUMERIC_THREAT,
						PROCESS_NAME, PROCESS_UID, ID, PACKAGE_THREAT },
				PACKAGE_NAME + "=? or " + PROCESS_NAME + "=?",
				new String[] { name }, null, null, TIMESTAMP + " DESC");

		if (cursor.getCount() == 0) {
			return null;
		}

		// although shouldn't be more than one, only care about the first just
		// in case
		cursor.moveToFirst();

		APackage pkg = new APackage();

		pkg.Name = cursor.getString(0);
		pkg.InstallTime = cursor.getLong(1);
		pkg.UpdateTime = cursor.getLong(2);
		pkg.Threat_Numeric = cursor.getFloat(3);
		pkg.ProcessName = cursor.getString(4);
		pkg.Uid = cursor.getString(5);
		pkg.ID = cursor.getInt(6);
		pkg.Threat = APackage.Threat_Type.values()[cursor.getInt(7)];
		cursor.close();

		return pkg;
	}

	public boolean updatePackage(APackage pkg) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(PACKAGE_THREAT, pkg.Threat.ordinal());
		values.put(PACKAGE_NUMERIC_THREAT, pkg.Threat_Numeric);

		int affectedRows = aidsDB.update(PACKAGE_TABLE_NAME, values, ID + "=?",
				new String[] { String.valueOf(pkg.ID) });

		if (affectedRows != 1) {
			return false;
		}

		return true;
	}

	public boolean insertComm(Comm pCom) {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		long insertedID = 0;
		ContentValues values = new ContentValues();

		values.put(TIMESTAMP, pCom.TimeStamp);
		values.put(PROCESS_UID, pCom.Uid);
		values.put(COMM_LOCAL_IP_PORT, pCom.LocalIpAndPort);
		values.put(COMM_REMOTE_IP_PORT, pCom.RemoteIpAndPort);

		try {
			aidsDB.beginTransaction();
			insertedID = aidsDB.insert(COMM_TABLE_NAME, null, values);
			aidsDB.setTransactionSuccessful();
		} finally {
			aidsDB.endTransaction();
		}

		if (insertedID == -1) {
			return false;
		}

		return true;
	}
	
	public boolean resetAllData() {
		SQLiteDatabase aidsDB = this.getWritableDatabase();

		Log.i(TAG, "Resetting data based on user command");

		for (String tabname : tables) {
			aidsDB.delete(tabname, "1", null);
		}

		return true;
	}
}
