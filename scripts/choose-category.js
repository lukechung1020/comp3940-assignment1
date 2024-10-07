
// Gets the categories from the database
function get_categories() {
    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "categories", true);

    xhttp.onload = function () {
        const obj = JSON.parse(this.responseText);
        if(obj.status != "SUCCESS") {
            window.location.href = "no-categories.html";
        }

        for (let i = 0; i < obj.categories.length; i++) {
            var currCategory = obj.categories[i];
            create_category_card(currCategory.id, currCategory.name, currCategory.content_path);
        }
    }

    xhttp.send();
}

// When browser opens add categories to select
window.onload = get_categories;

// Creates the category card
function create_category_card(categoryID, name, imagePath) {
    var cardContainer = document.createElement("div");
    cardContainer.className = "grid-item";

    var categoryForm = document.createElement("form");
    categoryForm.action = "quiz.html";

    var categoryIDInfo = document.createElement("input");
    categoryIDInfo.type = "hidden";
    categoryIDInfo.name = "category";
    categoryIDInfo.value = categoryID;

    var categoryImage = document.createElement("img");
    categoryImage.src = imagePath;
    categoryImage.alt = name;

    var submitBtn = document.createElement("button");
    submitBtn.type = "submit";
    submitBtn.innerHTML = name;

    categoryForm.appendChild(categoryIDInfo);
    categoryForm.appendChild(categoryImage);
    categoryForm.appendChild(submitBtn);
    cardContainer.appendChild(categoryForm);
    document.getElementById("categories").appendChild(cardContainer);
}