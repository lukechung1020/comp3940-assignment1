const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const sessionID = urlParams.get("sessionID");

document.getElementById("current-session-id").innerHTML = "Session ID: " + sessionID;


// Create a connection to the WebSocket server.
const socket = new WebSocket("ws://localhost:8081/comp3940-assignment1/multi-quiz/" + sessionID);

// This variable will track the role of the client: either 'MODERATOR' or 'PLAYER'.
let clientRole = "PLAYER";

// Event: When WebSocket connection is established.
socket.onopen = function(event) {
  console.log("Connected to the WebSocket server.");

  var message = {
    "role": clientRole,
    "action": "PLAYER_JOIN"
  }

  socket.send(JSON.stringify(message));
};

// Event: When a message is received from the server.
socket.onmessage = function(event) {
  console.log("Message from server:", event.data);

  let message = event.data;

};

// Event: When the WebSocket connection is closed.
socket.onclose = function(event) {
  console.log("Disconnected from the WebSocket server.");
};

// Event: When there is an error with the WebSocket.
socket.onerror = function(error) {
  console.error("WebSocket error:", error);
};

window.addEventListener("unload", function () {
  if(socket.readyState == WebSocket.OPEN)
      socket.close();
});
