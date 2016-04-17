import {Model} from 'backbone';
import {flatten} from 'underscore';

export default class TimelineModel extends Model {
    url = 'data/timeline.json';

    getAllTestcases() {
        if(!this.allTestcases) {
            this.allTestcases = flatten(this.get('hosts')
                .map(host =>
                    host.threads.map(thread => thread.testCases)
                )
            );
        }
        return this.allTestcases;
    }
}
