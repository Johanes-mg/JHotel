package com.jhotel.mg.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String TAG = "JHotel";
    private static final String DATABASE_NAME = "hotel.db";
    private static final int DATABASE_VERSION = 1;
    
    public static final String TABLE_SOLDE = "SOLDE";
    public static final String COL_SOLDE_ID = "id";
    public static final String COL_SOLDE_ACTUEL = "soldeActuel";
    
    public static final String TABLE_CHAMBRE = "CHAMBRE";
    public static final String COL_CHAMBRE_NUM = "numChambr";
    public static final String COL_CHAMBRE_DESIGN = "Design";
    public static final String COL_CHAMBRE_TYPE = "Type";
    public static final String COL_CHAMBRE_PRIX = "prixNuite";
    
    public static final String TABLE_RESERVER = "RESERVER";
    public static final String COL_RESERV_ID = "idreserv";
    public static final String COL_RESERV_NUM = "numChambr";
    public static final String COL_RESERV_DATE_RES = "dateReserv";
    public static final String COL_RESERV_DATE_ENTREE = "dateEntree";
    public static final String COL_RESERV_NBR_JOUR = "nbrJour";
    public static final String COL_RESERV_NOM = "nomClient";
    public static final String COL_RESERV_MAIL = "mail";
    
    public static final String TABLE_OCCUPER = "OCCUPER";
    public static final String COL_OCCUP_ID = "idOccup";
    public static final String COL_OCCUP_RESERV = "idreserv";
    
    public static final String TABLE_SEJOURNER = "SEJOURNER";
    public static final String COL_SEJOUR_ID = "idsejour";
    public static final String COL_SEJOUR_NUM = "numChambr";
    public static final String COL_SEJOUR_DATE_ENTREE = "dateEntreeSejour";
    public static final String COL_SEJOUR_NBR_JOUR = "nbrJour";
    public static final String COL_SEJOUR_NOM = "nomClient";
    public static final String COL_SEJOUR_TEL = "telephone";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSolde = "CREATE TABLE " + TABLE_SOLDE + "("
                + COL_SOLDE_ID + " INTEGER PRIMARY KEY,"
                + COL_SOLDE_ACTUEL + " INTEGER"
                + ")";
        db.execSQL(createSolde);
        
        String createChambre = "CREATE TABLE " + TABLE_CHAMBRE + "("
                + COL_CHAMBRE_NUM + " TEXT PRIMARY KEY,"
                + COL_CHAMBRE_DESIGN + " TEXT,"
                + COL_CHAMBRE_TYPE + " TEXT,"
                + COL_CHAMBRE_PRIX + " INTEGER"
                + ")";
        db.execSQL(createChambre);
        
        String createReserver = "CREATE TABLE " + TABLE_RESERVER + "("
                + COL_RESERV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_RESERV_NUM + " TEXT,"
                + COL_RESERV_DATE_RES + " TEXT,"
                + COL_RESERV_DATE_ENTREE + " TEXT,"
                + COL_RESERV_NBR_JOUR + " INTEGER,"
                + COL_RESERV_NOM + " TEXT,"
                + COL_RESERV_MAIL + " TEXT,"
                + "FOREIGN KEY(" + COL_RESERV_NUM + ") REFERENCES " 
                + TABLE_CHAMBRE + "(" + COL_CHAMBRE_NUM + ")"
                + ")";
        db.execSQL(createReserver);
        
        String createOccuper = "CREATE TABLE " + TABLE_OCCUPER + "("
                + COL_OCCUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_OCCUP_RESERV + " INTEGER,"
                + "FOREIGN KEY(" + COL_OCCUP_RESERV + ") REFERENCES " 
                + TABLE_RESERVER + "(" + COL_RESERV_ID + ")"
                + ")";
        db.execSQL(createOccuper);
        
        String createSejourner = "CREATE TABLE " + TABLE_SEJOURNER + "("
                + COL_SEJOUR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_SEJOUR_NUM + " TEXT,"
                + COL_SEJOUR_DATE_ENTREE + " TEXT,"
                + COL_SEJOUR_NBR_JOUR + " INTEGER,"
                + COL_SEJOUR_NOM + " TEXT,"
                + COL_SEJOUR_TEL + " TEXT,"
                + "FOREIGN KEY(" + COL_SEJOUR_NUM + ") REFERENCES " 
                + TABLE_CHAMBRE + "(" + COL_CHAMBRE_NUM + ")"
                + ")";
        db.execSQL(createSejourner);
        
        ContentValues values = new ContentValues();
        values.put(COL_SOLDE_ID, 1);
        values.put(COL_SOLDE_ACTUEL, 0);
        db.insert(TABLE_SOLDE, null, values);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEJOURNER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OCCUPER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAMBRE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOLDE);
        onCreate(db);
    }
    
    public long insertChambre(String num, String design, String type, int prix) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CHAMBRE_NUM, num);
        values.put(COL_CHAMBRE_DESIGN, design);
        values.put(COL_CHAMBRE_TYPE, type);
        values.put(COL_CHAMBRE_PRIX, prix);
        return db.insert(TABLE_CHAMBRE, null, values);
    }
    
    public Cursor getAllChambres() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CHAMBRE, 
            new String[]{COL_CHAMBRE_NUM + " as _id", COL_CHAMBRE_NUM, COL_CHAMBRE_DESIGN, COL_CHAMBRE_TYPE, COL_CHAMBRE_PRIX},
            null, null, null, null, COL_CHAMBRE_NUM + " ASC");
    }
    
    public int updateChambre(String num, String design, String type, int prix) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CHAMBRE_DESIGN, design);
        values.put(COL_CHAMBRE_TYPE, type);
        values.put(COL_CHAMBRE_PRIX, prix);
        return db.update(TABLE_CHAMBRE, values, COL_CHAMBRE_NUM + "=?", new String[]{num});
    }
    
    public int deleteChambre(String num) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CHAMBRE, COL_CHAMBRE_NUM + "=?", new String[]{num});
    }
    
    public long insertReservation(String numChambre, String dateReserv, String dateEntree, 
                                  int nbrJour, String nomClient, String mail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RESERV_NUM, numChambre);
        values.put(COL_RESERV_DATE_RES, dateReserv);
        values.put(COL_RESERV_DATE_ENTREE, dateEntree);
        values.put(COL_RESERV_NBR_JOUR, nbrJour);
        values.put(COL_RESERV_NOM, nomClient);
        values.put(COL_RESERV_MAIL, mail);
        return db.insert(TABLE_RESERVER, null, values);
    }
    
    public Cursor getAllReservations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RESERVER, 
            new String[]{COL_RESERV_ID + " as _id", COL_RESERV_ID, COL_RESERV_NUM, COL_RESERV_DATE_RES, 
                         COL_RESERV_DATE_ENTREE, COL_RESERV_NBR_JOUR, COL_RESERV_NOM, COL_RESERV_MAIL},
            null, null, null, null, COL_RESERV_ID + " DESC");
    }
    
    public int deleteReservation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_RESERVER, COL_RESERV_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public long insertOccuper(int idReserv) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_OCCUP_RESERV, idReserv);
        updateSolde(idReserv);
        return db.insert(TABLE_OCCUPER, null, values);
    }
    
    private void updateSolde(int idReserv) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT c." + COL_CHAMBRE_PRIX + ", r." + COL_RESERV_NBR_JOUR + 
                           " FROM " + TABLE_RESERVER + " r" +
                           " JOIN " + TABLE_CHAMBRE + " c ON r." + COL_RESERV_NUM + " = c." + COL_CHAMBRE_NUM +
                           " WHERE r." + COL_RESERV_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(idReserv)});
            if (cursor.moveToFirst()) {
                int prixNuite = cursor.getInt(0);
                int nbrJour = cursor.getInt(1);
                int montant = prixNuite * nbrJour;
                String updateSolde = "UPDATE " + TABLE_SOLDE + 
                                    " SET " + COL_SOLDE_ACTUEL + " = " + COL_SOLDE_ACTUEL + " + " + montant +
                                    " WHERE " + COL_SOLDE_ID + " = 1";
                db.execSQL(updateSolde);
                Log.d(TAG, "updateSolde: Ajout de " + montant + " Ar au solde");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    
    public Cursor getAllOccuper() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_OCCUPER, 
            new String[]{COL_OCCUP_ID + " as _id", COL_OCCUP_ID, COL_OCCUP_RESERV},
            null, null, null, null, COL_OCCUP_ID + " DESC");
    }
    
    public int deleteOccuper(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_OCCUPER, COL_OCCUP_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public long insertSejourner(String numChambre, String dateEntree, int nbrJour, 
                                String nomClient, String telephone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SEJOUR_NUM, numChambre);
        values.put(COL_SEJOUR_DATE_ENTREE, dateEntree);
        values.put(COL_SEJOUR_NBR_JOUR, nbrJour);
        values.put(COL_SEJOUR_NOM, nomClient);
        values.put(COL_SEJOUR_TEL, telephone);
        updateSoldeSejour(numChambre, nbrJour);
        return db.insert(TABLE_SEJOURNER, null, values);
    }
    
    private void updateSoldeSejour(String numChambre, int nbrJour) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT " + COL_CHAMBRE_PRIX + 
                           " FROM " + TABLE_CHAMBRE +
                           " WHERE " + COL_CHAMBRE_NUM + " = ?";
            cursor = db.rawQuery(query, new String[]{numChambre});
            if (cursor.moveToFirst()) {
                int prixNuite = cursor.getInt(0);
                int montant = prixNuite * nbrJour;
                String updateSolde = "UPDATE " + TABLE_SOLDE + 
                                    " SET " + COL_SOLDE_ACTUEL + " = " + COL_SOLDE_ACTUEL + " + " + montant +
                                    " WHERE " + COL_SOLDE_ID + " = 1";
                db.execSQL(updateSolde);
                Log.d(TAG, "updateSoldeSejour: Ajout de " + montant + " Ar au solde");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    
    public Cursor getAllSejourner() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SEJOURNER, 
            new String[]{COL_SEJOUR_ID + " as _id", COL_SEJOUR_ID, COL_SEJOUR_NUM, 
                         COL_SEJOUR_DATE_ENTREE, COL_SEJOUR_NBR_JOUR, COL_SEJOUR_NOM, COL_SEJOUR_TEL},
            null, null, null, null, COL_SEJOUR_ID + " DESC");
    }
    
    public int deleteSejourner(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SEJOURNER, COL_SEJOUR_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public boolean isChambreDisponible(String numChambre, String dateEntree, int nbrJour) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor1 = null;
        Cursor cursor2 = null;
        try {
            // Vérifier dans RESERVER
            String queryReserv = "SELECT * FROM " + TABLE_RESERVER + 
                                " WHERE " + COL_RESERV_NUM + " = ?" +
                                " AND (" + COL_RESERV_DATE_ENTREE + " <= ?" +
                                " AND date(" + COL_RESERV_DATE_ENTREE + ", '+' || " + COL_RESERV_NBR_JOUR + " || ' days') > ?)";
            cursor1 = db.rawQuery(queryReserv, new String[]{numChambre, dateEntree, dateEntree});
            
            // Vérifier dans SEJOURNER
            String querySejour = "SELECT * FROM " + TABLE_SEJOURNER + 
                                " WHERE " + COL_SEJOUR_NUM + " = ?" +
                                " AND (" + COL_SEJOUR_DATE_ENTREE + " <= ?" +
                                " AND date(" + COL_SEJOUR_DATE_ENTREE + ", '+' || " + COL_SEJOUR_NBR_JOUR + " || ' days') > ?)";
            cursor2 = db.rawQuery(querySejour, new String[]{numChambre, dateEntree, dateEntree});
            
            boolean disponible = (cursor1.getCount() == 0 && cursor2.getCount() == 0);
            if (!disponible) {
                Log.d(TAG, "isChambreDisponible: Chambre " + numChambre + " pas dispo pour " + dateEntree);
            }
            return disponible;
        } finally {
            if (cursor1 != null) cursor1.close();
            if (cursor2 != null) cursor2.close();
        }
    }
    
    public boolean isChambreDisponiblePourDate(String numChambre, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor1 = null;
        Cursor cursor2 = null;
        try {
            // Vérifier dans RESERVER
            String queryReserv = "SELECT * FROM " + TABLE_RESERVER + 
                                " WHERE " + COL_RESERV_NUM + " = ?" +
                                " AND " + COL_RESERV_DATE_ENTREE + " <= ?" +
                                " AND date(" + COL_RESERV_DATE_ENTREE + ", '+' || " + COL_RESERV_NBR_JOUR + " || ' days') > ?";
            cursor1 = db.rawQuery(queryReserv, new String[]{numChambre, date, date});
            
            // Vérifier dans SEJOURNER
            String querySejour = "SELECT * FROM " + TABLE_SEJOURNER + 
                                " WHERE " + COL_SEJOUR_NUM + " = ?" +
                                " AND " + COL_SEJOUR_DATE_ENTREE + " <= ?" +
                                " AND date(" + COL_SEJOUR_DATE_ENTREE + ", '+' || " + COL_SEJOUR_NBR_JOUR + " || ' days') > ?";
            cursor2 = db.rawQuery(querySejour, new String[]{numChambre, date, date});
            
            boolean disponible = (cursor1.getCount() == 0 && cursor2.getCount() == 0);
            if (!disponible) {
                Log.d(TAG, "isChambreDisponiblePourDate: Chambre " + numChambre + " pas dispo pour " + date);
                Log.d(TAG, "  - Reservations: " + cursor1.getCount());
                Log.d(TAG, "  - Sejours: " + cursor2.getCount());
            }
            return disponible;
        } finally {
            if (cursor1 != null) cursor1.close();
            if (cursor2 != null) cursor2.close();
        }
    }
    
    public Cursor getChambresLibres(String dateEntree, int nbrJour) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.* FROM " + TABLE_CHAMBRE + " c" +
                       " WHERE c." + COL_CHAMBRE_NUM + " NOT IN (" +
                       "   SELECT r." + COL_RESERV_NUM + " FROM " + TABLE_RESERVER + " r" +
                       "   WHERE (" + COL_RESERV_DATE_ENTREE + " <= ?" +
                       "   AND date(" + COL_RESERV_DATE_ENTREE + ", '+' || " + COL_RESERV_NBR_JOUR + " || ' days') > ?)" +
                       "   UNION" +
                       "   SELECT s." + COL_SEJOUR_NUM + " FROM " + TABLE_SEJOURNER + " s" +
                       "   WHERE (" + COL_SEJOUR_DATE_ENTREE + " <= ?" +
                       "   AND date(" + COL_SEJOUR_DATE_ENTREE + ", '+' || " + COL_SEJOUR_NBR_JOUR + " || ' days') > ?)" +
                       ") ORDER BY c." + COL_CHAMBRE_NUM + " ASC";
        return db.rawQuery(query, new String[]{dateEntree, dateEntree, dateEntree, dateEntree});
    }
    
    public int getSolde() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_SOLDE, null, COL_SOLDE_ID + "=?", 
                                     new String[]{"1"}, null, null, null);
            int solde = 0;
            if (cursor.moveToFirst()) {
                solde = cursor.getInt(cursor.getColumnIndex(COL_SOLDE_ACTUEL));
            }
            return solde;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
