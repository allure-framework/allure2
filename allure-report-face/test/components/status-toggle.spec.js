import settings from 'util/settings.js';
import StatusToggleView from 'components/status-toggle/StatusToggleView';

describe('StatusToggle', function() {
    beforeEach(function() {
        settings.set('visibleStatuses', {FAILED: true, BROKEN: true});
        this.view = new StatusToggleView().render();
        this.el = this.view.$el;
    });

    it('should render buttons according to settings', function() {
        expect([...this.el.find('.button')].map(button => button.textContent))
            .toEqual(['Failed', 'Broken', 'Canceled', 'Pending', 'Passed']);
        expect([...this.el.find('.button_active')].map(button => button.textContent)).toEqual(['Failed', 'Broken']);
    });

    it('should update model on click', function() {
        const passsed = this.el.find('.status-toggle__button_status_PASSED');
        passsed.click();
        expect(settings.get('visibleStatuses')).toEqual({FAILED: true, BROKEN: true, PASSED: true});

        passsed.click();
        expect(settings.get('visibleStatuses')).toEqual({FAILED: true, BROKEN: true, PASSED: false});
    });
});
