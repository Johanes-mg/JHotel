package com.jhotel.mg;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jhotel.mg.activities.ChambreActivity;
import com.jhotel.mg.activities.ClientsActivity;
import com.jhotel.mg.activities.StatistiquesActivity;
import com.jhotel.mg.database.DatabaseHelper;
import com.jhotel.mg.utils.DateUtils;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "JHotel";
    private DatabaseHelper dbHelper;
    private TextView tvSolde;
    private Button btnChambre, btnClients, btnRecherche, btnStatistiques;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dbHelper = new DatabaseHelper(this);
        
        tvSolde = findViewById(R.id.tvSolde);
        btnChambre = findViewById(R.id.btnChambre);
        btnClients = findViewById(R.id.btnClients);
        btnRecherche = findViewById(R.id.btnRecherche);
        btnStatistiques = findViewById(R.id.btnStatistiques);
        
        afficherSolde();
        
        btnChambre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChambreActivity.class));
            }
        });
        
        btnClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ClientsActivity.class));
            }
        });
        
        btnRecherche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bouton Recherche clique");
                afficherDialogueRecherche();
            }
        });
        
        btnStatistiques.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StatistiquesActivity.class));
            }
        });
    }
    
    private void afficherDialogueRecherche() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rechercher Chambre Libre");
        
        View view = getLayoutInflater().inflate(R.layout.dialog_recherche, null);
        final EditText etDate = view.findViewById(R.id.etDateRecherche);
        
        etDate.addTextChangedListener(new TextWatcher() {
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
                    etDate.removeTextChangedListener(this);
                    etDate.setText(formatted);
                    etDate.setSelection(formatted.length());
                    etDate.addTextChangedListener(this);
                }
            }
        });
        
        builder.setView(view);
        
        builder.setPositiveButton("Rechercher", (dialog, which) -> {
            String dateFR = etDate.getText().toString().trim();
            
            if (dateFR.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer une date", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!DateUtils.isValidDate(dateFR)) {
                Toast.makeText(this, "Date invalide. Veuillez entrer une date valide (jj/mm/aaaa)", Toast.LENGTH_LONG).show();
                return;
            }
            
            String dateSQL = DateUtils.convertirDateFRtoSQL(dateFR);
            if (dateSQL == null) {
                Toast.makeText(this, "Erreur de conversion de date", Toast.LENGTH_SHORT).show();
                return;
            }
            
            rechercherChambresLibres(dateFR, dateSQL);
        });
        
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
    
    private void rechercherChambresLibres(String dateFR, String dateSQL) {
        Cursor allChambres = dbHelper.getAllChambres();
        
        if (allChambres.getCount() == 0) {
            Toast.makeText(this, "Aucune chambre enregistree", Toast.LENGTH_LONG).show();
            allChambres.close();
            return;
        }
        
        StringBuilder resultat = new StringBuilder();
        resultat.append("=== CHAMBRES DISPONIBLES ===\n\n");
        resultat.append("Date recherchee : " + dateFR + "\n\n");
        boolean found = false;
        
        while (allChambres.moveToNext()) {
            String num = allChambres.getString(allChambres.getColumnIndex(DatabaseHelper.COL_CHAMBRE_NUM));
            String design = allChambres.getString(allChambres.getColumnIndex(DatabaseHelper.COL_CHAMBRE_DESIGN));
            String type = allChambres.getString(allChambres.getColumnIndex(DatabaseHelper.COL_CHAMBRE_TYPE));
            int prix = allChambres.getInt(allChambres.getColumnIndex(DatabaseHelper.COL_CHAMBRE_PRIX));
            
            boolean disponible = dbHelper.isChambreDisponiblePourDate(num, dateSQL);
            
            if (disponible) {
                found = true;
                resultat.append("LIBRE - " + num + " : " + design + " (" + type + ") - " + prix + " Ar\n");
            } else {
                resultat.append("OCCUPEE - " + num + " : " + design + "\n");
                resultat.append(getOccupationDetails(num, dateSQL));
                resultat.append("\n");
            }
        }
        allChambres.close();
        
        if (!found) {
            resultat.append("\nAucune chambre libre a cette date.\n");
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Resultat de la recherche")
            .setMessage(resultat.toString())
            .setPositiveButton("OK", null)
            .setCancelable(true)
            .show();
    }
    
    private String getOccupationDetails(String numChambre, String dateSQL) {
        StringBuilder details = new StringBuilder();
        
        Cursor cursorReserv = dbHelper.getReadableDatabase().rawQuery(
            "SELECT nomClient, dateEntree, nbrJour, mail FROM " + DatabaseHelper.TABLE_RESERVER +
            " WHERE " + DatabaseHelper.COL_RESERV_NUM + " = ?" +
            " AND " + DatabaseHelper.COL_RESERV_DATE_ENTREE + " <= ?" +
            " AND date(" + DatabaseHelper.COL_RESERV_DATE_ENTREE + ", '+' || " + 
            DatabaseHelper.COL_RESERV_NBR_JOUR + " || ' days') > ?",
            new String[]{numChambre, dateSQL, dateSQL});
        
        if (cursorReserv.moveToFirst()) {
            String nom = cursorReserv.getString(0);
            String dateEntreeSQL = cursorReserv.getString(1);
            int nbrJour = cursorReserv.getInt(2);
            String mail = cursorReserv.getString(3);
            
            String dateEntreeFR = DateUtils.convertirDateSQLtoFR(dateEntreeSQL);
            
            details.append("  - Reservation de " + nom + " (email: " + mail + ")\n");
            details.append("  - Du " + dateEntreeFR + " pour " + nbrJour + " jours\n");
        }
        cursorReserv.close();
        
        Cursor cursorSejour = dbHelper.getReadableDatabase().rawQuery(
            "SELECT nomClient, dateEntreeSejour, nbrJour, telephone FROM " + DatabaseHelper.TABLE_SEJOURNER +
            " WHERE " + DatabaseHelper.COL_SEJOUR_NUM + " = ?" +
            " AND " + DatabaseHelper.COL_SEJOUR_DATE_ENTREE + " <= ?" +
            " AND date(" + DatabaseHelper.COL_SEJOUR_DATE_ENTREE + ", '+' || " + 
            DatabaseHelper.COL_SEJOUR_NBR_JOUR + " || ' days') > ?",
            new String[]{numChambre, dateSQL, dateSQL});
        
        if (cursorSejour.moveToFirst()) {
            String nom = cursorSejour.getString(0);
            String dateEntreeSQL = cursorSejour.getString(1);
            int nbrJour = cursorSejour.getInt(2);
            String tel = cursorSejour.getString(3);
            
            String dateEntreeFR = DateUtils.convertirDateSQLtoFR(dateEntreeSQL);
            
            details.append("  - Sejour de " + nom + " (tel: " + tel + ")\n");
            details.append("  - Du " + dateEntreeFR + " pour " + nbrJour + " jours\n");
        }
        cursorSejour.close();
        
        if (details.length() == 0) {
            details.append("  - Information non disponible\n");
        }
        
        return details.toString();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        afficherSolde();
    }
    
    private void afficherSolde() {
        int solde = dbHelper.getSolde();
        tvSolde.setText(solde + " Ar");
    }
}
