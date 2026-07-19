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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReservationActivity extends AppCompatActivity {
    
    private static final String TAG = "JHotel";
    private DatabaseHelper dbHelper;
    
    // Champs de saisie
    private EditText etDateEntree, etNbrJour, etNomClient, etMail;
    private TextView tvPrixTotal;
    private Spinner spinnerChambres;
    private ArrayAdapter<String> spinnerAdapter;
    
    // Listes
    private ListView listViewEnAttente;
    private ListView listViewOccupees;
    
    // Boutons
    private Button btnReserver, btnOccuper, btnSupprimerOccuper;
    private Button btnOngletEnAttente, btnOngletOccupees;
    
    // États
    private CardView cardEnAttente, cardOccupees;
    private int selectedReservationId = -1;
    private int selectedOccuperId = -1;
    private String selectedNumChambre = "";
    private int prixNuite = 0;
    private int selectedEnAttentePosition = -1;
    private int selectedOccupeesPosition = -1;
    private boolean showEnAttente = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_new);
        
        dbHelper = new DatabaseHelper(this);
        
        // Formulaire
        spinnerChambres = findViewById(R.id.spinnerChambres);
        etDateEntree = findViewById(R.id.etDateEntree);
        etNbrJour = findViewById(R.id.etNbrJour);
        etNomClient = findViewById(R.id.etNomClient);
        etMail = findViewById(R.id.etMail);
        tvPrixTotal = findViewById(R.id.tvPrixTotal);
        
        // Boutons
        btnReserver = findViewById(R.id.btnReserver);
        btnOccuper = findViewById(R.id.btnOccuper);
        btnSupprimerOccuper = findViewById(R.id.btnSupprimerOccuper);
        
        // Onglets
        btnOngletEnAttente = findViewById(R.id.btnOngletEnAttente);
        btnOngletOccupees = findViewById(R.id.btnOngletOccupees);
        cardEnAttente = findViewById(R.id.cardEnAttente);
        cardOccupees = findViewById(R.id.cardOccupees);
        
        // Listes
        listViewEnAttente = findViewById(R.id.listViewEnAttente);
        listViewOccupees = findViewById(R.id.listViewOccupees);
        
        // Mettre la date du jour par défaut
        SimpleDateFormat sdfFR = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        String dateJour = sdfFR.format(new Date());
        etDateEntree.setText(dateJour);
        
        // Formatage automatique de la date
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
        
        // Calcul automatique du prix
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
        
        chargerSpinnerChambres();
        chargerListes();
        
        // Sélection chambre
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
        
        // Bouton Réserver
        btnReserver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserver();
            }
        });
        
        // Bouton Occuper (depuis la liste)
        btnOccuper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                occuperReservation();
            }
        });
        
        // Bouton Supprimer Occupation
        btnSupprimerOccuper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supprimerOccupation();
            }
        });
        
        // Onglets
        btnOngletEnAttente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnAttente = true;
                afficherOnglet();
            }
        });
        
        btnOngletOccupees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnAttente = false;
                afficherOnglet();
            }
        });
        
        // Clic sur une réservation en attente
        listViewEnAttente.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedEnAttentePosition == position) {
                    view.setSelected(false);
                    view.setActivated(false);
                    selectedEnAttentePosition = -1;
                    selectedReservationId = -1;
                    btnOccuper.setEnabled(false);
                    return;
                }
                
                if (selectedEnAttentePosition != -1) {
                    View prevView = listViewEnAttente.getChildAt(selectedEnAttentePosition - listViewEnAttente.getFirstVisiblePosition());
                    if (prevView != null) {
                        prevView.setSelected(false);
                        prevView.setActivated(false);
                    }
                }
                
                selectedEnAttentePosition = position;
                view.setSelected(true);
                view.setActivated(true);
                
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                selectedReservationId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_RESERV_ID));
                btnOccuper.setEnabled(true);
                Toast.makeText(ReservationActivity.this, "Reservation " + selectedReservationId + " selectionnee", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Clic sur une occupation (liste occupées)
        listViewOccupees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedOccupeesPosition == position) {
                    view.setSelected(false);
                    view.setActivated(false);
                    selectedOccupeesPosition = -1;
                    selectedOccuperId = -1;
                    btnSupprimerOccuper.setEnabled(false);
                    return;
                }
                
                if (selectedOccupeesPosition != -1) {
                    View prevView = listViewOccupees.getChildAt(selectedOccupeesPosition - listViewOccupees.getFirstVisiblePosition());
                    if (prevView != null) {
                        prevView.setSelected(false);
                        prevView.setActivated(false);
                    }
                }
                
                selectedOccupeesPosition = position;
                view.setSelected(true);
                view.setActivated(true);
                
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                selectedOccuperId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_OCCUP_ID));
                btnSupprimerOccuper.setEnabled(true);
                Toast.makeText(ReservationActivity.this, "Occupation " + selectedOccuperId + " selectionnee", Toast.LENGTH_SHORT).show();
            }
        });
        
        afficherOnglet();
    }
    
    private void afficherOnglet() {
        if (showEnAttente) {
            btnOngletEnAttente.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            btnOngletEnAttente.setTextColor(getResources().getColor(android.R.color.white));
            btnOngletOccupees.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btnOngletOccupees.setTextColor(getResources().getColor(android.R.color.darker_gray));
            cardEnAttente.setVisibility(View.VISIBLE);
            cardOccupees.setVisibility(View.GONE);
        } else {
            btnOngletOccupees.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            btnOngletOccupees.setTextColor(getResources().getColor(android.R.color.white));
            btnOngletEnAttente.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btnOngletEnAttente.setTextColor(getResources().getColor(android.R.color.darker_gray));
            cardEnAttente.setVisibility(View.GONE);
            cardOccupees.setVisibility(View.VISIBLE);
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
            
            spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chambres);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerChambres.setAdapter(spinnerAdapter);
        } catch (Exception e) {
            Log.e(TAG, "chargerSpinnerChambres: Erreur", e);
        }
    }
    
    private void chargerListes() {
        chargerEnAttente();
        chargerOccupees();
    }
    
    private void chargerEnAttente() {
        try {
            Cursor cursor = dbHelper.getAllReservations();
            
            String[] from = new String[]{
                DatabaseHelper.COL_RESERV_ID,
                DatabaseHelper.COL_RESERV_NOM,
                DatabaseHelper.COL_RESERV_NUM,
                DatabaseHelper.COL_RESERV_DATE_ENTREE,
                DatabaseHelper.COL_RESERV_NBR_JOUR
            };
            
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                from,
                new int[]{android.R.id.text1, android.R.id.text2},
                0
            );
            
            listViewEnAttente.setAdapter(adapter);
            listViewEnAttente.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            Log.d(TAG, "chargerEnAttente: " + cursor.getCount() + " reservations en attente");
        } catch (Exception e) {
            Log.e(TAG, "chargerEnAttente: Erreur", e);
        }
    }
    
    private void chargerOccupees() {
        try {
            String query = "SELECT o." + DatabaseHelper.COL_OCCUP_ID + ", " +
                           "o." + DatabaseHelper.COL_OCCUP_RESERV + ", " +
                           "r." + DatabaseHelper.COL_RESERV_NOM + ", " +
                           "r." + DatabaseHelper.COL_RESERV_NUM + ", " +
                           "r." + DatabaseHelper.COL_RESERV_DATE_ENTREE + ", " +
                           "r." + DatabaseHelper.COL_RESERV_NBR_JOUR +
                           " FROM " + DatabaseHelper.TABLE_OCCUPER + " o" +
                           " JOIN " + DatabaseHelper.TABLE_RESERVER + " r ON o." + DatabaseHelper.COL_OCCUP_RESERV + " = r." + DatabaseHelper.COL_RESERV_ID +
                           " ORDER BY o." + DatabaseHelper.COL_OCCUP_ID + " DESC";
            
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, null);
            
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{
                    DatabaseHelper.COL_RESERV_NOM,
                    DatabaseHelper.COL_RESERV_NUM
                },
                new int[]{android.R.id.text1, android.R.id.text2},
                0
            );
            
            listViewOccupees.setAdapter(adapter);
            listViewOccupees.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            Log.d(TAG, "chargerOccupees: " + cursor.getCount() + " occupations");
        } catch (Exception e) {
            Log.e(TAG, "chargerOccupees: Erreur", e);
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
                Toast.makeText(this, "Date invalide. Format: jj/mm/aaaa", Toast.LENGTH_LONG).show();
                return;
            }
            
            int nbrJour = Integer.parseInt(nbrJourStr);
            String dateEntreeSQL = DateUtils.convertirDateFRtoSQL(dateEntreeFR);
            
            if (!dbHelper.isChambreDisponible(numChambre, dateEntreeSQL, nbrJour)) {
                Toast.makeText(this, "Cette chambre n'est pas disponible a cette date", Toast.LENGTH_LONG).show();
                return;
            }
            
            SimpleDateFormat sdfSQL = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateReserv = sdfSQL.format(new Date());
            
            long result = dbHelper.insertReservation(numChambre, dateReserv, dateEntreeSQL, nbrJour, nomClient, mail);
            
            if (result != -1) {
                Toast.makeText(this, "Reservation effectuee avec succes", Toast.LENGTH_SHORT).show();
                String dateSortieFR = DateUtils.calculerDateSortie(dateEntreeFR, nbrJour);
                EmailSender.sendReservationEmail(this, mail, numChambre, dateEntreeFR, nbrJour, nomClient, dateSortieFR);
                viderChamps();
                chargerListes();
            } else {
                Toast.makeText(this, "Erreur lors de la reservation", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "reserver: Exception", e);
        }
    }
    
    private void occuperReservation() {
        if (selectedReservationId == -1) {
            Toast.makeText(this, "Selectionnez une reservation", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT * FROM " + DatabaseHelper.TABLE_OCCUPER + 
            " WHERE " + DatabaseHelper.COL_OCCUP_RESERV + " = ?",
            new String[]{String.valueOf(selectedReservationId)});
        
        if (cursor.getCount() > 0) {
            cursor.close();
            Toast.makeText(this, "Cette reservation est deja occupee", Toast.LENGTH_LONG).show();
            return;
        }
        cursor.close();
        
        long result = dbHelper.insertOccuper(selectedReservationId);
        if (result != -1) {
            Toast.makeText(this, "Occupation enregistree avec succes", Toast.LENGTH_SHORT).show();
            selectedReservationId = -1;
            selectedEnAttentePosition = -1;
            btnOccuper.setEnabled(false);
            chargerListes();
        } else {
            Toast.makeText(this, "Erreur lors de l'occupation", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void supprimerOccupation() {
        if (selectedOccuperId == -1) {
            Toast.makeText(this, "Selectionnez une occupation", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment supprimer cette occupation ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                int result = dbHelper.deleteOccuper(selectedOccuperId);
                if (result > 0) {
                    Toast.makeText(this, "Occupation supprimee", Toast.LENGTH_SHORT).show();
                    selectedOccuperId = -1;
                    selectedOccupeesPosition = -1;
                    btnSupprimerOccuper.setEnabled(false);
                    chargerListes();
                }
            })
            .setNegativeButton("Non", null)
            .show();
    }
    
    private void viderChamps() {
        etDateEntree.setText("");
        etNbrJour.setText("");
        etNomClient.setText("");
        etMail.setText("");
        spinnerChambres.setSelection(0);
        tvPrixTotal.setText("Prix / nuit: 0 Ar");
        prixNuite = 0;
        
        SimpleDateFormat sdfFR = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        String dateJour = sdfFR.format(new Date());
        etDateEntree.setText(dateJour);
    }
}
