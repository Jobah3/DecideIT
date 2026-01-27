package jovan.janjic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "decideit.db";
    private static final int DATABASE_VERSION = 2;

    // USERS table
    public static final String TABLE_USERS = "USERS";
    public static final String U_COL_ID = "_id";
    public static final String U_COL_NAME = "name";
    public static final String U_COL_SURNAME = "surname";
    public static final String U_COL_USERNAME = "username";
    public static final String U_COL_PASSWORD = "password_hash";
    public static final String U_COL_SALT = "password_salt";
    public static final String U_COL_ROLE = "role";

    // SESSIONS table
    public static final String TABLE_SESSIONS = "SESSIONS";
    public static final String S_COL_ID = "_id";
    public static final String S_COL_SERVER_ID = "server_id";
    public static final String S_COL_DATE = "date";
    public static final String S_COL_NAME = "session_name";
    public static final String S_COL_DESC = "description";
    public static final String S_COL_END = "end_of_voting";

    // VOTES table
    public static final String TABLE_VOTES = "VOTES";
    public static final String V_COL_ID = "_id";
    public static final String V_COL_YES = "yes_votes";
    public static final String V_COL_NO = "no_votes";
    public static final String V_COL_ABSTAIN = "abstain_votes";
    public static final String V_COL_SESSION_NAME = "session_name";
    public static final String V_COL_SESSION_DATE = "session_date";

    private static final int HASH_ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + U_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + U_COL_NAME + " TEXT, "
                + U_COL_SURNAME + " TEXT, "
                + U_COL_USERNAME + " TEXT UNIQUE, "
                + U_COL_PASSWORD + " TEXT, "
                + U_COL_SALT + " TEXT, "
                + U_COL_ROLE + " TEXT"
                + ");";

        String createSessions = "CREATE TABLE IF NOT EXISTS " + TABLE_SESSIONS + " ("
                + S_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + S_COL_SERVER_ID + " TEXT, "
                + S_COL_DATE + " TEXT, "
                + S_COL_NAME + " TEXT, "
                + S_COL_DESC + " TEXT, "
                + S_COL_END + " TEXT"
                + ");";

        String createVotes = "CREATE TABLE IF NOT EXISTS " + TABLE_VOTES + " ("
                + V_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + V_COL_YES + " INTEGER DEFAULT 0, "
                + V_COL_NO + " INTEGER DEFAULT 0, "
                + V_COL_ABSTAIN + " INTEGER DEFAULT 0, "
                + V_COL_SESSION_NAME + " TEXT, "
                + V_COL_SESSION_DATE + " TEXT"
                + ");";

        db.execSQL(createUsers);
        db.execSQL(createSessions);
        db.execSQL(createVotes);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_SESSIONS + " ADD COLUMN " + S_COL_SERVER_ID + " TEXT");
            } catch (Exception ignored) {}
        }
    }

    // PBKDF2 hashing with salt
    private static String toHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for (byte b : array) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
        }
        return out;
    }

    private static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return toHex(salt);
    }

    private static String pbkdf2(String password, String saltHex) {
        try {
            byte[] salt = fromHex(saltHex);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return toHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e("DB", "Hashing error", e);
            return null;
        }
    }

    // User operations
    public boolean insertUser(String name, String surname, String username, String passwordPlain, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(U_COL_NAME, name);
        cv.put(U_COL_SURNAME, surname);
        cv.put(U_COL_USERNAME, username);
        String salt = generateSalt();
        String hashed = pbkdf2(passwordPlain, salt);
        cv.put(U_COL_PASSWORD, hashed);
        cv.put(U_COL_SALT, salt);
        cv.put(U_COL_ROLE, role);
        long res = -1;
        try {
            res = db.insertOrThrow(TABLE_USERS, null, cv);
        } catch (Exception e) {
            Log.w("DB", "insertUser failed: " + e.getMessage());
        }
        return res != -1;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{U_COL_ID}, U_COL_USERNAME + "=?",
                new String[]{username}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public String[] getNameSurnameByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{U_COL_NAME, U_COL_SURNAME}, U_COL_USERNAME + "=?",
                new String[]{username}, null, null, null);
        String[] res = null;
        if (c.moveToFirst()) {
            res = new String[]{c.getString(0), c.getString(1)};
        }
        c.close();
        return res;
    }

    public boolean validateLogin(String username, String passwordPlain) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{U_COL_PASSWORD, U_COL_SALT}, U_COL_USERNAME + "=?",
                new String[]{username}, null, null, null);
        boolean valid = false;
        if (c.moveToFirst()) {
            String storedHash = c.getString(0);
            String salt = c.getString(1);
            String providedHash = pbkdf2(passwordPlain, salt);
            valid = storedHash != null && storedHash.equals(providedHash);
        }
        c.close();
        return valid;
    }

    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{U_COL_ROLE}, U_COL_USERNAME + "=?",
                new String[]{username}, null, null, null);
        String role = null;
        if (c.moveToFirst()) role = c.getString(0);
        c.close();
        return role;
    }

    // Votes operations
    public boolean ensureVoteRowExists(String sessionName, String sessionDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(TABLE_VOTES, new String[]{V_COL_ID}, V_COL_SESSION_NAME + "=? AND " + V_COL_SESSION_DATE + "=?",
                new String[]{sessionName, sessionDate}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        if (!exists) {
            ContentValues cv = new ContentValues();
            cv.put(V_COL_SESSION_NAME, sessionName);
            cv.put(V_COL_SESSION_DATE, sessionDate);
            long id = db.insert(TABLE_VOTES, null, cv);
            return id != -1;
        }
        return true;
    }

    public boolean incrementVote(String sessionName, String sessionDate, String voteType) {
        if (!ensureVoteRowExists(sessionName, sessionDate)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        String col;
        if ("yes".equalsIgnoreCase(voteType)) col = V_COL_YES;
        else if ("no".equalsIgnoreCase(voteType)) col = V_COL_NO;
        else col = V_COL_ABSTAIN;
        // UPDATE VOTES SET col = col + 1 WHERE session_name=? AND session_date=?
        db.execSQL(String.format(Locale.US,
                "UPDATE %s SET %s = COALESCE(%s,0) + 1 WHERE %s=? AND %s=?",
                TABLE_VOTES, col, col, V_COL_SESSION_NAME, V_COL_SESSION_DATE),
                new Object[]{sessionName, sessionDate});
        return true;
    }

    

    public void addVote(String sessionName, String sessionDate, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM VOTES WHERE session_name=? AND session_date=?", new String[]{sessionName, sessionDate});
        if (c.moveToFirst()) {
            int yes = c.getInt(c.getColumnIndexOrThrow("yes_votes"));
            int no = c.getInt(c.getColumnIndexOrThrow("no_votes"));
            int abstain = c.getInt(c.getColumnIndexOrThrow("abstain_votes"));
            if (type.equals("yes")) yes++;
            else if (type.equals("no")) no++;
            else abstain++;
            ContentValues cv = new ContentValues();
            cv.put("yes_votes", yes);
            cv.put("no_votes", no);
            cv.put("abstain_votes", abstain);
            db.update("VOTES", cv, "session_name=? AND session_date=?", new String[]{sessionName, sessionDate});
        } else {
            ContentValues cv = new ContentValues();
            cv.put("session_name", sessionName);
            cv.put("session_date", sessionDate);
            cv.put("yes_votes", type.equals("yes")?1:0);
            cv.put("no_votes", type.equals("no")?1:0);
            cv.put("abstain_votes", type.equals("abstain")?1:0);
            db.insert("VOTES", null, cv);
        }
        c.close();
    }

    public int[] getVotes(String sessionName, String sessionDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM VOTES WHERE session_name=? AND session_date=?", new String[]{sessionName, sessionDate});
        int[] res = new int[]{0,0,0};
        if (c.moveToFirst()) {
            res[0] = c.getInt(c.getColumnIndexOrThrow("yes_votes"));
            res[1] = c.getInt(c.getColumnIndexOrThrow("no_votes"));
            res[2] = c.getInt(c.getColumnIndexOrThrow("abstain_votes"));
        }
        c.close();
        return res;
    }
    
    // Sessions sync helpers
    public void upsertSessionFromServer(String serverId, String dateLocal, String name, String description, String endIso) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(S_COL_SERVER_ID, serverId);
        cv.put(S_COL_DATE, dateLocal);
        cv.put(S_COL_NAME, name);
        cv.put(S_COL_DESC, description);
        cv.put(S_COL_END, endIso);

        if (serverId != null && serverId.length() > 0) {
            Cursor c = db.query(TABLE_SESSIONS, new String[]{S_COL_ID}, S_COL_SERVER_ID + "=?", new String[]{serverId}, null, null, null);
            boolean exists = c.moveToFirst();
            c.close();
            if (exists) {
                db.update(TABLE_SESSIONS, cv, S_COL_SERVER_ID + "=?", new String[]{serverId});
                return;
            }
            db.insert(TABLE_SESSIONS, null, cv);
        } else {
            Cursor c = db.query(TABLE_SESSIONS, new String[]{S_COL_ID}, S_COL_NAME + "=? AND " + S_COL_DATE + "=?",
                    new String[]{name, dateLocal}, null, null, null);
            boolean exists = c.moveToFirst();
            c.close();
            if (exists) {
                db.update(TABLE_SESSIONS, cv, S_COL_NAME + "=? AND " + S_COL_DATE + "=?", new String[]{name, dateLocal});
            } else {
                db.insert(TABLE_SESSIONS, null, cv);
            }
        }
    }

    public String getServerIdForSession(String name, String dateLocal) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_SESSIONS, new String[]{S_COL_SERVER_ID}, S_COL_NAME + "=? AND " + S_COL_DATE + "=?",
                new String[]{name, dateLocal}, null, null, null);
        String id = null;
        if (c.moveToFirst()) id = c.getString(0);
        c.close();
        return id;
    }

    public void setVotes(String sessionName, String sessionDate, int yes, int no, int abstain) {
        ensureVoteRowExists(sessionName, sessionDate);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(V_COL_YES, yes);
        cv.put(V_COL_NO, no);
        cv.put(V_COL_ABSTAIN, abstain);
        db.update(TABLE_VOTES, cv, V_COL_SESSION_NAME + "=? AND " + V_COL_SESSION_DATE + "=?", new String[]{sessionName, sessionDate});
    }
}