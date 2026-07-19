package com.jhotel.mg.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.jhotel.mg.R;
import com.jhotel.mg.database.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatistiquesActivity extends AppCompatActivity {
    
    private static final String TAG = "JHotel";
    private DatabaseHelper dbHelper;
    private TextView tvSoldeActuel;
    private LinearLayout layoutMensuel;
    private Button btnReinitialiserSolde;
    private TextView tvAucuneDonnee;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistiques);
        
        dbHelper = new DatabaseHelper(this);
        
        tvSoldeActuel = findViewById(R.id.tvSoldeActuel);
        layoutMensuel = findViewById(R.id.layoutMensuel);
        btnReinitialiserSolde = findViewById(R.id.btnReinitialiserSolde);
        tvAucuneDonnee = findViewById(R.id.tvAucuneDonnee);
        
        afficherStatistiques();
        
        btnReinitialiserSolde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reinitialiserSolde();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        afficherStatistiques();
    }
    
    private void afficherStatistiques() {
        int solde = dbHelper.getSolde();
        tvSoldeActuel.setText(solde + " Ar");
        
        layoutMensuel.removeAllViews();
        
        // Requête corrigée avec les bons noms de colonnes
        String queryMois = "SELECT DISTINCT strftime('%m/%Y', dateEntreeSejour) as mois " +
                           "FROM " + DatabaseHelper.TABLE_SEJOURNER +
                           " UNION " +
                           "SELECT DISTINCT strftime('%m/%Y', dateEntree) as mois " +
                           "FROM " + DatabaseHelper.TABLE_RESERVER +
                           " WHERE idreserv IN (SELECT idreserv FROM " + DatabaseHelper.TABLE_OCCUPER + ")" +
                           " ORDER BY mois DESC";
        
        Cursor cursorMois = dbHelper.getReadableDatabase().rawQuery(queryMois, null);
        
        if (cursorMois.getCount() == 0) {
            tvAucuneDonnee.setVisibility(View.VISIBLE);
            cursorMois.close();
            return;
        }
        
        tvAucuneDonnee.setVisibility(View.GONE);
        
        while (cursorMois.moveToNext()) {
            String mois = cursorMois.getString(0);
            
            String[] parts = mois.split("/");
            String moisNum = parts[0];
            String annee = parts[1];
            
            String dateDebut = annee + "-" + moisNum + "-01";
            
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(annee), Integer.parseInt(moisNum) - 1, 1);
            int dernierJour = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            String dateFin = annee + "-" + moisNum + "-" + String.format("%02d", dernierJour);
            
            int gainMois = calculerGainPeriode(dateDebut, dateFin);
            
            if (gainMois > 0) {
                creerCarteMois(mois, gainMois, dateDebut, dateFin);
            }
        }
        cursorMois.close();
        
        if (layoutMensuel.getChildCount() == 0) {
            tvAucuneDonnee.setVisibility(View.VISIBLE);
        }
        
        Log.d(TAG, "Statistiques affichees");
    }
    
    private void creerCarteMois(String mois, int gainMois, String dateDebut, String dateFin) {
        CardView card = new CardView(this);
        CardView.LayoutParams params = new CardView.LayoutParams(
            CardView.LayoutParams.MATCH_PARENT,
            CardView.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setRadius(12f);
        card.setCardElevation(4f);
        card.setUseCompatPadding(true);
        
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(48, 32, 48, 32);
        innerLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        TextView tvMois = new TextView(this);
        tvMois.setText(mois);
        tvMois.setTextSize(18f);
        tvMois.setTextColor(getResources().getColor(android.R.color.black));
        tvMois.setLayoutParams(new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ));
        
        TextView tvTotalMois = new TextView(this);
        tvTotalMois.setText(gainMois + " Ar");
        tvTotalMois.setTextSize(18f);
        tvTotalMois.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        tvTotalMois.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        row1.addView(tvMois);
        row1.addView(tvTotalMois);
        innerLayout.addView(row1);
        
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.FRANCE);
        String moisActuel = sdf.format(now.getTime());
        
        if (mois.equals(moisActuel)) {
            SimpleDateFormat sdfJour = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String aujourdhui = sdfJour.format(new Date());
            int gainJour = calculerGainPeriode(aujourdhui, aujourdhui);
            
            if (gainJour > 0) {
                LinearLayout row2 = new LinearLayout(this);
                row2.setOrientation(LinearLayout.HORIZONTAL);
                row2.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                row2.setPadding(0, 16, 0, 8);
                
                TextView tvLabel1 = new TextView(this);
                tvLabel1.setText("Aujourd'hui");
                tvLabel1.setTextSize(14f);
                tvLabel1.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tvLabel1.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ));
                
                TextView tvJour = new TextView(this);
                tvJour.setText(gainJour + " Ar");
                tvJour.setTextSize(14f);
                tvJour.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                tvJour.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                
                row2.addView(tvLabel1);
                row2.addView(tvJour);
                innerLayout.addView(row2);
            }
            
            // Semaine
            String dateSemaineDebut = dateDebut;
            String dateSemaineFin = dateFin;
            int gainSemaine = calculerGainPeriode(dateSemaineDebut, dateSemaineFin);
            if (gainSemaine > 0 && gainSemaine != gainMois) {
                LinearLayout row3 = new LinearLayout(this);
                row3.setOrientation(LinearLayout.HORIZONTAL);
                row3.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                row3.setPadding(0, 0, 0, 8);
                
                TextView tvLabel2 = new TextView(this);
                tvLabel2.setText("Cette semaine");
                tvLabel2.setTextSize(14f);
                tvLabel2.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tvLabel2.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ));
                
                TextView tvSemaine = new TextView(this);
                tvSemaine.setText(gainSemaine + " Ar");
                tvSemaine.setTextSize(14f);
                tvSemaine.setTextColor(getResources().getColor(android.R.color.holo_purple));
                tvSemaine.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                
                row3.addView(tvLabel2);
                row3.addView(tvSemaine);
                innerLayout.addView(row3);
            }
        }
        
        card.addView(innerLayout);
        layoutMensuel.addView(card);
    }
    
    private int calculerGainPeriode(String dateDebut, String dateFin) {
        int total = 0;
        
        // Séjours - utilise dateEntreeSejour
        String query = "SELECT SUM(" + DatabaseHelper.COL_CHAMBRE_PRIX + " * " + DatabaseHelper.COL_SEJOUR_NBR_JOUR + ") " +
                       "FROM " + DatabaseHelper.TABLE_SEJOURNER + " s " +
                       "JOIN " + DatabaseHelper.TABLE_CHAMBRE + " c ON s." + DatabaseHelper.COL_SEJOUR_NUM + " = c." + DatabaseHelper.COL_CHAMBRE_NUM +
                       " WHERE s." + DatabaseHelper.COL_SEJOUR_DATE_ENTREE + " BETWEEN ? AND ?";
        
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, new String[]{dateDebut, dateFin});
        if (cursor.moveToFirst()) {
            total += cursor.getInt(0);
        }
        cursor.close();
        
        // Occupations - utilise dateEntree de RESERVER
        String query2 = "SELECT SUM(c." + DatabaseHelper.COL_CHAMBRE_PRIX + " * r." + DatabaseHelper.COL_RESERV_NBR_JOUR + ") " +
                        "FROM " + DatabaseHelper.TABLE_OCCUPER + " o " +
                        "JOIN " + DatabaseHelper.TABLE_RESERVER + " r ON o." + DatabaseHelper.COL_OCCUP_RESERV + " = r." + DatabaseHelper.COL_RESERV_ID +
                        " JOIN " + DatabaseHelper.TABLE_CHAMBRE + " c ON r." + DatabaseHelper.COL_RESERV_NUM + " = c." + DatabaseHelper.COL_CHAMBRE_NUM +
                        " WHERE r." + DatabaseHelper.COL_RESERV_DATE_ENTREE + " BETWEEN ? AND ?";
        
        Cursor cursor2 = dbHelper.getReadableDatabase().rawQuery(query2, new String[]{dateDebut, dateFin});
        if (cursor2.moveToFirst()) {
            total += cursor2.getInt(0);
        }
        cursor2.close();
        
        return total;
    }
    
    private void reinitialiserSolde() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment reinitialiser le solde a zero ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                dbHelper.getWritableDatabase().execSQL(
                    "UPDATE " + DatabaseHelper.TABLE_SOLDE + 
                    " SET " + DatabaseHelper.COL_SOLDE_ACTUEL + " = 0" +
                    " WHERE " + DatabaseHelper.COL_SOLDE_ID + " = 1"
                );
                
                dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_OCCUPER);
                dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_SEJOURNER);
                
                Toast.makeText(StatistiquesActivity.this, "Solde reinitialise a 0", Toast.LENGTH_SHORT).show();
                afficherStatistiques();
                Log.d(TAG, "Solde reinitialise a 0");
            })
            .setNegativeButton("Non", null)
            .show();
    }
}
