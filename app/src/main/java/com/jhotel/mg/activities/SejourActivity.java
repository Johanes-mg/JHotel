package com.jhotel.mg.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.jhotel.mg.R;
import com.jhotel.mg.database.DatabaseHelper;
import com.jhotel.mg.utils.DateUtils;
import com.jhotel.mg.utils.PDFGenerator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SejourActivity extends AppCompatActivity {
    
    private static final String TAG = "JHotel";
    private static final int REQUEST_PERMISSION = 100;
    private DatabaseHelper dbHelper;
    private EditText etDateEntree, etNbrJour, etNomClient, etTelephone;
    private TextView tvPrixTotal;
    private Button btnSejourner, btnListe, btnSupprimer, btnPDF;
    private ListView listViewSejours;
    private Spinner spinnerChambres;
    private ArrayAdapter<String> spinnerAdapter;
    private int selectedSejourId = -1;
    private String selectedNumChambre = "";
    private int prixNuite = 0;
    private int selectedPosition = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sejour);
        
        dbHelper = new DatabaseHelper(this);
        
        spinnerChambres = findViewById(R.id.spinnerChambresSejour);
        etDateEntree = findViewById(R.id.etDateEntreeSejour);
        etNbrJour = findViewById(R.id.etNbrJourSejour);
        etNomClient = findViewById(R.id.etNomClientSejour);
        etTelephone = findViewById(R.id.etTelephoneSejour);
        tvPrixTotal = findViewById(R.id.tvPrixTotalSejour);
        btnSejourner = findViewById(R.id.btnSejourner);
        btnListe = findViewById(R.id.btnListeSejour);
        btnSupprimer = findViewById(R.id.btnSupprimerSejour);
        btnPDF = findViewById(R.id.btnPDF);
        listViewSejours = findViewById(R.id.listViewSejours);
        
        verifierPermission();
        
        // Mettre la date du jour par défaut (modifiable)
        SimpleDateFormat sdfFR = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        String dateJour = sdfFR.format(new Date());
        etDateEntree.setText(dateJour);
        
        // Formatage automatique des tirets
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
        
        chargerSpinnerChambres();
        chargerListe();
        
        spinnerChambres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerChambres.getSelectedItem().toString();
                if (selected != null && !selected.isEmpty()) {
                    selectedNumChambre = selected.split(" - ")[0];
                    recupererPrixChambre(selectedNumChambre);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        btnSejourner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sejourner();
            }
        });
        
        btnListe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chargerListe();
            }
        });
        
        btnSupprimer.setOnClickListener(new View.OnClickListener() {
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
                try {
                    if (selectedPosition == position) {
                        view.setSelected(false);
                        view.setActivated(false);
                        view.setPressed(false);
                        selectedPosition = -1;
                        selectedSejourId = -1;
                        viderChamps();
                        Log.d(TAG, "onItemClick: Deselection");
                        return;
                    }
                    
                    if (selectedPosition != -1) {
                        View prevView = listViewSejours.getChildAt(selectedPosition - listViewSejours.getFirstVisiblePosition());
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
                    selectedSejourId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_ID));
                    String num = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_NUM));
                    int nbrJour = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_NBR_JOUR));
                    String nom = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_NOM));
                    String tel = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_SEJOUR_TEL));
                    
                    for (int i = 0; i < spinnerChambres.getCount(); i++) {
                        if (spinnerChambres.getItemAtPosition(i).toString().startsWith(num)) {
                            spinnerChambres.setSelection(i);
                            break;
                        }
                    }
                    etNbrJour.setText(String.valueOf(nbrJour));
                    etNomClient.setText(nom);
                    etTelephone.setText(tel);
                    Log.d(TAG, "onItemClick: Sejour selectionne ID=" + selectedSejourId);
                } catch (Exception e) {
                    Log.e(TAG, "onItemClick: Erreur", e);
                }
            }
        });
    }
    
    private void verifierPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                    REQUEST_PERMISSION);
            }
        }
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
            
            spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chambres);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerChambres.setAdapter(spinnerAdapter);
        } catch (Exception e) {
            Log.e(TAG, "chargerSpinnerChambres: Erreur", e);
        }
    }
    
    private void sejourner() {
        try {
            String selected = spinnerChambres.getSelectedItem().toString();
            String numChambre = selected.split(" - ")[0];
            String dateEntreeFR = etDateEntree.getText().toString().trim();
            String nbrJourStr = etNbrJour.getText().toString().trim();
            String nomClient = etNomClient.getText().toString().trim();
            String telephone = etTelephone.getText().toString().trim();
            
            if (dateEntreeFR.isEmpty() || nbrJourStr.isEmpty() || nomClient.isEmpty() || telephone.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Valider le format de la date
            if (!DateUtils.isValidDate(dateEntreeFR)) {
                Toast.makeText(this, "Date invalide. Format: jj/mm/aaaa", Toast.LENGTH_LONG).show();
                return;
            }
            
            int nbrJour = Integer.parseInt(nbrJourStr);
            String dateEntreeSQL = DateUtils.convertirDateFRtoSQL(dateEntreeFR);
            
            if (!dbHelper.isChambreDisponible(numChambre, dateEntreeSQL, nbrJour)) {
                Toast.makeText(this, "Cette chambre n'est pas disponible a cette date", Toast.LENGTH_LONG).show();
                Log.d(TAG, "sejourner: Chambre " + numChambre + " non disponible");
                return;
            }
            
            long result = dbHelper.insertSejourner(numChambre, dateEntreeSQL, nbrJour, nomClient, telephone);
            
            if (result != -1) {
                Toast.makeText(this, "Sejour enregistre avec succes", Toast.LENGTH_SHORT).show();
                viderChamps();
                chargerListe();
            } else {
                Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
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
        
        int result = dbHelper.deleteSejourner(selectedSejourId);
        if (result > 0) {
            Toast.makeText(this, "Sejour supprime", Toast.LENGTH_SHORT).show();
            viderChamps();
            chargerListe();
            selectedSejourId = -1;
            selectedPosition = -1;
        }
    }
    
    private void genererPDF() {
        if (selectedSejourId == -1) {
            Toast.makeText(this, "Selectionnez un sejour", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_SEJOURNER,
            null,
            DatabaseHelper.COL_SEJOUR_ID + "=?",
            new String[]{String.valueOf(selectedSejourId)},
            null, null, null);
        
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
    
    private void chargerListe() {
        try {
            Cursor cursor = dbHelper.getAllSejourner();
            
            String[] from = new String[]{
                DatabaseHelper.COL_SEJOUR_ID,
                DatabaseHelper.COL_SEJOUR_NOM,
                DatabaseHelper.COL_SEJOUR_NUM,
                DatabaseHelper.COL_SEJOUR_DATE_ENTREE,
                DatabaseHelper.COL_SEJOUR_NBR_JOUR
            };
            
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_sejour,
                cursor,
                from,
                new int[]{R.id.tvId, R.id.tvNom, R.id.tvChambreDate, R.id.tvNbrJour},
                0
            );
            
            listViewSejours.setAdapter(adapter);
            listViewSejours.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        } catch (Exception e) {
            Log.e(TAG, "chargerListe: Erreur", e);
        }
    }
    
    private void viderChamps() {
        etDateEntree.setText("");
        etNbrJour.setText("");
        etNomClient.setText("");
        etTelephone.setText("");
        spinnerChambres.setSelection(0);
        selectedSejourId = -1;
        selectedPosition = -1;
        tvPrixTotal.setText("Prix / nuit: 0 Ar");
        prixNuite = 0;
        
        // Remettre la date du jour
        SimpleDateFormat sdfFR = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        String dateJour = sdfFR.format(new Date());
        etDateEntree.setText(dateJour);
    }
}
