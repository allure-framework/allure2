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
        function collectAttachments(steps) {
            return steps.reduce((attachments, step) => {
                return attachments.concat(collectAttachments(step.steps), step.attachments);
            }, []);
        }
        this.allAttachments = collectAttachments(this.get('steps')).concat(this.get('attachments'));
    }

    getAttachment(uid) {
        return findWhere(this.allAttachments, {uid});
    }

    url() {
        return `data/${this.id}-testcase.json`;
    }
}
