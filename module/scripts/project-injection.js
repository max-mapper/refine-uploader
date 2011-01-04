var upload = function() { 
  Refine.postProcess(
      "uploader",
      "uploader",
      {},{},{},
      { 
          "onDone": function(response) {
              console.log(response);
          }
      }
  );
}

var showUploaderDialog = function() {
  var dialog = $(DOM.loadHTML("uploader", "scripts/uploader-dialog.html"));

  var elmts = DOM.bind(dialog);
  elmts.dialogHeader.text("Data Uploader");

  var level = DialogSystem.showDialog(dialog);

  elmts.okButton.click(function() {
      DialogSystem.dismissUntil(level - 1);
  }); 
};

MenuSystem.insertAfter(
    ExporterManager.MenuItems,
    [ "core/export-templating" ],
    {
      "id": "uploader/uploader",
      "label": "Upload...",
      "click": showUploaderDialog
    }
);

