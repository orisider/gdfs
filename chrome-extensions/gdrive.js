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
    //console.log(oAuthToken);
  });

  // drag and drop handling
  var overlay = $('<div>').addClass('gdnfOverlayForDnD').html('Drop file here');
  $(document.body).append(overlay);

  var targetAreas = $('textarea');
  targetAreas.on('dragenter', dragEnterHandler);

  $(overlay).on('dragleave', overlayDragLeaveHandler);
  $(overlay).on('drop', overlayDropHandler);

  function dragEnterHandler(evt) {
    $(overlay)
      .width($(evt.target).outerWidth())
      .height($(evt.target).outerHeight())
      //.css('line-height', $(box).outerHeight())
      .show();

    $(overlay).offset($(evt.target).offset());
  }

  function overlayDragLeaveHandler(evt) {
    $(overlay).hide();
  }

  function overlayDropHandler(evt) {
    evt.preventDefault();
    evt.dataTransfer = evt.originalEvent.dataTransfer;

    var files = evt.dataTransfer.files;
    if (files.length > 0) {
      console.log(files);
      uploadFileToGDrive(files[0], evt.target);
    }

    overlayDragLeaveHandler();
  }

  function uploadFileToGDrive(file, target) {
    if (!file) return; // No file selected

    var fileReader = new FileReader();
    fileReader.onload = function() {
      console.log('file loaded');
      var arraybuffer = fileReader.result;

      var view = new Uint8Array(arraybuffer);
      view[0] = 0;

      // Create an object which is suitable for use with FormData
      var blob = new Blob([view], {type: file.type});

      // Now, the form reconstruction + upload part:
      var formData = new FormData();
      formData.append('file', blob, file.name);

      var sessionUrl;
      var gdSession = $.ajax({
        url: 'https://www.googleapis.com/upload/drive/v2/files?uploadType=resumable',
        type: 'POST',
        headers: {
          "Authorization": oAuthToken,
          "X-Upload-Content-Type": file.type
          //"X-Upload-Content-Length": blob.size
        },
        data: JSON.stringify({
          "title": file.name
        }),
        cache: false,
        contentType: 'application/json',
        processData: false,
        dataType: 'json',
        success: function() {
          sessionUrl = gdSession.getResponseHeader('location');
          console.log('success');

          $.ajax({
            url: sessionUrl,
            type: 'PUT',
            headers: {
              "Authorization": oAuthToken
            },
            //data: formData,
            data: file,
            cache: false,
            contentType: false,
            processData: false,
            success: function(data, textStatus, jqXHR) {
              console.log('success to uploading2');
              console.log(data);
              console.log(data.webContentLink);
              $(target).sendkeys(data.webContentLink);
              $(target).html(data.webContentLink);
            },
            error: function(jqXHR, textStatus, error) {
              console.log('upload failed 2');
            },
            complete:function() {
              console.log('second upload is completed');
            }
          });
        },
        error: function(jqXHR, textStatus, error) {
          console.log('upload failed');
          console.log(jqXHR);
          console.log(textStatus);
          console.log(error);
        },
        complete: function() {
        }
      });
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
  }






/*
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
        console.log(file.name);
        console.log(file.type);
        console.log(blob.size);
        // ... handle remainder of the form ...

        var sessionUrl;

        var gdSession = $.ajax({
          url: 'https://www.googleapis.com/upload/drive/v2/files?uploadType=resumable',
          type: 'POST',
          headers: {
            "Authorization": oAuthToken,
            "X-Upload-Content-Type": file.type
            //"X-Upload-Content-Length": blob.size
          },
          data: JSON.stringify({
            "title": file.name
          }),
          cache: false,
          contentType: 'application/json',
          processData: false,
          dataType: 'json',
          success: function() {
            sessionUrl = gdSession.getResponseHeader('location');
            console.log('success')

            $.ajax({
              url: sessionUrl,
              type: 'PUT',
              headers: {
                "Authorization": oAuthToken
              },
              //data: formData,
              data: file,
              cache: false,
              contentType: false,
              processData: false,
              success: function() {
                console.log('success to uploading');
              },
              error: function(jqXHR, textStatus, error) {
                console.log('upload failed 2');
              },
              complete:function() {
                console.log('second upload is completed');
              }
            });
          },
          error: function(jqXHR, textStatus, error) {
            console.log('upload failed');
            console.log(jqXHR);
            console.log(textStatus);
            console.log(error);
          },
          complete: function() {
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
  }*/
})();