/*eslint-env jasmine*/
import proxyquire from 'proxyquire';
import {history} from 'backbone';

const router = proxyquire('./router', {});

describe('router', function() {
    beforeEach(function() {
        history.start();
    });
    afterEach(function() {
        history.stop();
    });

    it('should transit to new route and trigger event', function() {
        router.to('404');
        const onHome = jasmine.createSpy('onHome');
        router.on('route:home', onHome);
        router.to('');
        expect(onHome).toHaveBeenCalled();
    });

    it('should parse url params', function() {
        router.toUrl('project?tab=test');
        expect(router.getUrlParams()).toEqual({tab: 'test'});
    });

    it('should update search query without route change', function() {
        router.toUrl('project?tab=test');
        const onChange = jasmine.createSpy('onChange');
        router.on('route', onChange);
        router.setSearch({tab: 'next'});
        expect(onChange).not.toHaveBeenCalled();
        expect(router.getCurrentUrl()).toEqual('project?tab=next');
    });
});
