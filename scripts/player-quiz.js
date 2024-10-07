const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const sessionID = urlParams.get("sessionID");

document.getElementById("current-session-id").innerHTML = "Session ID: " + sessionID;


// Create a connection to the WebSocket server.
const socket = new WebSocket("ws://localhost:8081/comp3940-assignment1/multi-quiz/" + sessionID);

// This variable will track the role of the client: either 'MODERATOR' or 'PLAYER'.
let clientRole = "PLAYER";

// Event: When WebSocket connection is established.
socket.onopen = function (event) {
  console.log("Connected to the WebSocket server.");

  var message = {
    "role": clientRole,
    "action": "PLAYER_JOIN"
  }

  socket.send(JSON.stringify(message));
};

// Event: When a message is received from the server.
socket.onmessage = function (event) {
  console.log("Message from server:", event.data);

  let message = JSON.parse(event.data);

  switch (message.action) {
    case "SEND_ANSWERS":
      populate_answers(message.answers);
      break;
    case "MODERATOR_DISCONNECTED":
      window.location.href = "moderator-disconnected.html";
      break;
    case "CORRECT_ANSWER":
      color_answer(message.answer);
    default:
      console.log("ERROR Unknown action");
  }
  if (message.action == "SEND_ANSWERS") {
    populate_answers(message.answers);
  }
};

// Event: When the WebSocket connection is closed.
socket.onclose = function (event) {
  console.log("Disconnected from the WebSocket server.");
};

// Event: When there is an error with the WebSocket.
socket.onerror = function (error) {
  console.error("WebSocket error:", error);
};

window.addEventListener("unload", function () {
  if (socket.readyState == WebSocket.OPEN)
    socket.close();
});


// QUIZ LOGIC
// -------------------------------------------
let answersBox = document.getElementById("answer-container");
let selectedAnswerBox = document.getElementById("selected-answer-container");
let selectedAnswerButton = document.getElementById("selected-answer-btn");
let waitingAnswerMessage = document.getElementById("answer-message");

// Populates the answer buttons
function populate_answers(answers) {

  answersBox.style.display = "block";
  selectedAnswerBox.style.display = "none";
  selectedAnswerButton.className = "button-common"
  selectedAnswerButton.innerHTML = "NONE SELECTED";
  selectedAnswerButton.value = "NONE SELECTED";
  waitingAnswerMessage.innerHTML = "Waiting for moderator...";

  var currentButtonNum = 1;
  for (let i = 0; i < 4; i++) {
    var currentAnswerButton = document.getElementById("button" + currentButtonNum++);
    var answer = answers[i];
    if (answer === undefined) {
      currentAnswerButton.style.display = "none";
    } else {
      currentAnswerButton.value = answer;
      currentAnswerButton.innerHTML = answer;
      currentAnswerButton.name = answer;
      currentAnswerButton.style.display = "block";
    }
  }
}

function submit_answer(answer) {
  // Send submission
  submissionJSON = {
    "role": clientRole,
    "action": "ANSWER_SUBMISSION",
    "answer": answer
  } 
  socket.send(JSON.stringify(submissionJSON));

  // Display correct HTML
  answersBox.style.display = "none";
  selectedAnswerButton.innerHTML = answer;
  selectedAnswerButton.value = answer;
  selectedAnswerBox.style.display = "block";
}

// Changes the color of your answer
function color_answer(answer) {
  answersBox.style.display = "none";
  selectedAnswerBox.style.display = "block";
  waitingAnswerMessage.innerHTML = answer == selectedAnswerButton.value ? "CORRECT!" : "INCORRECT!";
  selectedAnswerButton.className = answer == selectedAnswerButton.value ? "button-common green-button" : "button-common red-button";
}
