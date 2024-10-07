// If not logged in redirect to login page, otherwise go to home page
document.addEventListener("DOMContentLoaded", function () {
    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "check-login", true);
    
    xhttp.onload = function() {
        var responseJSON = JSON.parse(this.responseText);

        if(responseJSON.status != "SUCCESS") {
            window.location.href = "login.html";
        }
    }

    xhttp.send();
})