import {Model} from 'backbone';

export default class SettingsModel extends Model {
    defaults() {
        return {
            language: 'en',
            testCaseSorting: {
                field: 'index',
                order: 'asc'
            },
            sidebarCollapsed: false,
            visibleStatuses: {
                failed: true,
                broken: true,
                skipped: true,
                unknown: true,
                passed: true
            },
            showGroupInfo: false
        };
    }

    fetch() {
        return new Promise(resolve => {
            const settings = window.localStorage.getItem('allure2Settings');
            if(settings) {
                this.set(JSON.parse(settings));
            }
            resolve();
        });
    }

    save(key, val) {
        this.set(key, val);
        const json = this.toJSON();
        window.localStorage.setItem('allure2Settings', JSON.stringify(json));
    }

    getVisibleStatuses(key) {
        return this.get(key) || this.get('visibleStatuses');
    }

    getTreeSorting(key) {
        return this.get(key) || {ascending: true, sorter: 0};
    }
}
