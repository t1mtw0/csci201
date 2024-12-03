cookParts = getCookie("sessid");
if (cookParts.length === 2) {
  fetch("/assign3/history", {
    method: "GET",
    credentials: "same-origin",
  })
    .then((res) => res.json())
    .then((data) => {
      let histList = document.getElementById("hist-list");
      if (data.status === "success") {
        document.getElementById("usern").innerHTML = data.usern;
        if (data.data.length === 0) {
          document.getElementById("prof-search-hist").innerHTML =
            "no search results";
          document
            .getElementById("prof-search-hist")
            .style.setProperty("text-align", "center");
          return;
        }
        for (const dat of data.data) {
          let li = document.createElement("li");
          li.classList.add("hist-item");
          li.innerHTML = dat.query;
          histList.appendChild(li);
        }
      }
    })
    .catch((error) => console.log(error.message));
} else {
  document.getElementById("prof-content").innerHTML =
    "please sign in to view this page";
  document
    .getElementById("prof-content")
    .style.setProperty("text-align", "center");
}
