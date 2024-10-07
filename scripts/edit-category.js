
let isDeleting = false;

// When browser opens add categories to select
window.onload = get_categories;

function get_categories() {
    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "categories", true);

    xhttp.onload = function () {
        if(isDeleting) {
            return;
        }

        var categoriesJSON = JSON.parse(this.responseText);
        if(categoriesJSON.status != "SUCCESS") {
            window.location.href = "create-category.html";
        }

        var category_select = document.getElementById("select-category");

        for (let i = 0; i < categoriesJSON.categories.length; i++) {
            var opt = document.createElement("option");
            opt.value = categoriesJSON.categories[i].id;
            opt.innerHTML = categoriesJSON.categories[i].name;
            category_select.appendChild(opt);
        }
    }

    xhttp.send();
}

function get_category(categoryID) {
    document.getElementById("category-media").innerHTML = "";

    if (categoryID == -1) {
        document.getElementById("edit-category-container").style.display = "none";
        return;
    }

    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "category?category=" + categoryID, true);

    xhttp.onload = function () {
        var categoryJSON = JSON.parse(this.responseText);

        var categoryImage = document.createElement("img");
        categoryImage.src = categoryJSON.category.media;
        categoryImage.alt = categoryJSON.category.name;

        document.getElementById("category-id").value = categoryJSON.category.id;
        document.getElementById("category-name").value = categoryJSON.category.name;
        document.getElementById("current-image").value = categoryJSON.category.media;

        document.getElementById("delete-category-id").value = categoryJSON.category.id;
        document.getElementById("delete-category-media").value = categoryJSON.category.media;

        document.getElementById("category-media").appendChild(categoryImage);
        document.getElementById("edit-category-container").style.display = "block";
    }

    xhttp.send();
}

function delete_category() {
    isDeleting = true;
    const xhttp = new XMLHttpRequest();

    var categoryID = document.getElementById("delete-category-id").value;
    var categoryMedia = document.getElementById("delete-category-media").value;

    xhttp.open("DELETE", "edit-category?category-id=" + categoryID + "&category-media=" + categoryMedia, true);

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