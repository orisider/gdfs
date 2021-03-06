var googleAuth = new OAuth2('google', {
  client_id: '718477221950-f5dnhv2q5n0oqi22gvlj9hb7thp9umov.apps.googleusercontent.com',
  client_secret: '0TzMUN26lTkgMaUWTpUvz6S2',
  api_scope: 'https://www.googleapis.com/auth/drive'
});

function login() {
  googleAuth.authorize(function() {
    googleAuth.getAccessToken();
  });
}

function logout() {
  googleAuth.clearAccessToken();
};

chrome.extension.onRequest.addListener(function(request, sender, callback) {
  if (request.type == 'oauthToken') {
    callback(googleAuth.getAccessToken());
  } else if (request.type == 'hasToken') {
    callback(googleAuth.hasAccessToken());
  }
});