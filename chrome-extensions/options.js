var bgPage = chrome.extension.getBackgroundPage();

window.addEventListener("load", function(event){
  if (!bgPage.googleAuth.hasAccessToken()) {
    $('#revoke').get(0).disabled = true;
  } else {
    $('#login').get(0).disabled = true;
  }

  $('#login').click(function() {
    bgPage.login();
  });

  $('#revoke').click(function() {
    bgPage.logout();
    $('#revoke').get(0).disabled = true;
    $('#login').get(0).disabled = false;
  });
});