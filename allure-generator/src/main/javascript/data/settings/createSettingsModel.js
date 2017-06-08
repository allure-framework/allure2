import {Model} from 'backbone';


export default function createSettingsModel(pluginName, defaults) {
  let storageKey = 'allure2Settings';
  if(pluginName) {
    storageKey += `-${pluginName}`;
  }

  return class SettingsModel extends Model {

    defaults() {
      return defaults;
    }

    fetch() {
      return new Promise(resolve => {
        const settings = window.localStorage.getItem(storageKey);
        if(settings) {
          this.set(JSON.parse(settings));
        }
        resolve();
      });
    }

    save(key, val) {
      this.set(key, val);
      const json = this.toJSON();
      window.localStorage.setItem(storageKey, JSON.stringify(json));
    }

    getVisibleStatuses(key) {
      return this.get(key) || this.get('visibleStatuses');
    }

    getTreeSorting(key) {
      return this.get(key) || {ascending: true, sorter: 'sorter.name'};
    }
  };
}

