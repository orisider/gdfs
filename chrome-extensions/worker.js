// Define the FormData object for the Web worker:
importScripts('xhr2-FormData.js')

// Note: In a Web worker, the global object is called "self" instead of "window"
self.onmessage = function(event) {
  var fileInput = window.form.querySelector('input[type="file"]');
  var file = fileInput.files[0];
  if (!file) return; // No file selected

  var fileReader = new FileReader();
  fileReader.onload = function() {
    var arraybuffer = fileReader.result;
    // To manipulate an arraybuffer, wrap it in a view:
    var view = new Uint8Array(arraybuffer);
    view[0] = 0; // For example, change the first byte to a NULL-byte

    // Create an object which is suitable for use with FormData
    var blob = new Blob([view], {type: file.type});

    // Now, the form reconstruction + upload part:
    var formData = new FormData();
    formData.append(fileInput.name, blob, file.name);
    // ... handle remainder of the form ...

    // Now, submit the form
    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'https://www.googleapis.com/upload/drive/v2/files?uploadType=media');
    xhr.onload = function() {
      // Do something. For example:
      alert(xhr.responseText);
    };
    xhr.onerror = function() {
      console.log(xhr); // Aw. Error. Log xhr object for debugging
    }
    xhr.send(formData);
  };
  fileReader.readAsArrayBuffer(file);
};