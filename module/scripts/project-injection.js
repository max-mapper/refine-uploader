var upload = function(url) { 
  Refine.postProcess(
      "uploader",
      "uploader",
      params = { "url": url },
      {},{},
      { 
          "onDone": function(response) {
              console.log(response);
          }
      }
  );
}

ExporterManager.handlers.showUploaderDialog = function() {
  var dialog = $(DOM.loadHTML("uploader", "scripts/uploader-dialog.html"));

  var elmts = DOM.bind(dialog);
  elmts.dialogHeader.text("Data Uploader");

  var level = DialogSystem.showDialog(dialog);

  elmts.uploadButton.click(function() {
    var url = elmts.serverInput[0].value;
    upload(url);
    DialogSystem.dismissUntil(level - 1);
  });
  
  elmts.cancelButton.click(function() {
    DialogSystem.dismissUntil(level - 1);
  });
  
};

MenuSystem.insertAfter(
    ExporterManager.MenuItems,
    [ "core/export-templating" ],
    {
      "id": "uploader/uploader",
      "label": "Upload...",
      "click": function() { ExporterManager.handlers.showUploaderDialog(); }
    }
);

