package com.jhotel.mg.models;

public class Chambre {
    private String numChambre;
    private String design;
    private String type;
    private int prixNuite;
    
    public Chambre() {}
    
    public Chambre(String numChambre, String design, String type, int prixNuite) {
        this.numChambre = numChambre;
        this.design = design;
        this.type = type;
        this.prixNuite = prixNuite;
    }
    
    public String getNumChambre() { return numChambre; }
    public void setNumChambre(String numChambre) { this.numChambre = numChambre; }
    public String getDesign() { return design; }
    public void setDesign(String design) { this.design = design; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getPrixNuite() { return prixNuite; }
    public void setPrixNuite(int prixNuite) { this.prixNuite = prixNuite; }
}
