import { Model } from "backbone";

export default class LocalStorageModel extends Model {
  storageKey() {
    return "ALLURE_REPORT_SETTINGS";
  }

  fetch() {
    return new Promise((resolve) => {
      const settings = window.localStorage.getItem(this.storageKey());
      if (settings) {
        this.set(JSON.parse(settings));
      }
      resolve();
    });
  }

  save(key, val) {
    this.set(key, val);
    const json = this.toJSON();
    window.localStorage.setItem(this.storageKey(), JSON.stringify(json));
  }
}
