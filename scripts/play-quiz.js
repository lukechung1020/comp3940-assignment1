
// Get the category ID and send it
const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const categoryID = urlParams.get("category");

var currentQuestionNumber = 0;
var questionsJSON;

// Gets the JSON for all questions
function get_questions() {
    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "quiz?category=" + categoryID, true);

    xhttp.onload = function () {
        questionsJSON = JSON.parse(this.responseText);

        if (questionsJSON.questions.length === 0) {
            window.location.href = "no-questions.html";
        }
        mediaParser(questionsJSON.questions[0].content_path);
        updateQuestionFields(questionsJSON);
    }
    xhttp.send();
}

// Update the HTML elements with the data from questions
function updateQuestionFields(JSONObject) {
    var questionNumber = document.getElementById("question numbers");
    questionNumber.innerText = "Question: " + ++currentQuestionNumber + "/" + JSONObject.questions.length;

    var question = document.getElementById("question");
    question.innerText = JSONObject.questions[currentQuestionNumber - 1].question;

    var currentButtonNum = 1;
    for (let i = 0; i < 4; i++) {
        var currentAnswerButton = document.getElementById("button" + currentButtonNum++);
        var answer = JSONObject.questions[currentQuestionNumber - 1].answers[i];
        var questionID = JSONObject.questions[currentQuestionNumber - 1].id;
        if (answer === undefined) {
            currentAnswerButton.style.display = "none";
        } else {
            currentAnswerButton.value = questionID;
            currentAnswerButton.innerHTML = answer;
            currentAnswerButton.name = answer;
            currentAnswerButton.style.display = "block";
        }
    }
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
        videoSource.type = "video/" + mediaType;

        questionMedia.appendChild(videoElement);
        videoElement.appendChild(videoSource);
        console.log("Video!");
    } else {
        mediaContainer.style.display = "none";
    }
}

// Check the answer, alert if wrong, move onto next question if correct
function checkAnswer(selectedAnswer, selectedQuestionID) {
    const xhttp = new XMLHttpRequest();
    xhttp.open("POST", "quiz?answer=" + selectedAnswer + "&questionID=" + selectedQuestionID, true);

    xhttp.onload = function () {
        const answerJSON = JSON.parse(this.responseText);

        if(questionsJSON.questions.length == currentQuestionNumber) {
            window.location.href = "end-of-quiz.html";
        }

        if(answerJSON.correct_answer_given) {
            mediaParser(questionsJSON.questions[currentQuestionNumber].content_path);
            updateQuestionFields(questionsJSON);
        } else {
            alert("Answer is incorrect!");
        }
    }
    xhttp.send();
}

window.onload = get_questions;