import {Model} from 'backbone';
import {findWhere} from 'underscore';

export default class BehaviorsModel extends Model {
    url = 'data/behaviors.json';

    getBehavior(uid) {
        const behavior = this.get('features').reduce((behavior, feature) => {
            return behavior || findWhere(feature.stories, {uid});
        }, null);
        if(!behavior) {
            throw new Error('Unable to find story ' + uid);
        }
        return new Model(behavior);
    }
}
