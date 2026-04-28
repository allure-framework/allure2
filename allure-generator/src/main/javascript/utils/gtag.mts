type GtagParams = Record<string, unknown>;

export default function gtag(event: string, params: GtagParams = {}) {
  add("event", event, params);
}

function add(...args: [string, string, GtagParams]) {
  const dataLayer = Array.isArray(window.dataLayer) ? window.dataLayer : [];
  dataLayer.push(args);
  window.dataLayer = dataLayer;
}
