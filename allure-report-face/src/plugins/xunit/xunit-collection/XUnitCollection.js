import {Collection} from 'backbone';

export default class XUnitCollection extends Collection {
    url = 'data/xunit.json';

    parse({time, statistic, testSuites}) {
        this.time = time;
        this.statistic = statistic;
        return testSuites;
    }
}
