package com.jhotel.mg.utils;

import java.util.Calendar;

public class DateUtils {
    
    public static boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }
        
        try {
            String[] parts = date.split("/");
            int jour = Integer.parseInt(parts[0]);
            int mois = Integer.parseInt(parts[1]);
            int annee = Integer.parseInt(parts[2]);
            
            if (annee < 1900 || annee > 2100) return false;
            if (mois < 1 || mois > 12) return false;
            if (jour < 1 || jour > 31) return false;
            
            int[] joursParMois = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            if (estAnneeBissextile(annee)) {
                joursParMois[1] = 29;
            }
            
            return jour <= joursParMois[mois - 1];
            
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean estAnneeBissextile(int annee) {
        return (annee % 4 == 0 && annee % 100 != 0) || (annee % 400 == 0);
    }
    
    public static String convertirDateFRtoSQL(String dateFR) {
        if (!isValidDate(dateFR)) return null;
        try {
            String[] parts = dateFR.split("/");
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String convertirDateSQLtoFR(String dateSQL) {
        try {
            String[] parts = dateSQL.split("-");
            if (parts.length != 3) return dateSQL;
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {
            return dateSQL;
        }
    }
    
    public static String calculerDateSortie(String dateEntreeFR, int nbrJour) {
        try {
            String[] parts = dateEntreeFR.split("/");
            int jour = Integer.parseInt(parts[0]);
            int mois = Integer.parseInt(parts[1]);
            int annee = Integer.parseInt(parts[2]);
            
            Calendar cal = Calendar.getInstance();
            cal.set(annee, mois - 1, jour);
            cal.add(Calendar.DAY_OF_MONTH, nbrJour);
            
            int newJour = cal.get(Calendar.DAY_OF_MONTH);
            int newMois = cal.get(Calendar.MONTH) + 1;
            int newAnnee = cal.get(Calendar.YEAR);
            
            return String.format("%02d/%02d/%04d", newJour, newMois, newAnnee);
            
        } catch (Exception e) {
            return dateEntreeFR + " + " + nbrJour + " jours";
        }
    }
}
