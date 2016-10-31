import {findWhere} from 'underscore';
import {Model} from 'backbone';

export default class TestcaseModel extends Model {
    get idAttribute() {
        return 'uid';
    }

    initialize() {
        this.on('sync', this.updateAttachments, this);
    }

    updateAttachments() {
        function orEmpty(items) {
            return items ? items : [];
        }
        function collectAttachments(steps) {
            return orEmpty(steps).reduce((attachments, step) => {
                return attachments.concat(collectAttachments(step.steps), orEmpty(step.attachments));
            }, []);
        }
        this.allAttachments = collectAttachments(this.get('steps'))
            .concat(orEmpty(this.get('attachments')));
    }

    getAttachment(uid) {
        return findWhere(this.allAttachments, {uid});
    }

    url() {
        return `data/test-cases/${this.id}.json`;
    }
}
