import {Collection} from 'backbone';

export default class TreeCollection extends Collection {

    parse({time, statistic, children}) {
        this.time = time;
        this.statistic = statistic;
        return children;
    }
}