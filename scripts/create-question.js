
// Gets the categories from the database and populates the categories select form
function get_categories() {
    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "categories", true);

    xhttp.onload = function () {
        const categoriesJSON = JSON.parse(this.responseText);
        if(categoriesJSON.status != "SUCCESS") {
            window.location.href = "no-categories.html"
        }

        var category_select = document.getElementById("category");

        for (let i = 0; i < categoriesJSON.categories.length; i++) {
            var opt = document.createElement("option");
            opt.value = categoriesJSON.categories[i].id;
            opt.innerHTML = categoriesJSON.categories[i].name;
            category_select.appendChild(opt);
        }
    }

    xhttp.send();
}

// When browser opens add categories to select
window.onload = get_categories;