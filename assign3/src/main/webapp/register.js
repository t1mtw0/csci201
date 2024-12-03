document
  .getElementById("reg-form")
  .addEventListener("submit", async function (e) {
    e.preventDefault();
    let cpw = document.getElementById("cpw");
    if (cpw.value != document.getElementById("pw").value) {
      cpw.setCustomValidity("Password Must be Matching.");
      cpw.reportValidity();
      return;
    }

    const formData = new FormData(e.target);
    const formObj = Object.fromEntries(formData);
    const headers = {
      "Content-Type": "application/json",
    };
    fetch("/assign3/register", {
      method: "POST",
      headers: headers,
      body: JSON.stringify(formObj),
    })
      .then((res) => res.json())
      .then((data) => {
        console.log(data.status);
        if (data.status === "error")
          document
            .getElementById("reg-err")
            .style.setProperty("display", "flex");
        else window.location.replace("/assign3/login.html");
      })
      .catch((error) => console.log(error.message));
  });

document.getElementById("cpw").addEventListener("change", function (e) {
  if (e.target.value === document.getElementById("pw").value) {
    e.target.setCustomValidity("");
    e.target.reportValidity();
  }
});
