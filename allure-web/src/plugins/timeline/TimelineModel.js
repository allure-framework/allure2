import {Model} from 'backbone';
import {flatten} from 'underscore';

export default class TimelineModel extends Model {
    url = 'data/timeline.json';

    getAllTestcases() {
        if(!this.allTestcases) {
            this.allTestcases = flatten(this.get('children')
                .map(host =>
                    host.children.map(thread => thread.children)
                )
            );
        }
        return this.allTestcases;
    }
}
