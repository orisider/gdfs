console.log('background.js')
var googleAuth = new OAuth2('google', {
  client_id: '718477221950-f5dnhv2q5n0oqi22gvlj9hb7thp9umov',
  client_secret: '0TzMUN26lTkgMaUWTpUvz6S2',
  api_scope: 'https://www.googleapis.com/auth/drive'
});

googleAuth.authorize(function() {
  googleAuth.getAccessToken()
});

function logout() {
  googleAuth.clearAccessToken();
};

chrome.extension.onRequest.addListener(function(request, sender, callback) {
  if (request.type = 'oauthToken') {
    //callback(oauth.getAuthorizationHeader('https://www.googleapis.com/upload/drive/v2/files?uploadType=media', 'POST'));
    callback(googleAuth.getAccessToken());
    //callback(oauth.getAccessToken())
  }
});