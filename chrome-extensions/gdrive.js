/**
 * Created with JetBrains WebStorm.
 * User: outsider
 * Date: 12. 9. 7.
 * Time: 오후 7:41
 */
(function() {
  var oAuthToken;
  chrome.extension.sendRequest({type:'oauthToken'}, function(value) {
    oAuthToken = "OAuth " + value;
    //oAuthToken = "OAuth ya29.AHES6ZTpmlZVMOvQjq8hGK6p4neUFWcgYUpRytFGr223DS9gAzTQCg";
    console.log(oAuthToken);
  });
  console.log('test');


  var form = $('#fileForm');
  if (form) {
    form.submit(function(evt) {
      evt.preventDefault();

      var file = $('#file')[0].files[0];
      if (!file) return; // No file selected

      var fileReader = new FileReader();
      fileReader.onload = function() {
        console.log('file loaded');
        var arraybuffer = fileReader.result;
        // To manipulate an arraybuffer, wrap it in a view:
        var view = new Uint8Array(arraybuffer);
        view[0] = 0; // For example, change the first byte to a NULL-byte

        // Create an object which is suitable for use with FormData
        var blob = new Blob([view], {type: file.type});

        // Now, the form reconstruction + upload part:
        var formData = new FormData();
        formData.append('file', blob, file.name);
        // ... handle remainder of the form ...

        $.ajax({
          url: 'https://www.googleapis.com/upload/drive/v2/files?uploadType=media',
          type: 'POST',
          headers: {
            'Authorization': oAuthToken,
            'contentType': 'text/html',
            'Content-length': 0
          },
          data: formData,
          cache: false,
          contentType: false,
          processData: false,
          success: function() {
            console.log('success')
          },
          error: function(jqXHR, textStatus, error) {
            console.log('upload failed');
            console.log(jqXHR);
            console.log(textStatus);
            console.log(error);
          },
          complete: function() {
            console.log(this.headers)
          }
        });

        // Now, submit the form
        //var xhr = new XMLHttpRequest();
        //xhr.open('POST', 'https://www.googleapis.com/upload/drive/v2/files?uploadType=media');
        //xhr.onload = function() {
          // Do something. For example:
         // console.log('xhr loaded');
         // console.log(xhr.responseText);
        //};
        //xhr.onerror = function() {
          //console.log('xhr error');
          //console.log(xhr); // Aw. Error. Log xhr object for debugging
        //}
        //xhr.send(formData);
      };
      fileReader.onerror = function(e) {
        console.log('onerror');
        console.log(e);
      };
      fileReader.onloadend = function(e) {
        console.log('onloadend');
        console.log(e);
      };
      fileReader.readAsArrayBuffer(file);
    });
  }
})();