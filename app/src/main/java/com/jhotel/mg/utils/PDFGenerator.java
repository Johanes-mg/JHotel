package com.jhotel.mg.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PDFGenerator {
    
    public static void generateReceipt(Context context, String nomClient, String numChambre, 
                                       int nbrJour, String dateEntree, String dateSortie) {
        try {
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File hotelDir = new File(documentsDir, "JHotel");
            if (!hotelDir.exists()) {
                hotelDir.mkdirs();
            }
            
            String fileName = "Recu_Sejour_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File file = new File(hotelDir, fileName);
            
            PdfDocument document = new PdfDocument();
            Paint paint = new Paint();
            Paint titlePaint = new Paint();
            titlePaint.setColor(Color.BLACK);
            titlePaint.setTextSize(24);
            titlePaint.setFakeBoldText(true);
            
            Paint contentPaint = new Paint();
            contentPaint.setColor(Color.BLACK);
            contentPaint.setTextSize(16);
            
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            android.graphics.Canvas canvas = page.getCanvas();
            
            int y = 100;
            
            canvas.drawText("=================================", 50, y, paint);
            y += 30;
            canvas.drawText("         JHotel - RECU", 50, y, titlePaint);
            y += 30;
            canvas.drawText("=================================", 50, y, paint);
            y += 50;
            
            canvas.drawText("Sejour N° " + System.currentTimeMillis(), 50, y, contentPaint);
            y += 40;
            
            canvas.drawLine(50, y, 545, y, paint);
            y += 30;
            
            canvas.drawText("Nom du Client : " + nomClient, 50, y, contentPaint);
            y += 30;
            canvas.drawText("Designation chambre : " + numChambre, 50, y, contentPaint);
            y += 30;
            canvas.drawText("Nombre de jour : " + nbrJour + " jours", 50, y, contentPaint);
            y += 30;
            canvas.drawText("Date d'entree : " + dateEntree, 50, y, contentPaint);
            y += 30;
            canvas.drawText("Date de sortie : " + dateSortie, 50, y, contentPaint);
            y += 50;
            
            canvas.drawLine(50, y, 545, y, paint);
            y += 30;
            
            canvas.drawText("Merci de votre visite !", 50, y, contentPaint);
            y += 30;
            canvas.drawText("Hotel JHotel", 50, y, contentPaint);
            
            document.finishPage(page);
            
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            Toast.makeText(context, "Reçu PDF genere dans Documents/JHotel/", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
