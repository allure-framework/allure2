import {Collection} from 'backbone';

export default class GraphCollection extends Collection {
    url = 'data/graph.json';

    parse({testCases}) {
        return testCases;
    }
}
