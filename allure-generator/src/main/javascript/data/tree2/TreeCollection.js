import {Collection} from 'backbone';

class TreeCollection extends Collection {

    url() {
        return this.options.url;
    }

}

export default TreeCollection;