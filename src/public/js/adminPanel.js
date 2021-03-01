//Prikazi forme za proizvode i kategorije koji se pojavljuju.
let prikaz_forme_proizvod = document.querySelector("#proizvodi_forma");
let prikaz_forme_kategorija = document.querySelector("#kategorije_forma");

//FadeOut poruke obaveštenja u jQuery na admin panelu
$(".poruka").delay(3500).fadeOut(1500);

//Funkcija koja opsluzuje prikazivanje forme vezane za proizvode i formine funkcije
function proizvodForma(object) {
    let radnja = object.dataset.radnja; //moze biti izmeni ili dodaj

    let naslov = document.querySelector("#forma_proizvod_naslov");
    let dugme = document.querySelector("#forma_proizvod_dugme");

    let field_naziv = document.querySelector("#naziv");
    let field_cena = document.querySelector("#cena");
    let field_slika = document.querySelector("#slika");
    let field_kategorija = document.querySelector("#kategorija");
    let field_opis = document.querySelector("#opis");

    if(radnja === "izmeni") {
        let id = object.dataset.id;

        //Ruta koja salje id proizvoda koji treba da se promeni Javi kako bi vratio JS atribute za prikaz u formi
        $.post("/prikaziIzmeniProizvod", {id_proizvoda: id}, function(response) {
            let proizvod = JSON.parse(response);

            // !!! Menja action properti kako bi forma radila ono sto treba. (izmeni, ne dodaj)
            document.querySelector("#proizvodiForma").action = "/izmeniProizvod/" + id;

            field_naziv.value = proizvod.naziv;
            field_cena.value = parseFloat(proizvod.cena);
            field_kategorija.value = proizvod.id_kategorije;
            field_opis.value = proizvod.opis;

            naslov.innerHTML = "Izmenite podatke o proizvodu";
            dugme.innerHTML = "Izmeni proizvod";

            //Otkrij formu i blokiraj skrol
            prikaz_forme_proizvod.style.display = "block";
            document.body.style.overflow = "hidden";
        });
    }

    else if(radnja === "dodaj") {
        // !!! Menja action properti kako bi forma radila ono sto treba. (dodaj, ne izmeni)
        document.querySelector("#proizvodiForma").action = "/dodajProizvod";

        naslov.innerHTML = "Dodajte novi proizvod";
        dugme.innerHTML = "Dodajte proizvod";

        //Sve setuje na prazno kako bi forma bila blanko
        field_naziv.value = "";
        field_cena.value = "";
        field_slika.required = true; //Da bi slika bila obavezna
        field_kategorija.value = "";
        field_opis.value = "";

        //Otkrij formu i blokiraj skrol
        prikaz_forme_proizvod.style.display = "block";
        document.body.style.overflow = "hidden";
    }
}

//Funkcija koja opsluzuje prikazivanje forme vezane za kategorije i formine funkcije
function kategorijaForma(object) {
    let id = object.dataset.id;
    let radnja = object.dataset.radnja; //moze biti izmeni ili dodaj

    let field_naziv = document.querySelector("#nazivk");
    let naslov = document.querySelector("#forma_kategorija_naslov");
    let dugme = document.querySelector("#forma_kategorija_dugme");
    let labela = document.querySelector("#labelak");

    labela.innerHTML = "Naziv kategorije";

    if(radnja === "izmeni") {
        // !!! Menja action properti kako bi forma radila ono sto treba. (izmeni, ne dodaj)
        document.querySelector("#kategorijeForma").action = "/izmeniKategoriju/" + id;

        //Ruta koja salje id proizvoda Javi kako bi vratila atribute proizvoda JS radi ispune forme
        $.post("/prikaziIzmeniKategoriju", {idKategorije: id}, function(response) {
            let kategorija = JSON.parse(response);

            field_naziv.value = kategorija.ime_kategorije;
            naslov.innerHTML = "Izmenite kategoriju";
            dugme.innerHTML = "Snimite izmene kategorije";

        });

        //Otkrij formu i spreci skrol
        prikaz_forme_kategorija.style.display = "block";
        document.body.style.overflow = "hidden";
    }
    else if(radnja === "dodaj") {
        // !!! Menja action properti kako bi forma radila ono sto treba. (dodaj, ne izmeni)
        document.querySelector("#kategorijeForma").action = "/dodajKategoriju";

        naslov.innerHTML = "Dodajte novu kategoriju";
        dugme.innerHTML = "Dodajte novu kategoriju";
        labela.innerHTML = "Naziv kategorije";
        field_naziv.value = "";

        prikaz_forme_kategorija.style.display = "block";
        document.body.style.overflow = "hidden";
    }
}


//                                  ======== ZATVARANJE FORMA ========
document.querySelector("#x_dugme_proizvod").addEventListener("click", function() {
    prikaz_forme_proizvod.style.display = "none";
    document.querySelector("#slika").required = false;
    document.body.style.overflow = "auto";
});

document.querySelector("#x_dugme_kategorija").addEventListener("click", function() {
    prikaz_forme_kategorija.style.display = "none";

    document.body.style.overflow = "auto";
});