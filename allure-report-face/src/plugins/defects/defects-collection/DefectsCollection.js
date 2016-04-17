import {Collection} from 'backbone';

export default class DefectsCollection extends Collection {
    url = 'data/defects.json';

    initialize() {
        this.on('sync', this.updateDefects, this);
    }

    updateDefects() {
        this.each(type => {
            type.get('defects').forEach(defect => {
                defect.status = type.get('status');
            });
        });
        this.allDefects = [].concat(...this.pluck('defects'));
    }

    parse({defectsList}) {
        return defectsList;
    }
}
