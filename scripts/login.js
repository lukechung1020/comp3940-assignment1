function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  if (!username || !password) {
    alert("Username or Password cannot be empty.");
    return;
  }

  const loginData = {
    username: username,
    password: password,
  };

  fetch("login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(loginData),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        window.location.href = "main";
      } else {
        alert(data.message);
      }
    })
    .catch((error) => console.error("Error:", error));
}

document.addEventListener("DOMContentLoaded", function () {
  document.querySelector("button").addEventListener("click", login);
});
