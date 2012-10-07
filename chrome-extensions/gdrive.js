/**
 * Created with JetBrains WebStorm.
 * User: outsider
 * Date: 12. 9. 7.
 * Time: 오후 7:41
 */
(function() {
  var oAuthToken;
  chrome.extension.sendRequest({type:'oauthToken'}, function(value) {
    oAuthToken = value;
    //console.log(oAuthToken);
  });

  // distinguish the service
  function findService(url) {
    if (url.match(/facebook/g)) {
      return "facebook";
    } else if (url.match(/goolge/g)) {
      return "gplus";
    } else if (url.match(/twitter/g)) {
      return "twitter"
    }
  }

  // initialize overlay for drop file
  var overlay = $('<div>').addClass('gdnfOverlayForDnD').html('Drop file here');
  $(document.body).append(overlay);
  $(overlay).on('dragleave', overlayDragLeaveHandler);
  $(overlay).on('drop', overlayDropHandler);

  // initialize tooltip
  var tooltipMsg = '<img src="' + chrome.extension.getURL('img/ajax-loader.gif') + '">';
  $.fn.tipsy.defaults = {
    delayIn: 0,      // delay before showing tooltip (ms)
    delayOut: 0,     // delay before hiding tooltip (ms)
    fade: true,     // fade tooltips in/out?
    fallback: '',    // fallback text to use when no tooltip text
    gravity: 'n',    // gravity
    html: true,     // is tooltip content HTML?
    live: false,     // use live event support?
    offset: 0,       // pixel offset of tooltip from element
    opacity: 0.8,    // opacity of tooltip
    title: function() {return tooltipMsg;},  // attribute/callback containing tooltip text
    trigger: 'manual' // how tooltip is triggered - hover | focus | manual
  };

  $(overlay).tipsy();

  // initialize drag event in text input place
  var targetInput;
  var service = findService(location.href);
  console.log('service: ', service);
  if (service === 'facebook') {
    var targetAreas = $('textarea');
    targetAreas.on('dragenter', dragEnterHandler);  function dragEnterHandler(evt) {
      $(overlay)
        .width($(evt.target).outerWidth())
        .height($(evt.target).outerHeight())
        //.css('line-height', $(box).outerHeight())
        .show();

      $(overlay).offset($(evt.target).offset());
      targetInput = evt.target;
    }
  } else if (service === 'gplus') {
  } else if (service === 'twitter') {
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
      uploadFileToGDrive(files[0]);

      $(overlay).tipsy("show");
    }

    overlayDragLeaveHandler();
  }

  function uploadFileToGDrive(file) {
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

      hasGdfsFolder();

      function hasGdfsFolder() {
        $.ajax({
          url: 'https://www.googleapis.com/drive/v2/files',
          type: 'GET',
          headers: {
            "Authorization": "Bearer " + oAuthToken
          },
          data: {
            "q": "title = 'gdfs' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
          },
          cache: false,
          contentType: 'application/json',
          processData: true,
          dataType: 'json',
          success: function(data, textStatus, jqXHR) {
            console.log('sucess checking gdfs folder');
            console.log(data);
            if (data.items.length > 0) {
              console.log('gdfs folder exist');
              uploadFile(data.items[0].id);
            } else {
              console.log('gdfs folder doesn\'t exist');
              createGdfsFolder();
            }
          },
          error: function(jqXHR, textStatus, error) { },
          complete: function() { }
        });
      }

      function createGdfsFolder() {
        $.ajax({
          url: 'https://www.googleapis.com/drive/v2/files',
          type: 'POST',
          headers: {
            "Authorization": "Bearer " + oAuthToken
          },
          data: JSON.stringify({
            "title": "gdfs",
            "mimeType": "application/vnd.google-apps.folder"
          }),
          cache: false,
          contentType: 'application/json',
          processData: false,
          dataType: 'json',
          success: function(data, textStatus, jqXHR) {
            console.log('folder creaded');
            console.log(data);
            uploadFile(data.id);
          },
          error: function(jqXHR, textStatus, error) { },
          complete: function() { }
        });
      }

      function uploadFile(parentId) {
        var sessionUrl;
        var gdSession = $.ajax({
          url: 'https://www.googleapis.com/upload/drive/v2/files?uploadType=resumable',
          type: 'POST',
          headers: {
            "Authorization": "OAuth " + oAuthToken,
            "X-Upload-Content-Type": file.type
            //"X-Upload-Content-Length": blob.size
          },
          data: JSON.stringify({
            "title": file.name,
            "mimeType": file.type,
            "parents": [{
              "kind": "drive#fileLink",
              "id": parentId
            }]
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
                "Authorization": "OAuth " + oAuthToken
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
                //$(target).sendkeys(data.webContentLink);
                console.log(targetInput);
                $(targetInput).val($(targetInput).val() + " " + data.webContentLink);
              },
              xhr: function() {  // custom xhr
                myXhr = $.ajaxSettings.xhr();
                if(myXhr.upload){ // check if upload property exists
                  myXhr.upload.addEventListener('progress', function(e) {if(e.lengthComputable) { console.log(e.loaded, e.total)}}, false); // for handling the progress of the upload
                }
                return myXhr;
              },
              error: function(jqXHR, textStatus, error) {
                console.log('upload failed 2');
              },
              complete:function() {
                console.log('second upload is completed');
                $(overlay).tipsy("hide");
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
      }
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
})();