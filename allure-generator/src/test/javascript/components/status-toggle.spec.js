import settings from 'util/settings';
import StatusToggleView from 'components/status-toggle/StatusToggleView';

describe('StatusToggle', function () {
    const statusesKey = 'testStatusKey';

    function StatusElement(el) {
        this.activeItems = () => el.find('.status-toggle__item_active').toArray().map(item => item.textContent);
        this.items = () => el.find('.status-toggle__item').toArray().map(item => item.textContent);
        this.passed = () => el.find('.status-toggle__item_status_passed');
    }

    beforeEach(function () {
        settings.set(statusesKey, {failed: true, broken: true, passed: false});
        this.view = new StatusToggleView({statusesKey});
        this.view.setContent();
        this.view.render();
        this.el = new StatusElement(this.view.$el);
    });

    xit('should render buttons according to settings', function () {
        expect(this.el.items()).toEqual(['failed', 'broken', 'passed', 'skipped', 'unknown']);
        expect(this.el.activeItems()).toEqual(['failed', 'broken']);
    });

    it('should update model on click', function () {
        this.el.passed().click();
        expect(settings.get(statusesKey)).toEqual({failed: true, broken: true, passed: true});

        this.el.passed().click();
        expect(settings.get(statusesKey)).toEqual({failed: true, broken: true, passed: false});
    });
});
