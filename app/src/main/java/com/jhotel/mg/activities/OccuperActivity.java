package com.jhotel.mg.activities;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.jhotel.mg.R;
import com.jhotel.mg.database.DatabaseHelper;

public class OccuperActivity extends AppCompatActivity {
    
    private static final String TAG = "JHotel";
    private DatabaseHelper dbHelper;
    private Spinner spinnerReservations;
    private Button btnOccuper, btnListe, btnSupprimer;
    private ListView listViewOccuper;
    private ArrayAdapter<String> spinnerAdapter;
    private int selectedOccuperId = -1;
    private int selectedPosition = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_occuper);
        
        Log.d(TAG, "onCreate: OccuperActivity demarre");
        
        dbHelper = new DatabaseHelper(this);
        
        spinnerReservations = findViewById(R.id.spinnerReservations);
        btnOccuper = findViewById(R.id.btnOccuper);
        btnListe = findViewById(R.id.btnListeOccuper);
        btnSupprimer = findViewById(R.id.btnSupprimerOccuper);
        listViewOccuper = findViewById(R.id.listViewOccuper);
        
        chargerSpinnerReservations();
        chargerListe();
        
        btnOccuper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bouton Occuper clique");
                occuper();
            }
        });
        
        btnListe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bouton Liste clique");
                chargerListe();
            }
        });
        
        btnSupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bouton Supprimer clique");
                supprimerOccuper();
            }
        });
        
        listViewOccuper.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (selectedPosition == position) {
                        view.setSelected(false);
                        view.setActivated(false);
                        view.setPressed(false);
                        selectedPosition = -1;
                        selectedOccuperId = -1;
                        Log.d(TAG, "onItemClick: Deselection");
                        return;
                    }
                    
                    if (selectedPosition != -1) {
                        View prevView = listViewOccuper.getChildAt(selectedPosition - listViewOccuper.getFirstVisiblePosition());
                        if (prevView != null) {
                            prevView.setSelected(false);
                            prevView.setActivated(false);
                            prevView.setPressed(false);
                        }
                    }
                    
                    selectedPosition = position;
                    view.setSelected(true);
                    view.setActivated(true);
                    
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    selectedOccuperId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_OCCUP_ID));
                    int idReserv = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_OCCUP_RESERV));
                    
                    for (int i = 0; i < spinnerReservations.getCount(); i++) {
                        if (spinnerReservations.getItemAtPosition(i).toString().startsWith(String.valueOf(idReserv))) {
                            spinnerReservations.setSelection(i);
                            break;
                        }
                    }
                    Log.d(TAG, "onItemClick: Occupation selectionnee ID=" + selectedOccuperId);
                } catch (Exception e) {
                    Log.e(TAG, "onItemClick: Erreur", e);
                }
            }
        });
    }
    
    private void chargerSpinnerReservations() {
        try {
            Cursor cursor = dbHelper.getAllReservations();
            
            // Récupérer les IDs des réservations déjà occupées
            Cursor cursorOccup = dbHelper.getAllOccuper();
            StringBuilder occupiedIds = new StringBuilder();
            occupiedIds.append(",");
            while (cursorOccup.moveToNext()) {
                int idReserv = cursorOccup.getInt(cursorOccup.getColumnIndex(DatabaseHelper.COL_OCCUP_RESERV));
                occupiedIds.append(idReserv).append(",");
            }
            cursorOccup.close();
            
            String[] reservations = new String[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_ID));
                String nom = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_NOM));
                String num = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_NUM));
                
                // Vérifier si la réservation est déjà occupée
                boolean isOccupied = occupiedIds.toString().contains("," + id + ",");
                if (isOccupied) {
                    reservations[i] = id + " - " + nom + " - Chambre " + num + " (DEJA OCCUPEE)";
                } else {
                    reservations[i] = id + " - " + nom + " - Chambre " + num;
                }
                i++;
            }
            cursor.close();
            
            spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, reservations);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerReservations.setAdapter(spinnerAdapter);
            Log.d(TAG, "chargerSpinnerReservations: " + reservations.length + " reservations chargees");
        } catch (Exception e) {
            Log.e(TAG, "chargerSpinnerReservations: Erreur", e);
        }
    }
    
    private void occuper() {
        try {
            String selected = spinnerReservations.getSelectedItem().toString();
            
            // Vérifier si la réservation est déjà occupée
            if (selected.contains("(DEJA OCCUPEE)")) {
                Toast.makeText(this, "Cette reservation est deja occupee", Toast.LENGTH_LONG).show();
                return;
            }
            
            int idReserv = Integer.parseInt(selected.split(" - ")[0]);
            
            // Vérifier une nouvelle fois en base
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_OCCUPER + 
                " WHERE " + DatabaseHelper.COL_OCCUP_RESERV + " = ?",
                new String[]{String.valueOf(idReserv)});
            
            if (cursor.getCount() > 0) {
                cursor.close();
                Toast.makeText(this, "Cette reservation est deja occupee", Toast.LENGTH_LONG).show();
                chargerSpinnerReservations();
                return;
            }
            cursor.close();
            
            long result = dbHelper.insertOccuper(idReserv);
            
            if (result != -1) {
                Toast.makeText(this, "Occupation enregistree avec succes", Toast.LENGTH_SHORT).show();
                chargerListe();
                chargerSpinnerReservations();
                selectedPosition = -1;
                selectedOccuperId = -1;
                Log.d(TAG, "occuper: Occupation ajoutee ID=" + result);
            } else {
                Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "occuper: Erreur lors de l'insertion");
            }
        } catch (Exception e) {
            Log.e(TAG, "occuper: Exception", e);
        }
    }
    
    private void supprimerOccuper() {
        if (selectedOccuperId == -1) {
            Toast.makeText(this, "Selectionnez une occupation", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int result = dbHelper.deleteOccuper(selectedOccuperId);
        if (result > 0) {
            Toast.makeText(this, "Occupation supprimee", Toast.LENGTH_SHORT).show();
            chargerListe();
            chargerSpinnerReservations();
            selectedOccuperId = -1;
            selectedPosition = -1;
            Log.d(TAG, "supprimerOccuper: Occupation supprimee");
        }
    }
    
    private void chargerListe() {
        try {
            String query = "SELECT o." + DatabaseHelper.COL_OCCUP_ID + " as _id, " +
                           "o." + DatabaseHelper.COL_OCCUP_ID + ", " +
                           "o." + DatabaseHelper.COL_OCCUP_RESERV + ", " +
                           "r." + DatabaseHelper.COL_RESERV_NOM + ", " +
                           "r." + DatabaseHelper.COL_RESERV_NUM + ", " +
                           "r." + DatabaseHelper.COL_RESERV_DATE_ENTREE + ", " +
                           "r." + DatabaseHelper.COL_RESERV_NBR_JOUR +
                           " FROM " + DatabaseHelper.TABLE_OCCUPER + " o" +
                           " JOIN " + DatabaseHelper.TABLE_RESERVER + " r ON o." + DatabaseHelper.COL_OCCUP_RESERV + " = r." + DatabaseHelper.COL_RESERV_ID +
                           " ORDER BY o." + DatabaseHelper.COL_OCCUP_ID + " DESC";
            
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, null);
            
            CustomOccuperAdapter adapter = new CustomOccuperAdapter(this, cursor);
            listViewOccuper.setAdapter(adapter);
            listViewOccuper.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            Log.d(TAG, "chargerListe: Liste chargee, " + cursor.getCount() + " elements");
        } catch (Exception e) {
            Log.e(TAG, "chargerListe: Erreur", e);
        }
    }
    
    private class CustomOccuperAdapter extends CursorAdapter {
        
        public CustomOccuperAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.list_item_occuper, parent, false);
        }
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int idOccup = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_OCCUP_ID));
            int idReserv = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_OCCUP_RESERV));
            String nom = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_NOM));
            String num = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_NUM));
            String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_DATE_ENTREE));
            int nbrJour = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_NBR_JOUR));
            
            TextView tvId = view.findViewById(R.id.tvIdOccup);
            TextView tvReservation = view.findViewById(R.id.tvReservationInfo);
            TextView tvClient = view.findViewById(R.id.tvClientInfo);
            
            tvId.setText(String.valueOf(idOccup));
            tvReservation.setText("Reservation #" + idReserv + " - Chambre " + num);
            tvClient.setText(nom + " | " + date + " (" + nbrJour + " jours)");
        }
    }
}
