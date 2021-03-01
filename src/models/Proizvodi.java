package models;

import main.Data;

import java.util.ArrayList;

public class Proizvodi {
    private int id;
    private String naziv;
    private double cena;
    private String slika;
    private String opis;
    private int id_kategorije;
    private String kategorija;

    public Proizvodi(int id, String naziv, double cena, String slika, String opis, int id_kategorije) {
        this.id = id;
        this.naziv = naziv;
        this.cena = cena;
        this.slika = slika;
        this.opis = opis;
        this.id_kategorije = id_kategorije;
        kategorija = "Podrazumevana kategorija";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    public String getSlika() {
        return slika;
    }

    public void setSlika(String slika) {
        this.slika = slika;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public int getId_kategorije() {
        return id_kategorije;
    }

    public void setId_kategorije(int id_kategorije) {
        this.id_kategorije = id_kategorije;
    }

    public String getKategorija() {
        return kategorija;
    }

    public void setKategorija(String kategorija) { this.kategorija = kategorija; }

    public void ucitajKategoriju(String kategorije_putanja) {
        ArrayList<Kategorije> kategorije = new ArrayList<>(Data.readFromJsonKategorije(kategorije_putanja));

        for(Kategorije k: kategorije) {
            if(this.getId_kategorije() == k.getId()) {
                this.setKategorija(k.getIme_kategorije());
                break;
            }
        }
    }
}