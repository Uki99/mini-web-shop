package models;

public class Kategorije {
    private int id;
    private String ime_kategorije;

    public Kategorije(int id, String ime_kategorije) {
        this.id = id;
        this.ime_kategorije = ime_kategorije;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIme_kategorije() {
        return ime_kategorije;
    }

    public void setIme_kategorije(String ime_kategorije) {
        this.ime_kategorije = ime_kategorije;
    }
}
