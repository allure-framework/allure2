import {Model} from 'backbone';
import {flatten} from 'underscore';

export default class TimelineModel extends Model {
    url = 'data/timeline.json';

    getAllTestcases() {
        if(!this.allTestcases) {
            this.allTestcases = flatten(this.get('children')
                .map(host =>
                    host.children.map(thread => thread.children)
                )
            );
        }
        return this.allTestcases;
    }

    getTestcases(host) {
        const startTime = this.get('time').start;
        return host.children.reduce((testcases, thread) =>
            testcases.concat(thread.children.map(({time, uid, name, status}) => {
                return {
                    uid, name, status,
                    start: time.start - startTime,
                    stop: time.stop - startTime,
                    thread: thread.name
                };
            })),
            []
        );
    }

    getFilteredData(minDuration){
        var data = this.get('children');
        var total = this.get('statistic').total;
        this.data = {
            children: data.map(host => {
                return Object.assign({}, host, { children: host['children'].map(thread => {
                    return Object.assign({}, thread, { children: thread['children'].filter(testCase => {
                            if (testCase.time.duration >= minDuration){
                                return true;
                            } else {
                                total--;
                                return false;
                            }
                        })
                    });
                }).filter(d=> {return d.children.length;})});
            }).filter(d=> {return d.children.length;}),
            selectedTestCases: total};
        return this.data;
    }
}