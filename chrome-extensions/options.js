var bgPage = chrome.extension.getBackgroundPage();

window.addEventListener("load", function(event){
  console.log(bgPage);
  console.log(bgPage.googleAuth);
  console.log(bgPage.googleAuth.hasAccessToken());

  if (!bgPage.googleAuth.hasAccessToken()) {
    $('#revoke').get(0).disabled = true;
  } else {
    $('#login').get(0).disabled = true;
  }

  $('#login').click(function() {
    bgPage.login();
  });

  $('#revoke').click(function() {
    console.log('call logout');
    bgPage.logout();
    //google.logout();
    $('#revoke').get(0).disabled = true;
    $('#login').get(0).disabled = false;
  });
});