import { Collection } from "backbone";
import { findWhere, flatten } from "underscore";
import { values } from "../../utils/statuses";

function updateTime(timeA, timeB, field, operation) {
  if (timeB && timeB[field]) {
    timeA[field] = operation(timeA[field], timeB[field]);
  }
}

export default class TreeCollection extends Collection {
  initialize(models, { url }) {
    this.url = url;
  }

  findLeaf(parentUid, uid) {
    return findWhere(this.allResults, { parentUid, uid });
  }

  getFlattenTestResults(children) {
    return flatten(
      children.map((child) => {
        if (child.children) {
          return this.getFlattenTestResults(child.children);
        }
        return child;
      }),
    );
  }

  parse({ uid, children }) {
    const items = children || [];
    this.uid = uid;
    this.allResults = this.getFlattenTestResults(items);
    this.allNodes = items;
    this.time = this.calculateTime(this.allResults);
    this.statistic = this.calculateStatistic(this.allResults);
    return items;
  }

  applyFilterAndSorting(filter, sorter) {
    const newChildren = this.getFilteredAndSortedChildren(this.allNodes, filter, sorter);
    this.reset(newChildren);
    this.testResults = this.getFlattenTestResults(newChildren);
  }

  getFilteredAndSortedChildren(children, filter, sorter) {
    return this.calculateOrder(children)
      .map((child) => {
        if (child.children) {
          const newChildren = this.getFilteredAndSortedChildren(child.children, filter, sorter);
          return {
            ...child,
            children: newChildren,
            statistic: this.calculateStatistic(newChildren),
            time: this.calculateTime(newChildren),
          };
        }
        return child;
      })
      .filter(filter)
      .sort(sorter);
  }

  getFirstTestResult() {
    if (this.testResults.length > 0) {
      return this.testResults[0];
    }
  }

  getLastTestResult() {
    if (this.testResults.length > 0) {
      return this.testResults[this.testResults.length - 1];
    }
  }

  getNextTestResult(testResultUid) {
    const index = this.testResults.findIndex((testResult) => testResult.uid === testResultUid);
    if (index < this.testResults.length - 1) {
      return this.testResults[index + 1];
    }
  }

  getPreviousTestResult(testResultUid) {
    const index = this.testResults.findIndex((testResult) => testResult.uid === testResultUid);
    if (index > 0) {
      return this.testResults[index - 1];
    }
  }

  calculateOrder(items) {
    let index = 0;
    items.forEach((item) => {
      if (item.children) {
        this.calculateOrder(item.children);
      } else {
        item.order = ++index;
      }
    });
    return items;
  }

  calculateStatistic(items) {
    const statistic = {};
    values.forEach((value) => {
      statistic[value] = 0;
    });
    items.forEach((item) => {
      if (item.children) {
        values.forEach((value) => {
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
      stop: Number.MIN_VALUE,
    };
    items.forEach((item) => {
      if (item.children) {
        updateTime(time, item.time, "maxDuration", Math.max);
        updateTime(time, item.time, "minDuration", Math.min);
        updateTime(time, item.time, "sumDuration", (a, b) => a + b);
      } else if (item.time && isFinite(item.time.duration)) {
        time.maxDuration = Math.max(time.maxDuration, item.time.duration);
        time.minDuration = Math.min(time.minDuration, item.time.duration);
        time.sumDuration = time.sumDuration + item.time.duration;
      }
      updateTime(time, item.time, "start", Math.min);
      updateTime(time, item.time, "stop", Math.max);
      time.duration = time.stop - time.start;
    });
    return time;
  }
}
