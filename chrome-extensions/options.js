var bgPage = chrome.extension.getBackgroundPage();

window.addEventListener("load", function(event){
  if (!bgPage.oauth.hasToken()) {
    $('#revoke').get(0).disabled = true;
  }

  $('#revoke').click(function() {
    bgPage.logout();
    $('#revoke').get(0).disabled = true;
  });
});