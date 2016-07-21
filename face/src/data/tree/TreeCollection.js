import {Collection} from 'backbone';

export default class TreeCollection extends Collection {

    initialize(models, {url}) {
        this.url = url;
    }

    parse({time, statistic, children}) {
        this.time = time;
        this.statistic = statistic;
        return children;
    }
}
