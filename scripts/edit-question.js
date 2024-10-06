
let isDeleting = false;

// Gets the questions from the DB and updates the select
function get_questions() {
    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "questions", true);

    xhttp.onload = function () {
        if(isDeleting) {
            return;
        }
        const questionsJSON = JSON.parse(this.responseText);
        if(questionsJSON.status != "SUCCESS") {
            window.location.href = "create-question.html";
        }

        var question_select = document.getElementById("select-question");

        for (let i = 0; i < questionsJSON.questions.length; i++) {
            var opt = document.createElement("option");
            opt.value = questionsJSON.questions[i].id;
            opt.innerHTML = questionsJSON.questions[i].question;
            question_select.appendChild(opt);
        }
    }

    xhttp.send();
}

// When browser opens add categories to select
window.onload = get_questions;

function get_categories(callback) {
    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "categories", true);

    xhttp.onload = function () {
        var categoriesJSON = JSON.parse(this.responseText);

        var category_select = document.getElementById("category");

        for (let i = 0; i < categoriesJSON.categories.length; i++) {
            var opt = document.createElement("option");
            opt.value = categoriesJSON.categories[i].id;
            opt.innerHTML = categoriesJSON.categories[i].name;
            category_select.appendChild(opt);
        }

        if(callback) {
            callback();
        }
    }

    xhttp.send();
}

var imageTypes = ["apng", "png", "avif", "gif", "jpg", "jpeg", "jfif", "pjpeg", "pjp", "svg", "webp"];
var videoTypes = ["mp4", "webm", "ogg", "mov"];

// Create the correct HTML element based on the media
function media_parser(mediaPath) {
    var questionMedia = document.getElementById("question-media");
    questionMedia.innerHTML = "";

    if (mediaPath === undefined || mediaPath == "") {
        questionMedia.style.display = "inline";
        questionMedia.innerText = "None";
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
    } else {
        mediaContainer.style.display = "none";
    }

    document.getElementById("current-image").value = mediaPath;
    document.getElementById("delete-question-media").value = mediaPath;
}

var questionJSON;

// Get the question data from the server based on ID
function get_question(questionID) {
    if(questionID == -1) {
        document.getElementById("edit-question-container").style.display = "none";
        return;
    }

    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "question?question=" + questionID, true);
    xhttp.onload = function () {
        var questionJSON = JSON.parse(this.responseText);
        get_categories(() => {
            update_selected_category(questionJSON.category);
        });
        media_parser(questionJSON.content_path);

        document.getElementById("question-id").value = questionJSON.id;
        document.getElementById("delete-question-id").value = questionJSON.id;
        document.getElementById("question").value = questionJSON.question;
        document.getElementById("correct-answer").value = questionJSON.correct_answer;
        document.getElementById("wrong-answer1").value = questionJSON.wrong_answer_1;
        document.getElementById("wrong-answer2").value = questionJSON.wrong_answer_2;
        document.getElementById("wrong-answer3").value = questionJSON.wrong_answer_3;

        document.getElementById("edit-question-container").style.display = "block";
    }

    xhttp.send();
}

function update_selected_category(categoryID) {
    document.getElementById("category").value = categoryID;
}

function delete_question() {
    isDeleting = true;
    const xhttp = new XMLHttpRequest();

    var questionID = document.getElementById("delete-question-id").value;
    var questionMedia = document.getElementById("delete-question-media").value;

    xhttp.open("DELETE", "edit-question?question-id=" + questionID + "&question-media=" + questionMedia, true);

    xhttp.onload = function () {
        // Parse the response from the server
        var responseJSON = JSON.parse(this.responseText);
        
        if (responseJSON.status === "SUCCESS") {
            // If deletion was successful, redirect to the success page
            window.location.href = "edit-success.html";
        } else {
            // If deletion failed, redirect to the failure page
            window.location.href = "edit-failure.html";
        }
    };

    xhttp.onerror = function () {
        // Handle request error (network issues, etc.)
        window.location.href = "edit-failure.html";
    };

    xhttp.send();
}
