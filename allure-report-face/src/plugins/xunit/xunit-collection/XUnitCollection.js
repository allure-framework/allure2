import {Collection} from 'backbone';

export default class XUnitCollection extends Collection {
    url = 'data/xunit.json';

    parse({time, testSuites}) {
        this.time = time;
        this.statistic = testSuites.reduce((statistic, testsuite) => {
            ['passed', 'pending', 'canceled', 'broken', 'failed', 'total'].forEach(function(status) {
                if(!statistic[status]) {
                    statistic[status] = 0;
                }
                statistic[status] += testsuite.statistic[status];
            });
            return statistic;
        }, {});
        return testSuites;
    }
}
