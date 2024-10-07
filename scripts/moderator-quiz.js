const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const sessionID = urlParams.get("sessionID");
const categoryID = urlParams.get("category");

document.getElementById("current-session-id").innerHTML = "Session ID: " + sessionID;

const clientRole = "MODERATOR";

// WEB SOCKET LOGIC
// -----------------------------------------------------------------------
// Event: When WebSocket connection is established.
const socket = new WebSocket("ws://localhost:8081/comp3940-assignment1/multi-quiz/" + sessionID);

socket.onopen = function (event) {
  console.log("Connected to the WebSocket server.");

  var message = {
    "role": clientRole,
    "action": "MODERATOR_JOIN"
  }

  socket.send(JSON.stringify(message));

  get_questions();
};

// Event: When a message is received from the server.
socket.onmessage = function (event) {
  console.log("Message from server:", event.data);

  let message = JSON.parse(event.data);

  switch(message.action) {
    case "PLAYER_JOINED":
      send_answers();
      break;
    case "ANSWER_SUBMISSION":
      for(let i = 0; i < currentQuestionAnswers.length; i++) {
        if(message.answer == currentQuestionAnswers[i]) {
          answersResultsCounts[i] += 1;
          var currentResultAnswerCount = document.getElementById("result-answer-" + (i+1) + "-count");
          currentResultAnswerCount.innerHTML = answersResultsCounts[i];
        }
      }
      break;
    case "MODERATOR_EXISTS":
      let tempArray = window.location.search.split("?");
      baseURL = tempArray[0];
      window.location.href = baseURL + "?category=" + categoryID + "&sessionID=" + Math.floor(Math.random() * 899999 + 100000);
      break;
    default:
      console.log(message);
      console.log("Unknown message action!");
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
  if (socket.readyState == WebSocket.OPEN) {
      modDCJSON = {
        "role": clientRole,
        "action": "MODERATOR_DISCONNECTED"
      }
      socket.send(JSON.stringify(modDCJSON));
      socket.close();
    }
});



// QUIZ RELATED LOGIC
// -----------------------------------------------------------------------

let currentQuestionNumber = 0;
let currentQuestionAnswers = [];
let currentQuestionAnswer = "";
var questionsJSON;

function get_questions() {
  const xhttp = new XMLHttpRequest();
  xhttp.open("GET", "questions?category=" + categoryID, true);

  xhttp.onload = function () {
    questionsJSON = JSON.parse(this.responseText);
    console.log(questionsJSON);
    if (questionsJSON.status == "No questions!") {
      window.location.href = "no-questions.html";
    }
    mediaParser(questionsJSON.questions[0].content_path);
    updateQuestionFields(questionsJSON);
    send_answers();
  }
  xhttp.send();
}

function send_answers() {
  if (currentQuestionAnswers.length > 0) {
    answersJSON = {
      "role": clientRole,
      "action": "SEND_ANSWERS",
      "answers": currentQuestionAnswers
    }
    socket.send(JSON.stringify(answersJSON));
  } else {
    console.log("No answers yet!");
  }
}

// Update the HTML elements with the data from questions
function updateQuestionFields(JSONObject) {
  let currentQuestion = JSONObject.questions[currentQuestionNumber];
  let answers = [];

  answers[0] = currentQuestion.correct_answer;
  currentQuestionAnswer = currentQuestion.correct_answer;
  answers[1] = currentQuestion.wrong_answer_1;
  if (currentQuestion.wrong_answer_2 != "") {
    answers[2] = currentQuestion.wrong_answer_2;
  }
  if (currentQuestion.wrong_answer_3 != "") {
    answers[3] = currentQuestion.wrong_answer_3;
  }

  answers = shuffle(answers);
  currentQuestionAnswers = answers;

  let questionNumber = document.getElementById("question numbers");
  questionNumber.innerText = "Question: " + ++currentQuestionNumber + "/" + JSONObject.questions.length;

  var question = document.getElementById("question");
  question.innerText = currentQuestion.question;

  var correctAnswerButton = document.getElementById("correct-answer-btn");
  correctAnswerButton.innerHTML = currentQuestionAnswer;

  var correctAnswerPanel = document.getElementById("correct-answer-panel");
  correctAnswerPanel.style.display = "none";

  var questionPanel = document.getElementById("question-container");
  questionPanel.style.display = "block";

  update_results_field(currentQuestionAnswers);
}

var imageTypes = ["apng", "png", "avif", "gif", "jpg", "jpeg", "jfif", "pjpeg", "pjp", "svg", "webp"];
var videoTypes = ["mp4", "webm", "ogg", "mov"];

// Create the correct HTML element based on the media
function mediaParser(mediaPath) {
  var questionMedia = document.getElementById("question-media");
  questionMedia.innerHTML = "";

  if (mediaPath === undefined) {
    return;
  }

  var temp = mediaPath.split(".");
  var mediaType = temp[temp.length - 1];

  var mediaContainer = document.getElementById("question-media");
  mediaContainer.style.display = "inline";
  if (imageTypes.includes(mediaType)) {
    var imgElement = document.createElement("img");
    imgElement.id = "question-img";
    imgElement.alt = "question-image";
    imgElement.src = mediaPath;

    questionMedia.appendChild(imgElement);
    console.log("Image!");
  } else if (videoTypes.includes(mediaType)) {
    var videoElement = document.createElement("video");
    videoElement.id = "question-video"
    videoElement.autoplay = true;
    videoElement.controls = true;
    videoElement.loop = true;

    var videoSource = document.createElement("source");
    videoSource.src = mediaPath;
    if (mediaType === "mov") {
      videoSource.type = "video/mp4";
  } else {
      videoSource.type = "video/" + mediaType;
  }

    questionMedia.appendChild(videoElement);
    videoElement.appendChild(videoSource);
    console.log("Video!");
  } else {
    mediaContainer.style.display = "none";
  }
}

let answersResultsCounts = [0, 0, 0, 0];

function update_results_field(answers) {
  answersResultsCounts = [0, 0, 0, 0];
  console.log(answers);
  for(let i = 1; i <= 4; i++) {
    console.log(i);
    var currentResultContainer = document.getElementById("result-answer-container-" + i);
    var currentResultAnswer = document.getElementById("result-answer-" + i);
    var currentResultAnswerCount = document.getElementById("result-answer-" + i + "-count");
    let currAnswer = answers[i - 1];

    if(currAnswer == undefined) {
      currentResultAnswer.innerHTML = "";
      currentResultAnswerCount.innerHTML = "";
      currentResultContainer.style.display = "none";
    } else {
      currentResultAnswer.innerHTML = currAnswer + ": ";
      currentResultAnswerCount.innerHTML = answersResultsCounts[i - 1];
      currentResultContainer.style.display = "block";
    }
  }
}

function display_answer() {
  let nextQuestionBtn = document.getElementById("next-question-btn");
  nextQuestionBtn.style.display = "block";

  document.getElementById("correct-answer-panel").style.display = "block";
  document.getElementById("question-container").style.display = "none";

  // Send the correct answers
  correctAnswerJSON = {
    "role": clientRole,
    "action": "CORRECT_ANSWER",
    "answer": currentQuestionAnswer
  }
  socket.send(JSON.stringify(correctAnswerJSON));

  if(currentQuestionNumber == questionsJSON.questions.length) {
    nextQuestionBtn.innerHTML = "End Quiz";
  }
}

function next_question() {
  if (currentQuestionNumber == questionsJSON.questions.length) {
    window.location.href = "end-of-quiz.html";
  } else {
    mediaParser(questionsJSON.questions[currentQuestionNumber].content_path);
    updateQuestionFields(questionsJSON);
    send_answers();
  }
}

// Shuffles the array
const shuffle = (array) => {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
  return array;
}

