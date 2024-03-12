export default function gtag(event, params) {
  add("event", event, params);
}

function add() {
  window.dataLayer = Array.isArray(window.dataLayer) ? window.dataLayer : [];
  window.dataLayer.push(arguments);
}
