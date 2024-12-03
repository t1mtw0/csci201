let navCont = document.getElementById("nav-cont");
let navSearch = document.querySelector(".nav-search");
let navLinks = document.getElementById("nav-links");
navCont.removeChild(navSearch);

let main = document.querySelector("main");
let searchForm = document.getElementById("search");
let histDets = document.getElementById("hist-details");
let results = document.getElementById("results-cont");
main.removeChild(results);
main.removeChild(histDets);

let searchText = document.getElementById("search-text");
let searchLat = document.getElementById("search-lat");
let searchLng = document.getElementById("search-long");

let rad = document.querySelector('input[name="search-opt"]:checked');
let lastRadio = "";
searchChangeOpt(rad);

function searchChangeOpt(radio) {
  if (radio.value === "city") {
    if (lastRadio === "city") return;
    document.getElementById("search-text-cont").appendChild(searchText);
    document.getElementById("search-text-cont").style.setProperty("flex", "1");
    searchLat = document.getElementById("search-lat");
    document.getElementById("search-lat-cont").removeChild(searchLat);
    document.getElementById("search-lat-cont").style.setProperty("flex", "0");
    searchLng = document.getElementById("search-long");
    document.getElementById("search-long-cont").removeChild(searchLng);
    document.getElementById("search-long-cont").style.setProperty("flex", "0");
    document.getElementById("search-text-img").hidden = false;
    document.getElementById("search-text-img-cont").hidden = false;
    document.getElementById("search-text-img-cont").style.display = "inherit";
    document.getElementById("search-map").hidden = true;
    document.getElementById("search-map-icon").style.display = "none";
    document.getElementById("search-cont").classList.remove("ll");
    document.getElementById("search-cont").classList.add("city");
    lastRadio = "city";
  } else if (radio.value === "ll") {
    if (lastRadio === "ll") return;
    searchText = document.getElementById("search-text");
    document.getElementById("search-text-cont").removeChild(searchText);
    document.getElementById("search-text-cont").style.setProperty("flex", "0");
    document.getElementById("search-lat-cont").appendChild(searchLat);
    document.getElementById("search-lat-cont").style.setProperty("flex", "1");
    document.getElementById("search-long-cont").appendChild(searchLng);
    document.getElementById("search-long-cont").style.setProperty("flex", "1");
    document.getElementById("search-text-img-cont").hidden = true;
    document.getElementById("search-text-img-cont").style.display = "none";
    document.getElementById("search-map-icon").style.display = "inherit";
    document.getElementById("search-map").hidden = false;
    document.getElementById("search-cont").classList.remove("city");
    document.getElementById("search-cont").classList.add("ll");
    document
      .getElementById("search-map-but")
      .addEventListener("click", function () {
        if (document.getElementById("map").innerHTML === "") {
          document.getElementById("map").style.setProperty("width", "40vw");
          document.getElementById("map").style.setProperty("height", "40vh");
          initMap();
        } else {
          document.getElementById("map").innerHTML = "";
          document.getElementById("map").style.setProperty("width", "0");
          document.getElementById("map").style.setProperty("height", "0");
        }
      });
    lastRadio = "ll";
  }
}

let sresdat = [];

document.getElementById("search").addEventListener("submit", search);

function search(e) {
  e.preventDefault();
  const formData = new FormData(e.target);
  const formObj = Object.fromEntries(formData);
  fetch("/assign3/search?" + new URLSearchParams(formObj).toString(), {
    method: "GET",
    credentials: "same-origin",
  })
    .then((res) => res.json())
    .then((data) => {
      if (data.status === "success") {
        sresdat = [];
        for (const dat of data.data) {
          let srd = new Date(parseInt(dat.sys.sunrise) * 1000);
          let ssd = new Date(parseInt(dat.sys.sunset) * 1000);
          let sr = {
            hours: srd.getHours(),
            minutes: srd.getMinutes(),
            seconds: srd.getSeconds(),
          };
          let ss = {
            hours: ssd.getHours(),
            minutes: ssd.getMinutes(),
            seconds: ssd.getSeconds(),
          };
          let dataObj = {
            tmp: dat.main.temp,
            tl: dat.main.temp_min,
            th: dat.main.temp_max,
            loc: dat.name,
            hum: dat.main.humidity,
            wnd: dat.wind.speed,
            crd: dat.coord,
            sr: sr,
            ss: ss,
          };
          sresdat.push(dataObj);
        }
        if (sresdat.length === 0) return;
        if (sresdat.length === 1) {
          if (!main.contains(histDets)) main.appendChild(histDets);
          let dat = sresdat.at(0);
          document.getElementById("det-loc").innerHTML = dat.loc;
          document.getElementById("det-low").innerHTML = dat.tl;
          document.getElementById("det-high").innerHTML = dat.th;
          document.getElementById("det-wind").innerHTML = dat.wnd;
          document.getElementById("det-prec").innerHTML = dat.hum + "%";
          document.getElementById("det-ll").innerHTML =
            dat.crd.lat + "/" + dat.crd.lon;
          document.getElementById("det-cur").innerHTML = dat.tmp;
          document.getElementById("det-ss").innerHTML =
            dat.sr.hours + "/" + dat.ss.hours;
          return;
        }
        if (main.contains(searchForm)) main.removeChild(searchForm);
        if (main.contains(histDets)) main.removeChild(histDets);
        main.appendChild(results);
        let resultsTb = document.getElementById("results-tb").children[0];
        let resultsC = [];
        for (const r of resultsTb.children) resultsC.push(r);
        for (const c of resultsC)
          if (c.getAttribute("id") !== "results-h") resultsTb.removeChild(c);
        let i = 0;
        for (const dat of sresdat) {
          let tr = document.createElement("tr");
          let locd = document.createElement("td");
          locd.innerHTML = dat.loc;
          let tld = document.createElement("td");
          tld.innerHTML = dat.tl;
          let thd = document.createElement("td");
          thd.innerHTML = dat.th;
          tr.appendChild(locd);
          tr.appendChild(tld);
          tr.appendChild(thd);
          tr.setAttribute("id", (i++).toString());
          tr.addEventListener("click", showDets);
          resultsTb.appendChild(tr);
        }
        updateResults();
      }
      if (!navCont.contains(navSearch)) {
        navCont.insertBefore(navSearch, navLinks);
        searchText = document.getElementById("search-text");
        searchLat = document.getElementById("search-lat");
        searchLng = document.getElementById("search-long");
        let rad = document.querySelector('input[name="search-opt"]:checked');
        lastRadio = "";
        searchChangeOpt(rad);
        navSearch.addEventListener("submit", search);
      }
    })
    .catch((error) => console.log(error.message));
}

function showDets(e) {
  let dat = sresdat.at(parseInt(e.target.getAttribute("id")));
  main.removeChild(results);
  main.appendChild(searchForm);
  main.appendChild(histDets);
  if (navCont.contains(navSearch)) navCont.removeChild(navSearch);
  document.getElementById("det-loc").innerHTML = dat.loc;
  document.getElementById("det-low").innerHTML = dat.tl;
  document.getElementById("det-high").innerHTML = dat.th;
  document.getElementById("det-wind").innerHTML = dat.wnd;
  document.getElementById("det-prec").innerHTML = dat.hum + "%";
  document.getElementById("det-ll").innerHTML = dat.crd.lat + "/" + dat.crd.lon;
  document.getElementById("det-cur").innerHTML = dat.tmp;
  document.getElementById("det-ss").innerHTML =
    dat.sr.hours + "/" + dat.ss.hours;
}

function updateResults() {
  let resultsTb = document.getElementById("results-tb").children[0];
  let resultsC = [];
  for (const c of resultsTb.children) {
    if (c.getAttribute("id") === "results-h") continue;
    resultsC.push(c);
  }
  for (const c of resultsTb.children) {
    if (c.getAttribute("id") === "results-h") continue;
    resultsTb.removeChild(c);
  }
  let select = document.getElementById("results-sort-opts");
  let sortOpt = select.options[select.selectedIndex].value;

  switch (sortOpt) {
    case "A-Z":
      resultsC.sort(function (a, b) {
        return a.children[0].innerHTML.localeCompare(b.children[0].innerHTML);
      });
      break;
    case "Z-A":
      resultsC.sort(function (a, b) {
        return b.children[0].innerHTML.localeCompare(a.children[0].innerHTML);
      });
      break;
    case "LDESC":
      resultsC.sort(function (a, b) {
        return (
          parseFloat(a.children[1].innerHTML) <
          parseFloat(b.children[1].innerHTML)
        );
      });
      break;
    case "LASC":
      resultsC.sort(function (a, b) {
        return (
          parseFloat(a.children[1].innerHTML) >
          parseFloat(b.children[1].innerHTML)
        );
      });
      break;
    case "HASC":
      resultsC.sort(function (a, b) {
        return (
          parseFloat(a.children[2].innerHTML) >
          parseFloat(b.children[2].innerHTML)
        );
      });
      break;
    case "HDESC":
      resultsC.sort(function (a, b) {
        return (
          parseFloat(a.children[2].innerHTML) <
          parseFloat(b.children[2].innerHTML)
        );
      });
      break;
  }

  for (const c of resultsC) resultsTb.appendChild(c);
}
