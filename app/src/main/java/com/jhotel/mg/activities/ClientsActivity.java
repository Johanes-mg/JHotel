package com.jhotel.mg.activities;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.jhotel.mg.R;
import com.jhotel.mg.database.DatabaseHelper;
import com.jhotel.mg.utils.DateUtils;
import com.jhotel.mg.utils.EmailSender;
import com.jhotel.mg.utils.PDFGenerator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClientsActivity extends AppCompatActivity {
    
    private static final String TAG = "JHotel";
    private DatabaseHelper dbHelper;
    
    // 0 = Reservation, 1 = Occupation, 2 = Sejour
    private int currentMode = 0;
    
    private Button btnSwitchReservation, btnSwitchOccupation, btnSwitchSejour;
    private CardView cardReservation, cardOccupation, cardSejour;
    private CardView cardFormulaire;
    
    private Spinner spinnerChambres;
    private EditText etDateEntree, etNbrJour, etNomClient;
    private TextView tvPrixTotal;
    
    // Reservation
    private EditText etMail;
    private Button btnReserver, btnAnnulerReservation;
    private ListView listViewReservations;
    
    // Sejour
    private EditText etTelephone;
    private Button btnSejourner, btnSupprimerSejour, btnPDF;
    private ListView listViewSejours;
    
    // Occupation
    private Spinner spinnerReservationsOccuper;
    private Button btnOccuper, btnSupprimerOccupation;
    private ListView listViewOccupations;
    private ArrayAdapter<String> spinnerOccuperAdapter;
    
    private String selectedNumChambre = "";
    private int prixNuite = 0;
    private int selectedReservationId = -1;
    private int selectedSejourId = -1;
    private int selectedOccupationId = -1;
    private int selectedReservPosition = -1;
    private int selectedSejourPosition = -1;
    private int selectedOccupationPosition = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);
        
        dbHelper = new DatabaseHelper(this);
        
        btnSwitchReservation = findViewById(R.id.btnSwitchReservation);
        btnSwitchOccupation = findViewById(R.id.btnSwitchOccupation);
        btnSwitchSejour = findViewById(R.id.btnSwitchSejour);
        cardReservation = findViewById(R.id.cardReservation);
        cardOccupation = findViewById(R.id.cardOccupation);
        cardSejour = findViewById(R.id.cardSejour);
        cardFormulaire = findViewById(R.id.cardFormulaire);
        
        spinnerChambres = findViewById(R.id.spinnerChambresClients);
        etDateEntree = findViewById(R.id.etDateEntreeClients);
        etNbrJour = findViewById(R.id.etNbrJourClients);
        etNomClient = findViewById(R.id.etNomClientClients);
        tvPrixTotal = findViewById(R.id.tvPrixTotalClients);
        
        etMail = findViewById(R.id.etMailClients);
        btnReserver = findViewById(R.id.btnReserverClients);
        btnAnnulerReservation = findViewById(R.id.btnAnnulerReservationClients);
        listViewReservations = findViewById(R.id.listViewReservationsClients);
        
        etTelephone = findViewById(R.id.etTelephoneClients);
        btnSejourner = findViewById(R.id.btnSejournerClients);
        btnSupprimerSejour = findViewById(R.id.btnSupprimerSejourClients);
        btnPDF = findViewById(R.id.btnPDFClients);
        listViewSejours = findViewById(R.id.listViewSejoursClients);
        
        spinnerReservationsOccuper = findViewById(R.id.spinnerReservationsOccuper);
        btnOccuper = findViewById(R.id.btnOccuperClients);
        btnSupprimerOccupation = findViewById(R.id.btnSupprimerOccupationClients);
        listViewOccupations = findViewById(R.id.listViewOccupationsClients);
        
        SimpleDateFormat sdfFR = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        String dateJour = sdfFR.format(new Date());
        etDateEntree.setText(dateJour);
        
        etDateEntree.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().replaceAll("/", "");
                if (!text.equals(current)) {
                    current = text;
                    String formatted = "";
                    for (int i = 0; i < text.length() && i < 8; i++) {
                        formatted += text.charAt(i);
                        if ((i == 1 || i == 3) && text.length() > i + 1) {
                            formatted += "/";
                        }
                    }
                    etDateEntree.removeTextChangedListener(this);
                    etDateEntree.setText(formatted);
                    etDateEntree.setSelection(formatted.length());
                    etDateEntree.addTextChangedListener(this);
                }
            }
        });
        
        etNbrJour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculerPrixTotal();
            }
        });
        
        btnSwitchReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = 0;
                afficherMode();
            }
        });
        
        btnSwitchOccupation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = 1;
                afficherMode();
            }
        });
        
        btnSwitchSejour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMode = 2;
                afficherMode();
            }
        });
        
        chargerSpinnerChambres();
        
        spinnerChambres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerChambres.getSelectedItem().toString();
                if (selected != null && !selected.isEmpty() && !selected.equals("Aucune chambre")) {
                    selectedNumChambre = selected.split(" - ")[0];
                    recupererPrixChambre(selectedNumChambre);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        btnReserver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserver();
            }
        });
        
        btnAnnulerReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                annulerReservation();
            }
        });
        
        listViewReservations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedReservPosition == position) {
                    view.setSelected(false);
                    selectedReservPosition = -1;
                    selectedReservationId = -1;
                    btnAnnulerReservation.setEnabled(false);
                    return;
                }
                if (selectedReservPosition != -1) {
                    View prev = listViewReservations.getChildAt(selectedReservPosition - listViewReservations.getFirstVisiblePosition());
                    if (prev != null) { prev.setSelected(false); }
                }
                selectedReservPosition = position;
                view.setSelected(true);
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                selectedReservationId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_ID));
                btnAnnulerReservation.setEnabled(true);
            }
        });
        
        btnSejourner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sejourner();
            }
        });
        
        btnSupprimerSejour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supprimerSejour();
            }
        });
        
        btnPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genererPDF();
            }
        });
        
        listViewSejours.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedSejourPosition == position) {
                    view.setSelected(false);
                    selectedSejourPosition = -1;
                    selectedSejourId = -1;
                    btnSupprimerSejour.setEnabled(false);
                    btnPDF.setEnabled(false);
                    return;
                }
                if (selectedSejourPosition != -1) {
                    View prev = listViewSejours.getChildAt(selectedSejourPosition - listViewSejours.getFirstVisiblePosition());
                    if (prev != null) { prev.setSelected(false); }
                }
                selectedSejourPosition = position;
                view.setSelected(true);
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                selectedSejourId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_ID));
                btnSupprimerSejour.setEnabled(true);
                btnPDF.setEnabled(true);
            }
        });
        
        chargerSpinnerReservationsOccuper();
        
        btnOccuper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                occuper();
            }
        });
        
        btnSupprimerOccupation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supprimerOccupation();
            }
        });
        
        listViewOccupations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedOccupationPosition == position) {
                    view.setSelected(false);
                    selectedOccupationPosition = -1;
                    selectedOccupationId = -1;
                    btnSupprimerOccupation.setEnabled(false);
                    return;
                }
                if (selectedOccupationPosition != -1) {
                    View prev = listViewOccupations.getChildAt(selectedOccupationPosition - listViewOccupations.getFirstVisiblePosition());
                    if (prev != null) { prev.setSelected(false); }
                }
                selectedOccupationPosition = position;
                view.setSelected(true);
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                selectedOccupationId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_OCCUP_ID));
                btnSupprimerOccupation.setEnabled(true);
            }
        });
        
        afficherMode();
        chargerListes();
    }
    
    private void afficherMode() {
        btnSwitchReservation.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnSwitchReservation.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnSwitchOccupation.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnSwitchOccupation.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnSwitchSejour.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnSwitchSejour.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        cardReservation.setVisibility(View.GONE);
        cardOccupation.setVisibility(View.GONE);
        cardSejour.setVisibility(View.GONE);
        
        if (currentMode == 0) {
            btnSwitchReservation.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            btnSwitchReservation.setTextColor(getResources().getColor(android.R.color.white));
            cardReservation.setVisibility(View.VISIBLE);
            cardFormulaire.setVisibility(View.VISIBLE);
            etMail.setVisibility(View.VISIBLE);
            etTelephone.setVisibility(View.GONE);
            btnReserver.setText("Reserver");
        } else if (currentMode == 1) {
            btnSwitchOccupation.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            btnSwitchOccupation.setTextColor(getResources().getColor(android.R.color.white));
            cardOccupation.setVisibility(View.VISIBLE);
            cardFormulaire.setVisibility(View.GONE);
            etMail.setVisibility(View.GONE);
            etTelephone.setVisibility(View.GONE);
        } else {
            btnSwitchSejour.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
            btnSwitchSejour.setTextColor(getResources().getColor(android.R.color.white));
            cardSejour.setVisibility(View.VISIBLE);
            cardFormulaire.setVisibility(View.VISIBLE);
            etMail.setVisibility(View.GONE);
            etTelephone.setVisibility(View.VISIBLE);
            btnSejourner.setText("Enregistrer");
        }
        
        chargerListes();
    }
    
    private void recupererPrixChambre(String numChambre) {
        try {
            Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_CHAMBRE,
                new String[]{DatabaseHelper.COL_CHAMBRE_PRIX},
                DatabaseHelper.COL_CHAMBRE_NUM + "=?",
                new String[]{numChambre}, null, null, null);
            if (cursor.moveToFirst()) {
                prixNuite = cursor.getInt(0);
                calculerPrixTotal();
            } else {
                prixNuite = 0;
                tvPrixTotal.setText("Prix / nuit: 0 Ar");
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "recupererPrixChambre: Erreur", e);
            prixNuite = 0;
        }
    }
    
    private void calculerPrixTotal() {
        try {
            String nbrJourStr = etNbrJour.getText().toString().trim();
            if (!nbrJourStr.isEmpty() && prixNuite > 0) {
                int nbrJour = Integer.parseInt(nbrJourStr);
                int total = prixNuite * nbrJour;
                tvPrixTotal.setText("Prix total: " + total + " Ar");
            } else if (prixNuite > 0) {
                tvPrixTotal.setText("Prix / nuit: " + prixNuite + " Ar");
            } else {
                tvPrixTotal.setText("Prix / nuit: 0 Ar");
            }
        } catch (Exception e) {
            Log.e(TAG, "calculerPrixTotal: Erreur", e);
        }
    }
    
    private void chargerSpinnerChambres() {
        try {
            Cursor cursor = dbHelper.getAllChambres();
            String[] chambres = new String[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()) {
                chambres[i] = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_CHAMBRE_NUM)) 
                              + " - " + cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_CHAMBRE_DESIGN));
                i++;
            }
            cursor.close();
            if (chambres.length == 0) {
                chambres = new String[]{"Aucune chambre"};
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chambres);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerChambres.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "chargerSpinnerChambres: Erreur", e);
        }
    }
    
    private void chargerSpinnerReservationsOccuper() {
        try {
            Cursor cursor = dbHelper.getAllReservations();
            
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
                
                boolean isOccupied = occupiedIds.toString().contains("," + id + ",");
                if (isOccupied) {
                    reservations[i] = id + " - " + nom + " - Chambre " + num + " (DEJA OCCUPEE)";
                } else {
                    reservations[i] = id + " - " + nom + " - Chambre " + num;
                }
                i++;
            }
            cursor.close();
            
            if (reservations.length == 0) {
                reservations = new String[]{"Aucune reservation"};
            }
            
            spinnerOccuperAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, reservations);
            spinnerOccuperAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerReservationsOccuper.setAdapter(spinnerOccuperAdapter);
        } catch (Exception e) {
            Log.e(TAG, "chargerSpinnerReservationsOccuper: Erreur", e);
        }
    }
    
    private void chargerListes() {
        chargerReservations();
        chargerSejours();
        chargerOccupations();
        chargerSpinnerReservationsOccuper();
    }
    
    private void chargerReservations() {
        try {
            Cursor cursor = dbHelper.getAllReservations();
            String[] from = new String[]{DatabaseHelper.COL_RESERV_ID, DatabaseHelper.COL_RESERV_NOM,
                DatabaseHelper.COL_RESERV_NUM, DatabaseHelper.COL_RESERV_DATE_ENTREE,
                DatabaseHelper.COL_RESERV_NBR_JOUR};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, cursor, from,
                new int[]{android.R.id.text1, android.R.id.text2}, 0);
            listViewReservations.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "chargerReservations: Erreur", e);
        }
    }
    
    private void chargerSejours() {
        try {
            Cursor cursor = dbHelper.getAllSejourner();
            String[] from = new String[]{DatabaseHelper.COL_SEJOUR_ID, DatabaseHelper.COL_SEJOUR_NOM,
                DatabaseHelper.COL_SEJOUR_NUM, DatabaseHelper.COL_SEJOUR_DATE_ENTREE,
                DatabaseHelper.COL_SEJOUR_NBR_JOUR};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, cursor, from,
                new int[]{android.R.id.text1, android.R.id.text2}, 0);
            listViewSejours.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "chargerSejours: Erreur", e);
        }
    }
    
    private void chargerOccupations() {
        try {
            String query = "SELECT o." + DatabaseHelper.COL_OCCUP_ID + ", o." + DatabaseHelper.COL_OCCUP_RESERV + 
                           ", r." + DatabaseHelper.COL_RESERV_NOM + ", r." + DatabaseHelper.COL_RESERV_NUM + 
                           ", r." + DatabaseHelper.COL_RESERV_DATE_ENTREE + ", r." + DatabaseHelper.COL_RESERV_NBR_JOUR +
                           " FROM " + DatabaseHelper.TABLE_OCCUPER + " o" +
                           " JOIN " + DatabaseHelper.TABLE_RESERVER + " r ON o." + DatabaseHelper.COL_OCCUP_RESERV + " = r." + DatabaseHelper.COL_RESERV_ID +
                           " ORDER BY o." + DatabaseHelper.COL_OCCUP_ID + " DESC";
            
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, null);
            
            String[] from = new String[]{
                DatabaseHelper.COL_OCCUP_ID,
                DatabaseHelper.COL_RESERV_NOM,
                DatabaseHelper.COL_RESERV_NUM
            };
            
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, cursor, from,
                new int[]{android.R.id.text1, android.R.id.text2}, 0);
            listViewOccupations.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "chargerOccupations: Erreur", e);
        }
    }
    
    private void reserver() {
        try {
            String selected = spinnerChambres.getSelectedItem().toString();
            if (selected.equals("Aucune chambre")) {
                Toast.makeText(this, "Aucune chambre disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            String numChambre = selected.split(" - ")[0];
            String dateEntreeFR = etDateEntree.getText().toString().trim();
            String nbrJourStr = etNbrJour.getText().toString().trim();
            String nomClient = etNomClient.getText().toString().trim();
            String mail = etMail.getText().toString().trim();
            
            if (dateEntreeFR.isEmpty() || nbrJourStr.isEmpty() || nomClient.isEmpty() || mail.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!DateUtils.isValidDate(dateEntreeFR)) {
                Toast.makeText(this, "Date invalide", Toast.LENGTH_LONG).show();
                return;
            }
            
            int nbrJour = Integer.parseInt(nbrJourStr);
            String dateEntreeSQL = DateUtils.convertirDateFRtoSQL(dateEntreeFR);
            
            if (!dbHelper.isChambreDisponible(numChambre, dateEntreeSQL, nbrJour)) {
                Toast.makeText(this, "Chambre non disponible", Toast.LENGTH_LONG).show();
                return;
            }
            
            SimpleDateFormat sdfSQL = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateReserv = sdfSQL.format(new Date());
            
            long result = dbHelper.insertReservation(numChambre, dateReserv, dateEntreeSQL, nbrJour, nomClient, mail);
            if (result != -1) {
                Toast.makeText(this, "Reservation effectuee", Toast.LENGTH_SHORT).show();
                String dateSortieFR = DateUtils.calculerDateSortie(dateEntreeFR, nbrJour);
                EmailSender.sendReservationEmail(this, mail, numChambre, dateEntreeFR, nbrJour, nomClient, dateSortieFR);
                viderChamps();
                chargerListes();
            }
        } catch (Exception e) {
            Log.e(TAG, "reserver: Exception", e);
        }
    }
    
    private void annulerReservation() {
        if (selectedReservationId == -1) {
            Toast.makeText(this, "Selectionnez une reservation", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Annuler cette reservation ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                dbHelper.deleteReservation(selectedReservationId);
                Toast.makeText(this, "Reservation annulee", Toast.LENGTH_SHORT).show();
                selectedReservationId = -1;
                selectedReservPosition = -1;
                btnAnnulerReservation.setEnabled(false);
                chargerListes();
            })
            .setNegativeButton("Non", null).show();
    }
    
    private void sejourner() {
        try {
            String selected = spinnerChambres.getSelectedItem().toString();
            if (selected.equals("Aucune chambre")) {
                Toast.makeText(this, "Aucune chambre disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            String numChambre = selected.split(" - ")[0];
            String dateEntreeFR = etDateEntree.getText().toString().trim();
            String nbrJourStr = etNbrJour.getText().toString().trim();
            String nomClient = etNomClient.getText().toString().trim();
            String telephone = etTelephone.getText().toString().trim();
            
            if (dateEntreeFR.isEmpty() || nbrJourStr.isEmpty() || nomClient.isEmpty() || telephone.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!DateUtils.isValidDate(dateEntreeFR)) {
                Toast.makeText(this, "Date invalide", Toast.LENGTH_LONG).show();
                return;
            }
            
            int nbrJour = Integer.parseInt(nbrJourStr);
            String dateEntreeSQL = DateUtils.convertirDateFRtoSQL(dateEntreeFR);
            
            if (!dbHelper.isChambreDisponible(numChambre, dateEntreeSQL, nbrJour)) {
                Toast.makeText(this, "Chambre non disponible", Toast.LENGTH_LONG).show();
                return;
            }
            
            long result = dbHelper.insertSejourner(numChambre, dateEntreeSQL, nbrJour, nomClient, telephone);
            if (result != -1) {
                Toast.makeText(this, "Sejour enregistre", Toast.LENGTH_SHORT).show();
                viderChamps();
                chargerListes();
            }
        } catch (Exception e) {
            Log.e(TAG, "sejourner: Exception", e);
        }
    }
    
    private void supprimerSejour() {
        if (selectedSejourId == -1) {
            Toast.makeText(this, "Selectionnez un sejour", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Supprimer ce sejour ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                dbHelper.deleteSejourner(selectedSejourId);
                Toast.makeText(this, "Sejour supprime", Toast.LENGTH_SHORT).show();
                selectedSejourId = -1;
                selectedSejourPosition = -1;
                btnSupprimerSejour.setEnabled(false);
                btnPDF.setEnabled(false);
                chargerListes();
            })
            .setNegativeButton("Non", null).show();
    }
    
    private void genererPDF() {
        if (selectedSejourId == -1) {
            Toast.makeText(this, "Selectionnez un sejour", Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_SEJOURNER,
            null, DatabaseHelper.COL_SEJOUR_ID + "=?",
            new String[]{String.valueOf(selectedSejourId)}, null, null, null);
        if (cursor.moveToFirst()) {
            String nom = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_NOM));
            String num = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_NUM));
            int nbrJour = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_NBR_JOUR));
            String dateEntreeSQL = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_DATE_ENTREE));
            String dateEntreeFR = DateUtils.convertirDateSQLtoFR(dateEntreeSQL);
            String dateSortieFR = DateUtils.calculerDateSortie(dateEntreeFR, nbrJour);
            PDFGenerator.generateReceipt(this, nom, num, nbrJour, dateEntreeFR, dateSortieFR);
        }
        cursor.close();
    }
    
    private void occuper() {
        try {
            String selected = spinnerReservationsOccuper.getSelectedItem().toString();
            if (selected.equals("Aucune reservation")) {
                Toast.makeText(this, "Aucune reservation disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selected.contains("(DEJA OCCUPEE)")) {
                Toast.makeText(this, "Cette reservation est deja occupee", Toast.LENGTH_LONG).show();
                return;
            }
            
            int idReserv = Integer.parseInt(selected.split(" - ")[0]);
            
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_OCCUPER + 
                " WHERE " + DatabaseHelper.COL_OCCUP_RESERV + " = ?",
                new String[]{String.valueOf(idReserv)});
            
            if (cursor.getCount() > 0) {
                cursor.close();
                Toast.makeText(this, "Cette reservation est deja occupee", Toast.LENGTH_LONG).show();
                chargerListes();
                return;
            }
            cursor.close();
            
            long result = dbHelper.insertOccuper(idReserv);
            if (result != -1) {
                Toast.makeText(this, "Occupation enregistree", Toast.LENGTH_SHORT).show();
                chargerListes();
                selectedOccupationPosition = -1;
                selectedOccupationId = -1;
                btnSupprimerOccupation.setEnabled(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "occuper: Exception", e);
        }
    }
    
    private void supprimerOccupation() {
        if (selectedOccupationId == -1) {
            Toast.makeText(this, "Selectionnez une occupation", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Supprimer cette occupation ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                dbHelper.deleteOccuper(selectedOccupationId);
                Toast.makeText(this, "Occupation supprimee", Toast.LENGTH_SHORT).show();
                selectedOccupationId = -1;
                selectedOccupationPosition = -1;
                btnSupprimerOccupation.setEnabled(false);
                chargerListes();
            })
            .setNegativeButton("Non", null).show();
    }
    
    private void viderChamps() {
        etDateEntree.setText("");
        etNbrJour.setText("");
        etNomClient.setText("");
        etMail.setText("");
        etTelephone.setText("");
        spinnerChambres.setSelection(0);
        tvPrixTotal.setText("Prix / nuit: 0 Ar");
        prixNuite = 0;
        SimpleDateFormat sdfFR = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        etDateEntree.setText(sdfFR.format(new Date()));
    }
}
