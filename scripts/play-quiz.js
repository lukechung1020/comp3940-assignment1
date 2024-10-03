
// Get the category ID and send it
const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const categoryID = urlParams.get("category");

var currentQuestionNumber = 0;

// Gets the JSON for all questions
function get_questions() {
  const xhttp = new XMLHttpRequest();
  xhttp.open("GET", "quiz?category=" + categoryID, true);

  xhttp.onload = function () {
      const obj = JSON.parse(this.responseText);
      console.log(obj);
      mediaParser(obj.questions[0].content_path);
      updateQuestionFields(obj);
  }
  xhttp.send();
}

function updateQuestionFields(JSONObject) {
  var questionNumber = document.getElementById("question numbers");
  questionNumber.innerText = "Question: " + ++currentQuestionNumber + "/" + JSONObject.questions.length;
  
  var question = document.getElementById("question");
  question.innerText = JSONObject.questions[currentQuestionNumber - 1].question;

  var currentButtonNum = 1;
  for(let i = 0; i < 4; i++) {
    var currentAnswerButton = document.getElementById("button" + currentButtonNum++);
      var answer = JSONObject.questions[currentQuestionNumber - 1].answers[i];
      if(answer === undefined) {
        currentAnswerButton.style.display = "none";
      } else {
        currentAnswerButton.value = answer;
        currentAnswerButton.innerHTML = answer;
        currentAnswerButton.style.display = "block";
      }
  }
}

var imageTypes = ["apng", "png", "avif", "gif", "jpg", "jpeg", "jfif", "pjpeg", "pjp", "svg", "webp"];
var videoTypes = ["mp4", "webm", "ogg", "mov"];

function mediaParser(mediaPath) {
  var questionMedia = document.getElementById("question-media");
  questionMedia.innerHTML = "";

  if(mediaPath === undefined) {
    return;
  }

  var temp = mediaPath.split(".");
  var mediaType = temp[temp.length - 1];

  if(imageTypes.includes(mediaType)) {
    var imgElement = document.createElement("img");
    imgElement.id = "question-img";
    imgElement.alt = "question-image";
    imgElement.src = mediaPath;

    questionMedia.appendChild(imgElement);
    console.log("Image!");
  } else if(videoTypes.includes(mediaType)) {
    var videoElement = document.createElement("video");
    videoElement.id = "question-video"
    videoElement.autoplay = true;
    videoElement.controls = true;
    videoElement.loop = true;

    var videoSource = document.createElement("source");
    videoSource.src = mediaPath;
    videoSource.type = "video/" + mediaType;

    questionMedia.appendChild(videoElement);
    videoElement.appendChild(videoSource);
    console.log("Video!");
  }
}

window.onload = get_questions;