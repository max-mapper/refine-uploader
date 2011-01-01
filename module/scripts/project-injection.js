var upload = function() { 
  body = {};
  updateOptions = {};

  Refine.postProcess(
      "uploader",
      "uploader",
      {},
      body,
      updateOptions,
      {}
  );
}

MenuSystem.insertAfter(
    ExporterManager.MenuItems,
    [ "core/export-templating" ],
    {
      "id": "uploader/uploader",
      "label": "Upload...",
      "click": upload
    }
);

