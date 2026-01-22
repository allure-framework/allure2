(function init() {
  const openBtn = document.getElementById("open");
  const traceEl = document.getElementById("trace");
  const errorEl = document.getElementById("error");

  const params = new URLSearchParams(window.location.search || "");
  const traceParam = params.get("trace");

  if (traceParam) {
    try {
      traceEl.textContent = decodeURIComponent(traceParam);
    } catch (e) {
      traceEl.textContent = traceParam;
    }
  } else {
    traceEl.textContent = "No trace URL provided";
    openBtn.disabled = true;
  }

  function setError(text) {
    errorEl.hidden = !text;
    errorEl.textContent = text || "";
  }

  openBtn.addEventListener("click", () => {
    setError("");
    if (!traceParam) {
      setError("Missing trace URL. The report did not provide ?trace=... parameter.");
      return;
    }

    const viewerUrl = `https://trace.playwright.dev/?trace=${traceParam}`;
    const win = window.open(viewerUrl, "_blank", "noopener,noreferrer");
    if (!win) {
      setError("Popup blocked. Allow popups for this report to open the trace viewer.");
    }
  });
})();
