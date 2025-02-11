const {host, hostname, href, origin, pathname, port, protocol, search} = window.location;
console.log(
    "host: " + host +
    "\nhostname: " + hostname +
    "\nhref: " + href +
    "\norigin: " + origin +
    "\npathname: " + pathname +
    "\nport: " + port +
    "\nprotocol: " + protocol +
    "\nsearch: " + search
);

const taskToDestroyEndpoint = "/deletetask";
const curentScheduledTasks = "/emailtasks";
const retrieveDirEndpoint = "/ytd";
const multiListsEndpoint = "/yt";
const taskArrayEndpoint = "/tasks";
const sendTaskEndpoint = "/emailtask";
const uniListEndpoint = "/yte";

document.onload = getDirectories();
document.onload = getActions();

/**
 * Modal handling
 */
const modalBoxArray = document.getElementsByClassName("modal");
const openModalButtonArray = document.getElementsByClassName("glow-box");
const modalBackgroundArray = document.getElementsByClassName("modal-background");
const closeModalButtonArray = document.getElementsByClassName("close-modal");
const appendButtonArray = document.getElementsByClassName("append-button");
const midWrapperArray = document.getElementsByClassName("mid-wrapper");
// const accordionButton = document.getElementsByClassName("accordion");
const goBackArray = document.getElementsByClassName("go-back");
const botArray = document.getElementsByClassName("bot-modal");

for (let i = 0; i < modalBoxArray.length; i++) {
    openModalButtonArray[i].addEventListener("click", function () {
        modalBoxArray[i].classList.add("scale-in");
        modalBackgroundArray[i].style.display = "grid";

        if (i == 1) {
            getDirectories();
        }
    });

    closeModalButtonArray[i].addEventListener("click", function () {
        modalBoxArray[i].classList.remove("scale-in");
        modalBoxArray[i].classList.add("scale-out");
        setTimeout(function () {
            modalBackgroundArray[i].style.display = "none";
            modalBoxArray[i].classList.remove("scale-out");
        }, 300);
    });

    appendButtonArray[i].addEventListener("click", function () {
        botArray[i].style.display = "none";
        midWrapperArray[i].style.display = "flex";
        closeModalButtonArray[i].style.display = "none";
        goBackArray[i].style.display = "var(--fa-display, inline-block)";
    });
    
    goBackArray[i].addEventListener("click", function () {
        midWrapperArray[i].style.display = "none";
        botArray[i].style.display = "flex";
        goBackArray[i].style.display = "none";
        closeModalButtonArray[i].style.display = "var(--fa-display, inline-block)";
    });

    // accordionButton[i].addEventListener("click", function () {
    //     this.classList.toggle("active");
    //     if (this.classList.contains("fa-caret-down")) {
    //         this.classList.remove("fa-caret-down");
    //         this.classList.add("fa-caret-up");
    //     } else {
    //         this.classList.remove("fa-caret-up");
    //         this.classList.add("fa-caret-down");
    //     }
        
    //     let panel = this.previousElementSibling;
    //     let p = panel.previousElementSibling;
    //     if (panel.style.display === "block") {
    //         panel.style.display = "none";
    //         p.style.display = "block";
    //     } else {
    //         p.style.display = "none";
    //         panel.style.display = "block";
    //     }
    // });
}

const plusButton = document.getElementsByClassName("fa-plus");

/**
 * First modal
 */
let listItems = [];
let input1 = document.querySelector(".textInput");

input1.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
        addToFirstList();
    }
});

function addToFirstList() {
    if (input1.value.trim() === "") {
        alert("Please enter values for each field.");
        return;
    }

    let text = input1.value.trim();
    if (text !== "") {
        listItems.push(text);
        input1.value = ""; // Clear the text input

        plusButton[0].classList.add("rotate-quarter");
        setTimeout(function () {
            let list = document.querySelector(".text-list");
            let listItem = document.createElement("li");
            listItem.appendChild(document.createTextNode(text));
            list.appendChild(listItem);
            // Apply flip-in animation to the new list item
            listItem.classList.add("flip-in");

            plusButton[0].classList.remove("rotate-quarter");
        }, 300);
    }
}

async function sendFirstList() {
    const result = await sendFirstListToServer(listItems);
    clearFirstList(result.report);
}

async function sendFirstListToServer() {
    if (listItems.length === 0) {
        alert("The list is empty. Please add items to the list first.");
        return;
    }

    let data = {
        links: listItems
    };

    // Sending the list with AJAX call
    let response = await fetch(uniListEndpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });
    if (response.status === 200) {
        let result = await response.json();
        console.log(listItems);
        console.log(result);
        return result;
    } else {
        alert("Something went wrong");
    }
}

function clearFirstList(msg) {
    listItems = []; // Clear the array list

    // Clear the items list with animation
    let list = document.querySelector(".text-list");
    let listItemsToClear = list.getElementsByTagName("li");
    for (let i = 0; i < listItemsToClear.length; i++) {
        listItemsToClear[i].classList.remove("flip-in");
        listItemsToClear[i].classList.add("flip-out");
    }

    useTypewriter(list, 0, msg);
    result = null;
}

/**
 * Second modal
*/
let treeData = {};
let input2 = document.querySelectorAll(".textInput")[1];

input2.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
        addToSecondList();
    }
});

async function getDirectories() {
    directoryList.innerHTML = `<option value="" disabled selected hidden>Choose directory...</option>`;
    let response = await fetch(retrieveDirEndpoint);
    let data = await response.json();

    const directories = data.map(item => item.name);
    directories.forEach(directory => {
        let option = document.createElement("option");
        option.text = directory;
        option.value = directory;
        directoryList.appendChild(option);
    });
}

function addToSecondList() {
    if (input2.value.trim() === "") {
        alert("Please enter values for each field.");
        return;
    }

    let dropdown = document.getElementById("directoryList");
    let chosenDirectory = dropdown.value;
    
    if (chosenDirectory === "") {
        alert("Please select an element from the dropdown.");
        return;
    }
    
    let text = input2.value.trim();
    if (text !== "") {
        input2.value = ""; // Clear the text input

        plusButton[1].classList.add("rotate-quarter");
        setTimeout(function () {
            let list = document.querySelectorAll(".text-list")[1];

            if (!treeData[chosenDirectory]) {
                treeData[chosenDirectory] = [];

                // Create directory nested ul
                let nestedLi = document.createElement("li");
                nestedLi.appendChild(document.createTextNode(chosenDirectory));
                nestedLi.classList.add("dir", "flip-in");
                let nestedUl = document.createElement("ul");
                nestedLi.appendChild(nestedUl);
                list.appendChild(nestedLi);
            }

            let inners = list.getElementsByClassName("dir");

            for (let li of inners) {
                if (li.outerText.substring(0, chosenDirectory.length) === chosenDirectory) {
                    let nestedUl = li.querySelector("ul");
                    let listItem = document.createElement("li");
                    listItem.classList.add("tab", "flip-in");

                    nestedUl.childElementCount > 0 ? listItem.appendChild(document.createTextNode("├── " + text)) : listItem.appendChild(document.createTextNode("└── " + text));

                    nestedUl.prepend(listItem);

                    break;
                }
            }
            treeData[chosenDirectory].push(text);

            plusButton[1].classList.remove("rotate-quarter");
        }, 300);
    }
}

async function sendSecondList() {
    const result = await sendSecondListToServer();
    clearSecondList(result.report);
}

async function sendSecondListToServer() {
    if (Object.keys(treeData).length === 0) {
        alert("The list is empty. Please add items to the list first.");
        return;
    }

    // Sending the list with AJAX call
    let response = await fetch(multiListsEndpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(treeData)
    });
    if (response.status === 200) {
        let result = await response.json();
        console.log(treeData);
        console.log(result);
        return result;
    } else {
        alert("Something went wrong");
    }
}

function clearSecondList(msg) {
    treeData = {};

    let list = document.querySelectorAll(".text-list")[1];
    let listItemsToClear = list.getElementsByTagName("li");
    for (let i = 0; i < listItemsToClear.length; i++) {
        listItemsToClear[i].classList.remove("flip-in");
        listItemsToClear[i].classList.add("flip-out");
    }

    useTypewriter(list, 1, msg);
    result = null;
}

/**
 * Third modal
 */
let taskList = [];
let counter = 0;
let emailTemplate = {
    to: "andruycira@icloud.com",
    subject: "",
    body: "Lorem ipsum"
}

function taskTemplate() {
    timeframe = 0;
    email = {};
}

timeForTask.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
        addToThirdList();
    }
});  

function addToThirdList() {
    if (isNaN(Number(timeForTask.value)) || timeForTask.value === "") {
        alert("Please enter values for each field.");
        return;
    }

    let newTask = Object.create(taskTemplate);
    const theTime = timeForTask.value;
    const theSubject = subjectForTask.value;
    newTask.timeframe = minutesToMilliseconds(Number(theTime));
    newTask.email = Object.assign({}, emailTemplate);

    newTask.email.subject = theSubject;

    timeForTask.value = ""; // Clear the input

    plusButton[2].classList.add("rotate-quarter");
    setTimeout(function () {
        let list = document.querySelectorAll(".text-list")[2];
        let listItem = document.createElement("li");

        theSubject === "Turn AC on" ?
            listItem.appendChild(document.createTextNode(`AC will start ${pushToTaskList(newTask)}`)) :
            listItem.appendChild(document.createTextNode(`AC will stop ${pushToTaskList(newTask)}`));

        list.appendChild(listItem);
        // Apply flip-in animation to the new list item
        listItem.classList.add("flip-in");

        plusButton[2].classList.remove("rotate-quarter");
    }, 300);
}

function minutesToMilliseconds(minutes) {
    return minutes * 60000;
}

function pushToTaskList(newTask) {
    if (taskList.length > 0) {
        if (taskList[taskList.length - 1].email.subject === "Turn AC on" && newTask.email.subject === "Turn AC on") {
            newTask.timeframe += minutesToMilliseconds(120);
        }

        newTask.timeframe += taskList[taskList.length - 1].timeframe;
    }
    taskList.push(newTask);
    console.log(taskList);
    taskList.forEach(task => {
        console.log(new Date(task.timeframe + Date.now()).toLocaleString());
    })

    return new Date(newTask.timeframe + Date.now()).toLocaleString();
}    

async function sendThirdList() {
    taskList.forEach(task => {
        task.timeframe += Date.now();
    });
    const result = await sendFourthListToServer()
    clearThirdList(result.report);
}

function clearThirdList(msg) {
    taskList = [];

    let list = document.querySelectorAll(".text-list")[2];
    let listItemsToClear = list.getElementsByTagName("li");
    for (let i = 0; i < listItemsToClear.length; i++) {
        listItemsToClear[i].classList.remove("flip-in");
        listItemsToClear[i].classList.add("flip-out");
    }

    useTypewriter(list, 2, msg);
    result = null;
}

/**
 * Fourth modal
 */
async function getActions() {
    taskActions.innerHTML = `<option value="" disabled selected hidden>Choose your destiny...</option>`;
    let response = await fetch(taskArrayEndpoint);
    let data = await response.json();

    data.forEach(action => {
        let option = document.createElement("option");
        option.text = action;
        option.value = action;
        taskActions.appendChild(option);
    });
}

let time = document.getElementById("time");

time.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
        addToFourthList();
    }
});

function addToFourthList() {
    if (time.value === "" || time.value === isNaN(Number(time.value))) {
        alert("Please select an element from the dropdown.");
        return;
    }

    let dropdown = document.getElementById("taskActions");
    let action = dropdown.value;

    if (action === "") {
        alert("Please select an element from the dropdown.");
        return;
    }

    let taskObj = Object.create(taskTemplate);
    taskObj.email = Object.assign({}, emailTemplate);

    taskObj.email.subject = action;

    let timeValue = time.value;
    if (Date.parse(timeValue) === NaN || timeValue === "") {
        alert("Please enter a value for time.");
        return;
    }

    if (timeValue !== "") {
        taskObj.timeframe = Date.parse(timeValue);
        time.value = "";

        plusButton[3].classList.add("rotate-quarter");
        setTimeout(function () {
            let list = document.querySelectorAll(".text-list")[3];
            if (list.hasChildNodes()) {
                list.innerHTML = "";
            }
            let listItem = document.createElement("li");
            listItem.appendChild(document.createTextNode(
                `${action} ${Date.now() > Date.parse(timeValue) ? "(Inmediately)" : "-> " + Date.prototype.toLocaleString.call(new Date(Date.parse(timeValue)))}`
                    // Otherwise
                    // "(" + Date.prototype.toLocaleDateString.call(new Date(Date.parse(timeValue)))
                    // + " at " + Date.prototype.toLocaleTimeString.call(new Date(Date.parse(timeValue))).slice(0, -6)
                    // + Date.prototype.toLocaleTimeString.call(new Date(Date.parse(timeValue))).slice(-3) + ")"}`
            ));
            list.appendChild(listItem);
            // Apply flip-in animation to the new list item
            listItem.classList.add("flip-in");

            plusButton[3].classList.remove("rotate-quarter");
        }, 300);
    }
}

async function sendFourthList() {
    sendFourthListToServer();
    const result = await sendFourthListToServer();
    clearFourthList(result.report);
}

async function sendFourthListToServer() {
    if (taskList.length === 0) {
        alert("Please add an element to the list.");
        return;
    }

    console.log(taskList);

    // Sending the list with AJAX call
    let response = await fetch(sendTaskEndpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(taskList)
    });
    if (response.status === 200) {
        let result = await response.json();
        console.log(result);
        return result;
    } else {
        alert("Something went wrong");
    }
}

function clearFourthList(msg) {
    taskList = [];

    let list = document.querySelectorAll(".text-list")[3];
    let listItemsToClear = list.getElementsByTagName("li");
    for (let i = 0; i < listItemsToClear.length; i++) {
        listItemsToClear[i].classList.remove("flip-in");
        listItemsToClear[i].classList.add("flip-out");
    }

    useTypewriter(list, 3, msg);
    result = null;
}

/**
 * Fith modal
 */
let tasks = [];
let taskToKill = {};

async function gatherTaskList() {
    currentTasks.innerHTML = `<option value="" disabled selected hidden> Current tasks...</option>`;
    let response = await fetch(curentScheduledTasks);
    let data = await response.json();

    tasks = data;
    tasks.forEach(task => {
        let option = document.createElement("option");
        option.text = task.name + " (" + task.time + ")";
        option.value = task.id;
        currentTasks.appendChild(option);
    })
}

currentTasks.addEventListener("change", function (e) {
    if (e.key === "Enter") {
        addToFithList();
    }
});

function addToFithList() {
    if (currentTasks.value === "") {
        alert("Please select an element from the dropdown.");
        return;
    }

    let dropdown = document.getElementById("currentTasks");
    let task = dropdown.value;
    if (task === "") {
        alert("Please select an element from the dropdown.");
        return;
    }
    for (let i = 0; i < tasks.length; i++) {
        if (tasks[i].id === task) {
            taskToKill = tasks[i];
            break;
        }
    }
    console.log(taskToKill);

    plusButton[4].classList.add("rotate-quarter");
    setTimeout(function () {
        let list = document.querySelectorAll(".text-list")[4];
        if (list.hasChildNodes()) {
            list.innerHTML = "";
        }
        let listItem = document.createElement("li");
        listItem.appendChild(document.createTextNode(taskToKill.name + " (" + taskToKill.time + ")"));
        list.appendChild(listItem);
        // Apply flip-in animation to the new list item
        listItem.classList.add("flip-in");

        plusButton[4].classList.remove("rotate-quarter");
    }, 300);
}

async function sendFithList() {
    const result = await sendFithListToServer();
    clearFithList(result.report);
}

async function sendFithListToServer() {
    if (Object.keys(taskToKill).length === 0) {
        alert("The list is empty. Please add items to the list first.");
        return;
    }

    // Sending the list with AJAX call
    let response = await fetch(taskToDestroyEndpoint, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(taskToKill)
    });
    if (response.status === 200) {
        let result = await response.json();
        console.log(result);
        return result;
    } else {
        alert("Something went wrong");
    }
}

function clearFithList(msg) {
    taskToKill = {};

    let list = document.querySelectorAll(".text-list")[4];
    let listItemsToClear = list.getElementsByTagName("li");
    for (let i = 0; i < listItemsToClear.length; i++) {
        listItemsToClear[i].classList.remove("flip-in");
        listItemsToClear[i].classList.add("flip-out");
    }

    useTypewriter(list, 4, msg); // Adjust the element index here
    result = null;
}

/**
 * Typewriter animation
 */
const typying = 50;
const backspace = 10;
function animateTypewriter(element, msg, index) {
    if (index < msg.length) {
        element.textContent += msg.charAt(index);
        index++;
        setTimeout(function () {
            animateTypewriter(element, msg, index);
        }, typying); // Adjust the speed of the typewriter effect here
    }
}

function reverseAnimation(element, msg, index) {
    if (index >= 0) {
        element.textContent = msg.substring(0, index);
        index--;
        setTimeout(function () {
            reverseAnimation(element, msg, index);
        }, backspace); // Adjust the speed of the reverse effect here
    }
}

/**
 * Typewriter functionality
 */
function useTypewriter(list, element, msg) {
    // Remove list items after animation completes
    setTimeout(function () {
        list.innerHTML = "";

        // Toggle typewriter cursor on for 2.5s
        let resultText = document.querySelectorAll(".typewriter")[element];
        resultText.classList.add("show-cursor");

        // Remove the result message after 5 seconds
        setTimeout(function () {
            animateTypewriter(resultText, msg, 0)

            // Toggle typewriter cursor off after 2.5s
            setTimeout(function () {
                reverseAnimation(resultText, msg, msg.length);
                resultText.classList.remove("show-cursor");
            }, 3000 + (backspace * msg.length));
        }, 5000 + (typying * msg.length));
    }, 500);
}
