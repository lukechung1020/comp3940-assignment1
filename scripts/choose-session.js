function check_valid_session() {
  sessionID = document.getElementById("session-id").value;
  if(sessionID.length != 6) {
    alert("Session ID must be EXACTLY 6 digits!");
    return;
  }

  const xhttp = new XMLHttpRequest();
  xhttp.open("GET", "check-session?sessionID=" + sessionID, true);

  xhttp.onload = function () {
      responseJSON = JSON.parse(this.responseText);
      console.log(responseJSON);
      if (!responseJSON.session_valid) {
          alert("Invalid session ID!");
      } else {
        window.location.href = "player-quiz.html?sessionID=" + sessionID;
      }
  }
  xhttp.send();
}
