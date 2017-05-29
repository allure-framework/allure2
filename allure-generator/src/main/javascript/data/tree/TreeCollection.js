import {Collection} from 'backbone';
import {flatten} from 'underscore';
import getComparator from './comparator';

export default class TreeCollection extends Collection {

    initialize(models, {url}) {
        this.url = url;
    }

    getFlattenTestcases(children) {
        return flatten(children.map(child => {
            if (child.type === 'TestGroupNode') {
                return this.getFlattenTestcases(child.children);
            }
            return child;
        }));
    }

    parse({time, statistic, children}) {
        this.allTestcases = this.getFlattenTestcases(children);
        this.allNodes = children;
        this.time = time;
        this.statistic = statistic;
        return children;
    }

    applyFilterAndSorting(statuses, sortSettings) {
        const newChildren = this.getFilteredAndSortedChildren(
            this.allNodes,
            statuses,
            getComparator(sortSettings)
        );
        this.reset(newChildren);
        this.testcases = this.getFlattenTestcases(newChildren);
    }

    getFilteredAndSortedChildren(children, statuses, sorter) {
        return children
            .map(child => {
                if (child.type === 'TestGroupNode') {
                    return {
                        ...child,
                        children: this.getFilteredAndSortedChildren(child.children, statuses, sorter)
                    };
                }
                return child;
            })
            .filter(child => {
                if (child.type === 'TestGroupNode') {
                    return child.children.length > 0;
                }
                return statuses[child.status];
            })
            .sort(sorter);
    }

    getNextTestcase(testcaseUid) {
        const index = this.testcases.findIndex(testcase => testcase.uid === testcaseUid);
        if (index < this.testcases.length - 1) {
            return this.testcases[index + 1];
        }
    }

    getPreviousTestcase(testcaseUid) {
        const index = this.testcases.findIndex(testcase => testcase.uid === testcaseUid);
        if (index > 0) {
            return this.testcases[index - 1];
        }
    }
}
