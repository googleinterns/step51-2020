/* Set the width of the side navigation to 250px and the left margin of the page content to 250px and add a black background color to body */
function openNav() {
  document.getElementById("mySidenav").style.width = "250px";
  document.getElementById("main").style.marginLeft = "250px";
  document.body.style.backgroundColor = "rgba(0,0,0,0.4)";
}

/* Set the width of the side navigation to 0 and the left margin of the page content to 0, and the background color of body to white */
function closeNav() {
  document.getElementById("mySidenav").style.width = "0";
  document.getElementById("main").style.marginLeft = "0";
  document.body.style.backgroundColor = "white";
}

/* Checks user's login status and sets logout link accordingly */
function getLogsStatus() {

    fetch('/userapi').then(response => response.json()).then(logStatus => {
        const link = document.getElementById("logout-link");

        if(logStatus.isLoggedIn) {

            console.log('User is Logged In');
            link.href = logStatus.Url;
        }
        else {
            console.log('User is not Logged In');
            link.href = '../index.html';
        }
    });
}