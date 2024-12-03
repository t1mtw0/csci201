function getCookie(name) {
  const cookStr = `; ${document.cookie}`;
  return cookStr.split(`; ${name}=`);
}

cookParts = getCookie("sessid");
if (cookParts.length === 2) {
  document
    .getElementById("nav-signed-in")
    .style.setProperty("display", "inherit");
  document
    .getElementById("nav-signed-out")
    .style.setProperty("display", "none");
} else {
  document.getElementById("nav-signed-in").style.setProperty("display", "none");
  document
    .getElementById("nav-signed-out")
    .style.setProperty("display", "inherit");
}

function logout() {
  if (getCookie("sessid").length !== 2) return;
  fetch("/assign3/logout", {
    method: "POST",
    credentials: "same-origin",
  })
    .then((res) => location.replace("/assign3"))
    .catch((error) => console.log(error.message));
}
