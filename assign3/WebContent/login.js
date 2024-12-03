document
  .getElementById("login-form")
  .addEventListener("submit", async function (e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const formObj = Object.fromEntries(formData);
    const headers = {
      "Content-Type": "application/json",
    };
    fetch("/assign3/login", {
      method: "POST",
      credentials: "same-origin",
      headers: headers,
      body: JSON.stringify(formObj),
    })
      .then((res) => res.json())
      .then((data) => {
        console.log(data.status);
        if (data.status === "error")
          document.getElementById("login-err").style.setProperty("display", "flex");
        else
          window.location.replace("/assign3");
      })
      .catch((error) => {
        console.log("fetch error");
      });
  });
