const endpoints = {
    compare: '/compare',
    listOfDates: '/listOfDates',
    listOfAccounts: '/listOfAccounts',
    deleteAccounts: '/deleteAccounts',
    protectAccounts: '/protectAccounts'
};

document.onload = getListOfDates();
let links = [];
let index = 0;
let altIndex = 0;
const batchSize = 10;
const interval = 5000;

async function collectData() {
    collector.disabled = true;
    collector.style.cursor = 'not-allowed';

    const response = await fetch(endpoints.compare);
    const data = await response.json();

    if (response.ok) {
        console.log(data.report);
        const pre = document.createElement("pre");
        pre.textContent = JSON.stringify(data.report, null, 2);
        document.body.innerHTML = "";
        document.body.appendChild(pre);
        setTimeout(() => {
            location.reload();
        }, interval);
    } else {
        console.log("Failed to send the request")
        const pre = document.createElement("pre");
        pre.textContent = "Something went wrong";
        document.body.innerHTML = "";
        document.body.appendChild(pre);
    }
}

async function submitForm(event) {
    event.preventDefault(); // Prevents the form from submitting the default way
    btn1.disabled = true;
    btn1.style.cursor = 'not-allowed';

    const formData = new FormData(dateForm);
    const queryString = new URLSearchParams(formData).toString();

    const response = await fetch(endpoints.listOfAccounts + `?${queryString}`);

    if (response.ok) { // Checks if response status is 200
        divForm.style.display = "none"; // Hides the form on success
        retrieved.style.display = "flex"; // Shows the list on success
        collector.style.display = "none"; // Hides the button on success
        buttonBox.style.display = "inline-block"; // Shows the buttons on success
        changeIndex.style.display = "inline-block"; // Shows the input on success

        const data = await response.json();

        for (const [key, value] of Object.entries(data)) {
            const div = document.createElement("div");
            div.innerHTML = `<label></label><a href=${value} target="_blank">${key}</a><button onclick="moveBox(this)">Delete</button>`;
            div.classList.add("box");
            toBeKeptList.appendChild(div);
        }

        sortBoxes(toBeKeptList);
        index++;
        for (const box of toBeKeptList.children) {
            box.firstChild.innerText = index++;
        }
        index = 0;
        links = Array.from(toBeKeptList.querySelectorAll("a")).map(a => a.href);
    } else {
        alert("Failed to submit form");
    }
}

changeIndex.addEventListener("input", () => {
    if (changeIndex.value > 0 && changeIndex.value <= links.length) {
        changeIndex.setCustomValidity("");
    } else {
        changeIndex.setCustomValidity("Invalid index");
    }
});

changeIndex.onchange = () => {
    if (changeIndex.checkValidity()) {
        index = changeIndex.value - 1;
        updateIndexDisplay();
    } else {
        changeIndex.reportValidity();
    }
    changeIndex.value = "";
};

function updateIndexDisplay() {
    buttonBox.innerText = `Open links batch (from index ${index + 1})`;
}

function sendList() {
    btn2.disabled = true;
    btn2.style.cursor = 'not-allowed';
    if (toBeDeletedList.children.length > 0) {
        deleteAccounts(Array.from(toBeDeletedList.querySelectorAll(".box")).map(box => box.children[1].textContent));
    } else {
        protectAccounts(Array.from(toBeDeletedList.querySelectorAll(".box")).map(box => box.children[1].textContent));
    }
}

function openLinksBatch() {
    for (let i = 0; i < batchSize; i++) {
        if (index >= links.length) {
            return;
        }
        window.open(links[index++], '_blank');
    }
    updateIndexDisplay();
}

function openAllLinks() {
    btn3.disabled = true;
    btn3.style.cursor = 'not-allowed';
    openLinksRecursively();
    altIndex = 0;
    btn3.disabled = false;
    btn3.style.cursor = 'pointer';
}

// Function to open a batch of links
function openLinksRecursively() {
    if (altIndex >= links.length) return; // Stop if all links have been opened

    // Open up to 'batchSize' links in the current batch
    for (let i = 0; i < batchSize && altIndex < links.length; i++) {
        window.open(links[altIndex], '_blank');
        altIndex++;
    }

    // Set a timeout to open the next batch
    if (altIndex < links.length) {
        setTimeout(openLinksRecursively, interval);
    }
}

async function protectAccounts(list) {
    const formData = new FormData(dateForm);
    const queryString = new URLSearchParams(formData).toString();

    const response = await fetch(endpoints.protectAccounts + `?${queryString}`, {
        method: "PUT",
        body: JSON.stringify(list),
        headers: {
            'Content-Type': 'application/json'
        },
    });
    const data = await response.json();

    if (response.ok) {
        console.log(data.report);
        const pre = document.createElement("pre");
        pre.textContent = JSON.stringify(data.report, null, 2);
        document.body.innerHTML = "";
        document.body.appendChild(pre);
        setTimeout(() => {
            location.reload();
        }, interval);
    } else {
        console.log("Failed to send the list for protection")
        const pre = document.createElement("pre");
        pre.textContent = "Something went wrong";
        document.body.innerHTML = "";
        document.body.appendChild(pre);
    }
}

async function deleteAccounts(list) {
    const formData = new FormData(dateForm);
    const queryString = new URLSearchParams(formData).toString();

    const response = await fetch(endpoints.deleteAccounts + `?${queryString}`, {
        method: "DELETE",
        body: JSON.stringify(list),
        headers: {
            'Content-Type': 'application/json'
        },
    });
    const data = await response.json();

    if (response.ok) {
        console.log(data.report);
        const pre = document.createElement("pre");
        pre.textContent = JSON.stringify(data.report, null, 2);
        document.body.innerHTML = "";
        document.body.appendChild(pre);
        setTimeout(() => {
            location.reload();
        }, interval);
    } else {
        console.log("Failed to send the list")
        const pre = document.createElement("pre");
        pre.textContent = JSON.stringify(list, null, 2);
        document.body.innerHTML = "";
        document.body.appendChild(pre);
    }
}

function moveBox(button) {
    const box = button.parentElement;

    if (box.parentElement.id === "toBeKeptList") {
        // Move to top list
        toBeDeletedList.appendChild(box);
        button.innerText = "Keep";
        sortBoxes(toBeDeletedList);
    } else {
        // Move back to bottom list
        toBeKeptList.appendChild(box);
        button.innerText = "Delete";
        sortBoxes(toBeKeptList);
    }

    if (toBeDeletedList.children.length > 0) {
        retrieved.children[0].style.display = "block";
        toBeDeletedList.style.display = "grid";
    } else {
        retrieved.children[0].style.display = "none";
        toBeDeletedList.style.display = "none";
    }
}

function sortBoxes(parentDiv) {
    const boxes = Array.from(parentDiv.querySelectorAll(".box"));
    boxes.sort((a, b) => a.children[1].textContent.localeCompare(b.children[1].textContent));
    parentDiv.innerHTML = "";
    boxes.forEach(box => {
        parentDiv.appendChild(box);
    });
}

async function getListOfDates() {
    const response = await fetch(endpoints.listOfDates);
    const data = await response.json();

    for (let i = 0; i < data.length; i++) {
        const option = document.createElement("option");
        option.value = data[i];
        option.text = data[i];
        dates.appendChild(option);
    }
}
