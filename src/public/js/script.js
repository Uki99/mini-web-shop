//Potrebne promenljive
let sortiranje = document.querySelector("#sortiranje");
let login = document.querySelector("#login");
let zatvori_formu = document.querySelector("#x_dugme");
let pretraga = document.querySelector("#pretraga");

//Search listener. Kad se pritisne enter
pretraga.addEventListener("keyup", function(key) {
    if (key.keyCode === 13) {
        let zahtev = pretraga.value;

        //Asinhrono slanje zahteva serveru da isfiltrira proizvode po zahtevu i vrati JSON
        $.post("/pretraga", {query: zahtev}, function(response) {
            let trazeni_proizvodi = JSON.parse(response);

            prikaziProizvode(trazeni_proizvodi);
        })
    }
});

//Listener koji iz drop down-a filtrira proizvode
sortiranje.addEventListener("change", function() {
    let tip = sortiranje.value;

    //Asinhrono salje zahtev serveru da isfiltrira proizvode po filteru iz drop down-a. Vraca JSON.
    $.post("/filtriraj", {filter: tip}, function(response) {
        let sortirani_proizvodi = JSON.parse(response);

        prikaziProizvode(sortirani_proizvodi);
    });
});

//Prikaz login forme
login.addEventListener("click", function() {
    let form = document.getElementById("dim");
    form.style.display = "block";
});

//Skrivanje login forme
zatvori_formu.addEventListener("click", function() {
    let form = document.getElementById("dim");
    form.style.display = "none";
});


// ----- FUNKCIJE ------

//Funkcija koja generise html, za svaki pojedinacni proizvod. Argument je JSON.
function prikaziProizvode(proizvodi) {
    let main = document.querySelector("#proizvodi");

    let html = "";

    for(let proizvod of proizvodi) {
        html += 
		`<div class="mojProizvod"> 
             <img src="images/${proizvod.slika}" alt="proizvod" style="width: 150px; height: 150px;">
             <h4>${proizvod.naziv}</h4>
             <p>Kategorija: ${proizvod.kategorija}</p>
             <h3>${proizvod.cena} RSD</h3>
         </div>`;
    }

    main.innerHTML = html;
}