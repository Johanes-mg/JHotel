package com.jhotel.mg.models;

public class Sejourner {
    private int idSejour;
    private String numChambre;
    private String dateEntree;
    private int nbrJour;
    private String nomClient;
    private String telephone;
    
    public Sejourner() {}
    
    public Sejourner(int idSejour, String numChambre, String dateEntree, 
                     int nbrJour, String nomClient, String telephone) {
        this.idSejour = idSejour;
        this.numChambre = numChambre;
        this.dateEntree = dateEntree;
        this.nbrJour = nbrJour;
        this.nomClient = nomClient;
        this.telephone = telephone;
    }
    
    public int getIdSejour() { return idSejour; }
    public void setIdSejour(int idSejour) { this.idSejour = idSejour; }
    public String getNumChambre() { return numChambre; }
    public void setNumChambre(String numChambre) { this.numChambre = numChambre; }
    public String getDateEntree() { return dateEntree; }
    public void setDateEntree(String dateEntree) { this.dateEntree = dateEntree; }
    public int getNbrJour() { return nbrJour; }
    public void setNbrJour(int nbrJour) { this.nbrJour = nbrJour; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
