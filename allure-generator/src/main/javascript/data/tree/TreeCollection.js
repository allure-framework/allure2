import {Collection} from 'backbone';
import {flatten} from 'underscore';
import {values} from '../../util/statuses';

function updateTime(timeA, timeB, field, operation) {
    if (timeB && timeB[field]) {
        timeA[field] = operation(timeA[field], timeB[field]);
    }
}

export default class TreeCollection extends Collection {

    initialize(models, {url}) {
        this.url = url;
    }

    getFlattenTestResults(children) {
        return flatten(children.map(child => {
            if (child.children) {
                return this.getFlattenTestResults(child.children);
            }
            return child;
        }));
    }

    parse({children}) {
        const items = children || [];
        this.allResults = this.getFlattenTestResults(items);
        this.allNodes = items;
        this.time = this.calculateTime(this.allResults);
        this.statistic = this.calculateStatistic(this.allResults);
        return items;

    }

    applyFilterAndSorting(filter, sorter) {
        const newChildren = this.getFilteredAndSortedChildren(
            this.allNodes,
            filter,
            sorter
        );
        this.reset(newChildren);
        this.testResults = this.getFlattenTestResults(newChildren);
    }

    getFilteredAndSortedChildren(children, filter, sorter) {
        return children
            .map(child => {
                if (child.children) {
                    const newChildren = this.getFilteredAndSortedChildren(child.children, filter, sorter);
                    return {
                        ...child,
                        children: newChildren,
                        statistic: this.calculateStatistic(newChildren),
                        time: this.calculateTime(newChildren)
                    };
                }
                return child;
            })
            .filter(filter)
            .sort(sorter);
    }

    getNextTestResult(testResultUid) {
        const index = this.testResults.findIndex(testResult => testResult.uid === testResultUid);
        if (index < this.testResults.length - 1) {
            return this.testResults[index + 1];
        }
    }

    getPreviousTestResult(testResultUid) {
        const index = this.testResults.findIndex(testResult => testResult.uid === testResultUid);
        if (index > 0) {
            return this.testResults[index - 1];
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
                updateTime(time, item.time, 'maxDuration', Math.max);
                updateTime(time, item.time, 'minDuration', Math.min);
                updateTime(time, item.time, 'sumDuration', (a, b) => a + b);
            } else if (item.time && item.time.duration) {
                time.maxDuration = Math.max(time.maxDuration, item.time.duration);
                time.minDuration = Math.min(time.minDuration, item.time.duration);
                time.sumDuration = time.sumDuration + item.time.duration;
            }
            updateTime(time, item.time, 'start', Math.min);
            updateTime(time, item.time, 'stop', Math.max);
            time.duration = time.stop - time.start;
        });
        return time;
    }
}
