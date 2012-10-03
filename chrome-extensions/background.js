var oauth = ChromeExOAuth.initBackgroundPage({
  'request_url': 'https://www.google.com/accounts/OAuthGetRequestToken',
  'authorize_url': 'https://www.google.com/accounts/OAuthAuthorizeToken',
  'access_url': 'https://www.google.com/accounts/OAuthGetAccessToken',
  'consumer_key': '718477221950.apps.googleusercontent.com',
  'consumer_secret': '5lAQh9UwBsEc_ZOW69bXFT5p',
  'scope': 'https://www.googleapis.com/auth/drive',
  'app_name': 'Google Drive Prototype'
});

function callback(resp, xhr) {
  // ... Process text response ...
};

function onAuthorized() {
  var url = "https://www.googleapis.com/drive/v2/files";
  oauth.sendSignedRequest(url, callback, {
    'parameters' : {
      'alt' : 'json',
      'max-results' : 100
    }
  });
};

function logout() {
  oauth.clearTokens();
};

oauth.authorize(onAuthorized);

chrome.extension.onRequest.addListener(function(request, sender, callback) {
  if (request.type = 'oauthToken') {
    //callback(oauth.getAuthorizationHeader('https://www.googleapis.com/upload/drive/v2/files?uploadType=media', 'POST'));
    alert(oauth.getToken());
    alert(oauth.hasToken());
    callback(oauth.getToken());
    //callback(oauth.getAccessToken())
  }
});