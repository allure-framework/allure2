import {findWhere} from 'underscore';
import {Model} from 'backbone';
import {makeArray} from '../../utils/arrays';


function collectAttachments({steps, attachments}) {
    return makeArray(steps)
        .reduce((result, step) => result.concat(collectAttachments(step)), [])
        .concat(makeArray(attachments));
}


export default class TestResultModel extends Model {
    get idAttribute() {
        return 'uid';
    }

    parse(testResult) {
        this.allAttachments = makeArray(testResult.beforeStages)
            .concat(makeArray(testResult.testStage))
            .concat(makeArray(testResult.afterStages))
            .reduce((result, stage) => result.concat(collectAttachments(stage)), []);
        return testResult;
    }


    getAttachment(uid) {
        return findWhere(this.allAttachments, {uid: uid.toString()});
    }

    url() {
        return `data/test-cases/${this.id}.json`;
    }
}
