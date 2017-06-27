import {Collection} from 'backbone';
import {flatten} from 'underscore';
import {values} from '../../util/statuses';
import getComparator from './comparator';

export default class TreeCollection extends Collection {

    initialize(models, {url}) {
        this.url = url;
    }

    getFlattenTestcases(children) {
        return flatten(children.map(child => {
            if (child.children) {
                return this.getFlattenTestcases(child.children);
            }
            return child;
        }));
    }

    parse({children}) {
        const items = children || [];
        this.allTestcases = this.getFlattenTestcases(items);
        this.allNodes = items;
        this.time = this.calculateTime(this.allTestcases);
        this.statistic = this.calculateStatistic(this.allTestcases);
        return items;

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
                if (child.children) {
                    const newChildren = this.getFilteredAndSortedChildren(child.children, statuses, sorter);
                    return {
                        ...child,
                        children: newChildren,
                        statistic: this.calculateStatistic(newChildren),
                        time: this.calculateTime(newChildren)
                    };
                }
                return child;
            })
            .filter(child => {
                if (child.children) {
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

    calculateStatistic(items) {
        const statistic = {};
        values.forEach(value => {
            statistic[value] = 0;
        });
        items.forEach(item => {
            if (item.children) {
                values.forEach(value => {
                    statistic[value] += item.statistic ? item.statistic[value] : 0;

                });
            } else {
                statistic[item.status]++;
            }
        });
        return statistic;
    }

    calculateTime(items) {
        const time = {
            maxDuration: Number.MIN_VALUE,
            minDuration: Number.MAX_VALUE,
            sumDuration: 0,
            duration: 0,
            start: Number.MAX_VALUE,
            stop: Number.MIN_VALUE
        };
        items.forEach(item => {
            if (item.children) {
                TreeCollection.updateTime(time, item.time, 'maxDuration', Math.max);
                TreeCollection.updateTime(time, item.time, 'minDuration', Math.min);
                TreeCollection.updateTime(time, item.time, 'sumDuration', (a, b) => a + b);
            } else if (item.time && item.time.duration) {
                time.maxDuration = Math.max(time.maxDuration, item.time.duration);
                time.minDuration = Math.min(time.minDuration, item.time.duration);
                time.sumDuration = time.sumDuration + item.time.duration;
            }
            TreeCollection.updateTime(time, item.time, 'start', Math.min);
            TreeCollection.updateTime(time, item.time, 'stop', Math.max);
            time.duration = time.stop - time.start;
        });
        return time;
    }

    static updateTime(timeA, timeB, field, operation) {
        if (timeB && timeB[field]) {
            timeA[field] = operation(timeA[field], timeB[field]);
        }
    }
}
