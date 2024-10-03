
// Gets the categories from the database and populates the categories select form
function get_categories() {
    const xhttp = new XMLHttpRequest();
    xhttp.open("GET", "upload-question", true);

    xhttp.onload = function () {
        const obj = JSON.parse(this.responseText);
        console.log(obj);
        var category_select = document.getElementById("category");

        for (let i = 0; i < obj.categories.length; i++) {
            var opt = document.createElement("option");
            opt.value = obj.categories[i].id;
            opt.innerHTML = obj.categories[i].category;
            category_select.appendChild(opt);
        }
    }

    xhttp.send();
}

// When browser opens add categories to select
window.onload = get_categories;