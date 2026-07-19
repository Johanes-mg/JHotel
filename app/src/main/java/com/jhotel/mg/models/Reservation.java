package com.jhotel.mg.models;

public class Reservation {
    private int id;
    private String numChambre;
    private String dateReserv;
    private String dateEntree;
    private int nbrJour;
    private String nomClient;
    private String mail;
    
    public Reservation() {}
    
    public Reservation(int id, String numChambre, String dateReserv, String dateEntree, 
                       int nbrJour, String nomClient, String mail) {
        this.id = id;
        this.numChambre = numChambre;
        this.dateReserv = dateReserv;
        this.dateEntree = dateEntree;
        this.nbrJour = nbrJour;
        this.nomClient = nomClient;
        this.mail = mail;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumChambre() { return numChambre; }
    public void setNumChambre(String numChambre) { this.numChambre = numChambre; }
    public String getDateReserv() { return dateReserv; }
    public void setDateReserv(String dateReserv) { this.dateReserv = dateReserv; }
    public String getDateEntree() { return dateEntree; }
    public void setDateEntree(String dateEntree) { this.dateEntree = dateEntree; }
    public int getNbrJour() { return nbrJour; }
    public void setNbrJour(int nbrJour) { this.nbrJour = nbrJour; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
}
