package com.jhotel.mg.activities;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.jhotel.mg.R;
import com.jhotel.mg.database.DatabaseHelper;

public class ChambreActivity extends AppCompatActivity {
    
    private static final String TAG = "JHotel";
    private DatabaseHelper dbHelper;
    private ListView listViewChambres;
    private EditText etNumChambre, etDesign, etPrix;
    private Spinner spinnerType;
    private Button btnAjouter, btnModifier, btnSupprimer, btnListe;
    private String selectedNum = "";
    private int selectedPosition = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chambre);
        
        dbHelper = new DatabaseHelper(this);
        
        etNumChambre = findViewById(R.id.etNumChambre);
        etDesign = findViewById(R.id.etDesign);
        etPrix = findViewById(R.id.etPrix);
        spinnerType = findViewById(R.id.spinnerType);
        btnAjouter = findViewById(R.id.btnAjouter);
        btnModifier = findViewById(R.id.btnModifier);
        btnSupprimer = findViewById(R.id.btnSupprimer);
        btnListe = findViewById(R.id.btnListe);
        listViewChambres = findViewById(R.id.listViewChambres);
        
        String[] types = {"Choisir un type", "Standard", "Luxe", "Suite", "Economique", "Familiale", "Executive", "Studio"};
        ArrayAdapter<String> adapterTypes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapterTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterTypes);
        
        etNumChambre.setEnabled(true);
        viderChamps();
        chargerListe();
        
        listViewChambres.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (selectedPosition == position) {
                        view.setSelected(false);
                        view.setActivated(false);
                        view.setPressed(false);
                        selectedPosition = -1;
                        selectedNum = "";
                        viderChamps();
                        etNumChambre.setEnabled(true);
                        Log.d(TAG, "onItemClick: Deselection");
                        return;
                    }
                    
                    if (selectedPosition != -1) {
                        View prevView = listViewChambres.getChildAt(selectedPosition - listViewChambres.getFirstVisiblePosition());
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
                    selectedNum = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_CHAMBRE_NUM));
                    etNumChambre.setText(selectedNum);
                    etDesign.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_CHAMBRE_DESIGN)));
                    
                    String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_CHAMBRE_TYPE));
                    for (int i = 0; i < types.length; i++) {
                        if (types[i].equals(type)) {
                            spinnerType.setSelection(i);
                            break;
                        }
                    }
                    
                    etPrix.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COL_CHAMBRE_PRIX))));
                    etNumChambre.setEnabled(false);
                    Log.d(TAG, "onItemClick: Chambre selectionnee = " + selectedNum);
                } catch (Exception e) {
                    Log.e(TAG, "onItemClick: Erreur", e);
                }
            }
        });
        
        btnAjouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bouton Ajouter clique");
                etNumChambre.setEnabled(true);
                ajouterChambre();
            }
        });
        
        btnModifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bouton Modifier clique");
                modifierChambre();
            }
        });
        
        btnSupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bouton Supprimer clique");
                supprimerChambre();
            }
        });
        
        btnListe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Bouton Liste clique");
                chargerListe();
            }
        });
    }
    
    private void ajouterChambre() {
        try {
            String num = etNumChambre.getText().toString().trim();
            String design = etDesign.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();
            String prixStr = etPrix.getText().toString().trim();
            
            Log.d(TAG, "ajouterChambre: num=" + num + ", design=" + design + ", type=" + type + ", prix=" + prixStr);
            
            if (num.isEmpty() || design.isEmpty() || type.equals("Choisir un type") || prixStr.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int prix = Integer.parseInt(prixStr);
            long result = dbHelper.insertChambre(num, design, type, prix);
            
            if (result != -1) {
                Toast.makeText(this, "Chambre ajoutee avec succes", Toast.LENGTH_SHORT).show();
                viderChamps();
                chargerListe();
                etNumChambre.setEnabled(true);
                selectedPosition = -1;
                Log.d(TAG, "ajouterChambre: Chambre ajoutee ID=" + result);
            } else {
                Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "ajouterChambre: Erreur lors de l'insertion");
            }
        } catch (Exception e) {
            Log.e(TAG, "ajouterChambre: Exception", e);
        }
    }
    
    private void modifierChambre() {
        try {
            if (selectedNum.isEmpty()) {
                Toast.makeText(this, "Selectionnez une chambre", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String design = etDesign.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();
            String prixStr = etPrix.getText().toString().trim();
            
            if (design.isEmpty() || type.equals("Choisir un type") || prixStr.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int prix = Integer.parseInt(prixStr);
            int result = dbHelper.updateChambre(selectedNum, design, type, prix);
            
            if (result > 0) {
                Toast.makeText(this, "Chambre modifiee avec succes", Toast.LENGTH_SHORT).show();
                viderChamps();
                chargerListe();
                selectedNum = "";
                selectedPosition = -1;
                etNumChambre.setEnabled(true);
                Log.d(TAG, "modifierChambre: Chambre modifiee");
            } else {
                Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "modifierChambre: Erreur lors de la mise a jour");
            }
        } catch (Exception e) {
            Log.e(TAG, "modifierChambre: Exception", e);
        }
    }
    
    private void supprimerChambre() {
        if (selectedNum.isEmpty()) {
            Toast.makeText(this, "Selectionnez une chambre", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment supprimer cette chambre ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                int result = dbHelper.deleteChambre(selectedNum);
                if (result > 0) {
                    Toast.makeText(this, "Chambre supprimee", Toast.LENGTH_SHORT).show();
                    viderChamps();
                    chargerListe();
                    selectedNum = "";
                    selectedPosition = -1;
                    etNumChambre.setEnabled(true);
                    Log.d(TAG, "supprimerChambre: Chambre supprimee");
                }
            })
            .setNegativeButton("Non", null)
            .show();
    }
    
    private void chargerListe() {
        try {
            Cursor cursor = dbHelper.getAllChambres();
            Log.d(TAG, "chargerListe: Nombre de chambres = " + cursor.getCount());
            
            String[] from = new String[]{
                DatabaseHelper.COL_CHAMBRE_NUM,
                DatabaseHelper.COL_CHAMBRE_DESIGN,
                DatabaseHelper.COL_CHAMBRE_TYPE,
                DatabaseHelper.COL_CHAMBRE_PRIX
            };
            
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_chambre,
                cursor,
                from,
                new int[]{R.id.tvNumero, R.id.tvDesign, R.id.tvTypePrix},
                0
            );
            
            listViewChambres.setAdapter(adapter);
            listViewChambres.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            Log.d(TAG, "chargerListe: Liste chargee, " + cursor.getCount() + " elements");
        } catch (Exception e) {
            Log.e(TAG, "chargerListe: Erreur", e);
        }
    }
    
    private void viderChamps() {
        etNumChambre.setText("");
        etDesign.setText("");
        etPrix.setText("");
        spinnerType.setSelection(0);
        selectedNum = "";
        selectedPosition = -1;
        Log.d(TAG, "viderChamps: Champs vidés");
    }
}
