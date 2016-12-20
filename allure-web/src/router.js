import {omit} from 'underscore';
import {Router, history} from 'backbone';
import urlLib from 'url';

class AppRouter extends Router {
    constructor() {
        super({
            routes: {
                '*default': 'notFound'
            }
        });
        this.on('route', this.onRouteChange, this);
    }

    onRouteChange() {
        this.lastUrl = this.getCurrentUrl();
    }

    getCurrentUrl() {
        return history.getFragment();
    }

    reload() {
        history.loadUrl(this.getCurrentUrl());
    }

    to(pathname, query, options) {
        query = omit(query, value => value === null);
        const url = urlLib.format({pathname, query});
        return this.toUrl(url, options);
    }

    toUrl(url, options) {
        return this.navigate(url, Object.assign({trigger: true}, options));
    }

    setSearch(search) {
        const {pathname} = urlLib.parse(this.getCurrentUrl());
        return this.to(pathname, search);
    }

    getUrlParams() {
        const parsed = urlLib.parse(this.getCurrentUrl(), true);
        return parsed ? parsed.query : {};
    }
}

export default new AppRouter();
