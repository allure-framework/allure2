import {findWhere} from 'underscore';
import {Model} from 'backbone';
import {makeArray} from '../../util/arrays';

export default class TestResultModel extends Model {
    get idAttribute() {
        return 'uid';
    }

    initialize() {
        this.on('sync', this.updateAttachments, this);
    }

    updateAttachments() {
        function collectAttachments({steps, attachments}) {
            return makeArray(steps)
                .reduce((result, step) => result.concat(collectAttachments(step)), [])
                .concat(makeArray(attachments));
        }
        this.allAttachments = makeArray(this.get('beforeStages'))
            .concat(makeArray(this.get('testStage')))
            .concat(makeArray(this.get('afterStages')))
            .reduce((result, stage) => result.concat(collectAttachments(stage)), []);
    }

    getAttachment(uid) {
        return findWhere(this.allAttachments, {uid});
    }

    url() {
        return `data/test-cases/${this.id}.json`;
    }
}
