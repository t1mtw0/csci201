let map;

async function initMap() {
  const { Map } = await google.maps.importLibrary("maps");
  const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

  map = new Map(document.getElementById("map"), {
    center: { lat: 47.517, lng: -27.528 },
    zoom: 2,
    mapId: "1",
  });

  map.addListener("click", (e) => {
    document.getElementById("map").style.setProperty("width", "0");
    document.getElementById("map").style.setProperty("height", "0");
    document.getElementById("map").innerHTML = "";
    document.getElementById("search-lat").value = e.latLng.lat().toString();
    document.getElementById("search-long").value = e.latLng.lng().toString();
  });
}
