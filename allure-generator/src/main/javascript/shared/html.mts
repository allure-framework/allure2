const escapeMap = {
  "&": "&amp;",
  "<": "&lt;",
  ">": "&gt;",
  '"': "&quot;",
  "'": "&#x27;",
  "`": "&#x60;",
  "=": "&#x3D;",
};

const badChars = /[&<>"'`=]/g;

export const escapeHtml = (value: unknown): string => {
  if (value === null || typeof value === "undefined") {
    return "";
  }

  return String(value).replace(badChars, (char) => escapeMap[char as keyof typeof escapeMap]);
};

export const escapeAttr = escapeHtml;
