const ensureReportDataReady = () =>
  new Promise(function (resolve) {
    (function waitForReady() {
      if (window.reportDataReady !== false) {
        return resolve(true);
      }
      setTimeout(waitForReady, 30);
    })();
  });

const loadReportData = (name) => {
  return ensureReportDataReady().then(function () {
    return new Promise(function (resolve, reject) {
      if (window.reportData[name]) {
        resolve(window.reportData[name]);
      } else {
        reject("not found");
      }
    });
  });
};

export const reportDataUrl = async (url, contentType = "application/octet-stream") => {
  if (window.reportData) {
    const value = await loadReportData(url);
    return `data:${contentType};base64,${value}`;
  }
  return url;
};
