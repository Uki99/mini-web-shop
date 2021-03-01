package main;

import static spark.Spark.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import com.google.gson.Gson;
import models.*;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

public class Launcher {
    @SuppressWarnings("DuplicatedCode")
    public static void main(String[] args) {
        //staticFiles.location("/public");
        staticFiles.externalLocation("src/public"); //Koristiti staticFiles.externalLocation() samo u razvojnom okruženju
        port(9000);

        String proizvodi_putanja = "proizvodi.json";
        String kategorije_putanja = "kategorije.json";

        HashMap<String, Object> polja = new HashMap<>();

        get("/", (request, response) -> {
            //Radi performansa u budućnosti učitavati proizvode što ređe zato što će baze možda biti ogromne
            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));

            //Povezivanje tabele kategorija i proizvoda. Ne može se pozvati u konstruktoru svakog proizvoda zbog načina na koji se čita JSON.
            for (Proizvodi p : proizvodi) {
                p.ucitajKategoriju(kategorije_putanja);
            }

            //Provera da li sam admin radi prikazivanja dodatnih opcija za admina
            if (request.session().attribute("user") != null) {
                polja.put("admin", "admin");
            }

            //Hash mapa koja se šalje index-u
            polja.put("proizvodi", proizvodi);

            return new ModelAndView(polja, "index.hbs");
        }, new HandlebarsTemplateEngine());

        post("/pretraga", (request, response) -> {
            //Ukoliko je response.type() tekst, onda je u JS-u potrebno parsirati odgovor u JSON. Ukoliko stavimo da odgovor vraćamo kao JSON, taj korak u JS-u nije potreban
            response.type("text/text");

            String zahtev = request.queryParams("query");

            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));
            ArrayList<Proizvodi> filtrirani_proizvodi = new ArrayList<>();

            for (Proizvodi p : proizvodi) {
                if (p.getNaziv().toLowerCase().contains(zahtev.toLowerCase()) || p.getOpis().toLowerCase().contains(zahtev.toLowerCase())) {
                    p.ucitajKategoriju(kategorije_putanja);
                    filtrirani_proizvodi.add(p);
                }
            }

            Gson gson = new Gson();
            return gson.toJson(filtrirani_proizvodi);
        });

        post("/filtriraj", (request, response) -> {
            response.type("text/text");
            String filter = request.queryParams("filter");

            Gson gson = new Gson();

            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));

            for (Proizvodi p : proizvodi) {
                p.ucitajKategoriju(kategorije_putanja);
            }

            switch (filter) {
                case "cena_rastuce": {
                    proizvodi.sort((o1, o2) -> (int) (o1.getCena() - o2.getCena()));

                    break;
                }
                case "cena_opadajuce": {
                    proizvodi.sort((o1, o2) -> (int) (o1.getCena() - o2.getCena()));

                    Collections.reverse(proizvodi);

                    break;
                }
                case "naziv_rastuce": {
                    proizvodi.sort((o1, o2) -> o1.getNaziv().compareToIgnoreCase(o2.getNaziv()));

                    break;
                }
                case "naziv_opadajuce": {
                    proizvodi.sort((o1, o2) -> o1.getNaziv().compareToIgnoreCase(o2.getNaziv()));

                    Collections.reverse(proizvodi);

                    break;
                }
                default:
                    return gson.toJson(new ArrayList<Proizvodi>());
            }

            return gson.toJson(proizvodi);
        });

        post("/login", (request, response) -> {
            String username = request.queryParams("korisnicko_ime");
            String password = request.queryParams("lozinka");

            // Statičko predefinisano korisničko ime i lozinka. Veoma nesigurno lol, treba kriptovati i koristiti druge tehnike.
            if (username.equals("admin") && password.equals("admin")) {
                request.session().attribute("user", "Admin");
                response.redirect("/");
                return null;
            }

            response.redirect("/");
            return null;
        });

        get("/logout", (request, response) -> {
            request.session().removeAttribute("user");
            polja.remove("admin");
            response.redirect("/");
            return null;
        });

        get("/adminPanel", (request, response) -> {
            //Redirekcija na početnu stranu ukoliko običan korisnik pokuša da pristupi admin panelu
            if (request.session().attribute("user") == null) {
                response.redirect("/");
                return null;
            }

            polja.remove("poruka");

            //if blok zadužen za proveru postojanja poruke i njeno uklanjanje
            if(polja.containsKey("poruka_za_prikaz")) {
                polja.put("poruka", polja.get("poruka_za_prikaz"));
                polja.remove("poruka_za_prikaz");
            }

            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));

            for (Proizvodi p : proizvodi) {
                p.ucitajKategoriju(kategorije_putanja);
            }

            ArrayList<Kategorije> kategorije = new ArrayList<>(Data.readFromJsonKategorije(kategorije_putanja));

            polja.put("proizvodi", proizvodi);
            polja.put("kategorije", kategorije);

            return new ModelAndView(polja, "adminPanel.hbs");
        }, new HandlebarsTemplateEngine());

        post("/prikaziIzmeniProizvod", (request, response) -> {
            response.type("text/text");

            int id = Integer.parseInt(request.queryParams("id_proizvoda"));

            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));
            Gson gson = new Gson();

            for(Proizvodi p: proizvodi) {
                if(p.getId() == id) {
                    p.ucitajKategoriju(kategorije_putanja);

                    return gson.toJson(p);
                }
            }

            //Vrati prazan JSON ukoliko ne pronađeš traženi proizvod za izmenu
            return gson.toJson(new ArrayList<>());
        });

        post("/izmeniProizvod/:id", "multipart/form-data",(request, response) -> {
            //Multipart config kako bi multipart forma radila
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement("images");
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));
            ArrayList<Kategorije> kategorije = new ArrayList<>(Data.readFromJsonKategorije(kategorije_putanja));


            int id = Integer.parseInt(request.params("id"));
            Part slikaUpload = request.raw().getPart("slika");
            String naziv = request.queryParams("naziv");
            double cena = Double.parseDouble(request.queryParams("cena"));
            String opis = request.queryParams("opis");
            int id_kategorije = Integer.parseInt(request.queryParams("kategorija"));


            for(Proizvodi p: proizvodi) {
                if(p.getId() == id) {
                    //Setovanje slike ako je promenjena
                    if(slikaUpload.getSize() != 0) {
                        //Provera da li je fajl dozvoljenog tipa (.png i .jpg)
                        String ekstenzija = slikaUpload.getSubmittedFileName().split("\\.")[1];

                        if(!(ekstenzija.equals("png") || ekstenzija.equals("jpg") || ekstenzija.equals("jpeg"))) {
                            polja.put("poruka_za_prikaz", "<div class='poruka' style='background-color: firebrick'>Fajl nije odgovarajućeg tipa! (.png ili .jpg)</div>");
                            response.redirect("/adminPanel");
                            return null;
                        }

                        Path out = Paths.get("src/public/images/" + slikaUpload.getSubmittedFileName());

                        try (final InputStream in = slikaUpload.getInputStream()) {
                            //Brisanje prethodne slike (neobavezno)
                            /*
                            Path staraSlika = Paths.get("src/public/images" + p.getSlika())
                            Files.delete(staraSlika);
                             */

                            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                        }

                        p.setSlika(slikaUpload.getSubmittedFileName());
                        slikaUpload.delete();
                    }

                    //Setovanje ostalih atributa
                    p.setNaziv(naziv);
                    p.setCena(cena);
                    p.setOpis(opis);

                    //Setovanje kategorije proizvoda
                    for(Kategorije k: kategorije) {
                        if(k.getId() == id_kategorije) {
                            p.setId_kategorije(k.getId());
                            break;
                        }
                    }

                    break;
                }
            }

            Data.writeToJSONProizvodi(proizvodi, proizvodi_putanja);

            response.redirect("/adminPanel");

            polja.put("poruka_za_prikaz", "<div class='poruka'>Proizvod je uspešno izmenjen!</div>");

            return null;
        });

        get("/izbrisiProizvod/:id", (request, response) -> {
            if (request.session().attribute("user") == null) {
                response.redirect("/");
                return null;
            }

            int id = Integer.parseInt(request.params("id"));

            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));

            for(Proizvodi p: proizvodi) {
                if(p.getId() == id) {
                    proizvodi.remove(p);
                    break;
                }
            }

            Data.writeToJSONProizvodi(proizvodi, proizvodi_putanja);

            response.redirect("/adminPanel");

            polja.put("poruka_za_prikaz", "<div class='poruka'>Proizvod je uspešno izbrisan!</div>");

            return null;
        });

        get("/izbrisiKategoriju/:id", (request, response) -> {
            if (request.session().attribute("user") == null) {
                response.redirect("/");
                return null;
            }

            ArrayList<Kategorije> kategorije = new ArrayList<>(Data.readFromJsonKategorije(kategorije_putanja));
            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));

            int id = Integer.parseInt(request.params("id"));

            for(Kategorije k: kategorije) {
                if(k.getId() == id) {
                    for(Proizvodi p: proizvodi) {
                        if(p.getId_kategorije() == id) {
                            response.redirect("/adminPanel");
                            polja.put("poruka_za_prikaz", "<div class='poruka' style='background-color: firebrick'>Nije moguće obrisati kategiriju jer sadrži proizvode!</div>");
                            return null;
                        }
                    }

                    kategorije.remove(k);
                    break;
                }
            }

            Data.writeToJSONKategorije(kategorije, kategorije_putanja);

            response.redirect("/adminPanel");

            polja.put("poruka_za_prikaz", "<div class='poruka'>Kategorija je uspešno izbrisana!</div>");

            return null;
        });

        post("/prikaziIzmeniKategoriju", (request, response) -> {
           int id = Integer.parseInt(request.queryParams("idKategorije"));
           response.type("text/text");

           ArrayList<Kategorije> kategorije = new ArrayList<>(Data.readFromJsonKategorije(kategorije_putanja));
           Kategorije trazena_kategorija = null;

           for(Kategorije k: kategorije) {
               if(k.getId() == id) {
                   trazena_kategorija = k;
                   break;
               }
           }

           Gson gson = new Gson();
           return gson.toJson(trazena_kategorija);
        });

        post("/izmeniKategoriju/:id", (request, response) -> {
           int id = Integer.parseInt(request.params("id"));
           String naziv = request.queryParams("nazivk");

           ArrayList<Kategorije> kategorije = new ArrayList<>(Data.readFromJsonKategorije(kategorije_putanja));

           for(Kategorije k: kategorije) {
               if(k.getId() == id) {
                   k.setIme_kategorije(naziv);
                   break;
               }
           }

           Data.writeToJSONKategorije(kategorije, kategorije_putanja);

           response.redirect("/adminPanel");

            polja.put("poruka_za_prikaz", "<div class='poruka'>Kategorija je uspešno izmenjena!</div>");

           return null;
        });

        post("/dodajProizvod", "multipart/form-data", (request, response) -> {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement("images");
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

            String naziv = request.queryParams("naziv");
            double cena = Double.parseDouble(request.queryParams("cena"));
            Part slikaUpload = request.raw().getPart("slika");
            int id_kategorije = Integer.parseInt(request.queryParams("kategorija"));
            String opis = request.queryParams("opis");

            ArrayList<Proizvodi> proizvodi = new ArrayList<>(Data.readFromJsonProizvodi(proizvodi_putanja));

            String ekstenzija = slikaUpload.getSubmittedFileName().split("\\.")[1];

            if(!(ekstenzija.equals("png") || ekstenzija.equals("jpg") || ekstenzija.equals("jpeg"))) {
                polja.put("poruka_za_prikaz", "<div class='poruka' style='background-color: firebrick'>Fajl nije odgovarajućeg tipa! (.png ili .jpg)</div>");
                response.redirect("/adminPanel");
                return null;
            }

            Path out = Paths.get("src/public/images/" + slikaUpload.getSubmittedFileName());

            try (final InputStream in = slikaUpload.getInputStream()) {
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            }

            proizvodi.add(new Proizvodi(proizvodi.get(proizvodi.size() - 1).getId() + 1, naziv, cena, slikaUpload.getSubmittedFileName(), opis, id_kategorije));

            Data.writeToJSONProizvodi(proizvodi, proizvodi_putanja);

            response.redirect("/adminPanel");

            slikaUpload.delete();

            polja.put("poruka_za_prikaz", "<div class='poruka'>Proizvod je uspešno dodat u bazu!</div>");

            return 0;
        });

        post("/dodajKategoriju", (request, response) -> {
            String naziv_kategorije = request.queryParams("nazivk");
            ArrayList<Kategorije> kategorije = new ArrayList<>(Data.readFromJsonKategorije(kategorije_putanja));

            kategorije.add(new Kategorije(kategorije.get(kategorije.size() - 1).getId() + 1, naziv_kategorije));
            Data.writeToJSONKategorije(kategorije, kategorije_putanja);

            response.redirect("/adminPanel");

            polja.put("poruka_za_prikaz", "<div class='poruka'>Kategorija je uspešno dodata u bazu!</div>");

            return 0;
        });
    }
}