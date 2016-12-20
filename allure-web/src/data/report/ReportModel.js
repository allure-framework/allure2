import {Model} from 'backbone';

export default class ReportModel extends Model {
    url = 'data/report.json';

    fetch(...args) {
        if(!this.fetchPromise) {
            this.fetchPromise = super.fetch(...args);
        }
        return new Promise((res, rej) => this.fetchPromise.then(res, rej));
    }
}
