package com.jhotel.mg.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class EmailSender {
    
    public static void sendReservationEmail(Context context, String to, String numChambre, 
                                            String dateEntree, int nbrJour, String nomClient,
                                            String dateSortie) {
        String subject = "Confirmation de reservation - JHotel";
        
        String body = "Bonjour " + nomClient + ",\n\n";
        body += "Votre reservation a ete confirmee.\n\n";
        body += "Details de la reservation :\n";
        body += "- Chambre : " + numChambre + "\n";
        body += "- Date d'entree : " + dateEntree + "\n";
        body += "- Nombre de jours : " + nbrJour + " jours\n";
        body += "- Date de sortie : " + dateSortie + "\n\n";
        body += "Merci de votre confiance.\n";
        body += "Hotel JHotel";
        
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + to));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        
        context.startActivity(Intent.createChooser(emailIntent, "Envoyer par email"));
    }
}
