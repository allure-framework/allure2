import {Collection} from 'backbone';
import {flatten} from 'underscore';

export default class TreeCollection extends Collection {

    initialize(models, {url}) {
        this.url = url;
    }

    getFlattenTestcases(children) {
        return flatten(children.map(child => {
            if(child.type === 'TestGroupNode') {
                return this.getFlattenTestcases(child.children)
            }
            return child;
        }))
    }

    parse({time, statistic, children}) {
        this.testcases = this.getFlattenTestcases(children);
        this.time = time;
        this.statistic = statistic;
        return children;
    }


    getNextTestcase(testcaseUid) {
        const index = this.testcases.findIndex(testcase => testcase.uid === testcaseUid);
        if(index < this.testcases.length - 1) {
            return this.testcases[index + 1];
        }
    }

    getPreviousTestcase(testcaseUid) {
        const index = this.testcases.findIndex(testcase => testcase.uid === testcaseUid);
        if(index > 0) {
            return this.testcases[index - 1];
        }
    }
}
