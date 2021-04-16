"use strict";
module.exports = function(input) {
  const el = document.createElement("textarea");

  el.value = input;

  // Prevent keyboard from showing on mobile
  el.setAttribute("readonly", "");

  el.style.contain = "strict";
  el.style.position = "absolute";
  el.style.left = "-9999px";
  el.style.fontSize = "12pt"; // Prevent zooming on iOS

  const selection = getSelection();
  let originalRange = false;
  if (selection.rangeCount > 0) {
    originalRange = selection.getRangeAt(0);
  }

  document.body.appendChild(el);
  el.select();

  let success = false;
  try {
    success = document.execCommand("copy");
  } catch (err) {
    // do nothing
  }

  document.body.removeChild(el);

  if (originalRange) {
    selection.removeAllRanges();
    selection.addRange(originalRange);
  }

  return success;
};
