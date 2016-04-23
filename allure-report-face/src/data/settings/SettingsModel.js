import {Model} from 'backbone';
import {protoprop} from '../../decorators';

export default class SettingsModel extends Model {
    @protoprop
    defaults = {
        language: 'en',
        testCaseSorting: {
            field: 'index',
            order: 'asc'
        },
        sidebarCollapsed: false,
        visibleStatuses: {
            failed: true,
            broken: true,
            canceled: true,
            pending: true,
            passed: true
        }
    };

    fetch() {
        return new Promise(resolve => {
            const settings = window.localStorage.getItem('allure2Settings');
            this.set(JSON.parse(settings));
            resolve();
        });
    }

    save(key, val) {
        this.set(key, val);
        const json = this.toJSON();
        window.localStorage.setItem('allureSettings', JSON.stringify(json));
    }
}
