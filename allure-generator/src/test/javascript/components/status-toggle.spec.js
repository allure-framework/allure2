import StatusToggleView from 'components/status-toggle/StatusToggleView';
import {getSettingsForTreePlugin} from 'utils/settingsFactory';

describe('StatusToggle', function () {
    const statistic = {failed: '4', broken: '3', passed: '2', skipped: '1', unknown: '0'};
    const settings = getSettingsForTreePlugin('ALLURE_TEST');

    function StatusElement(el) {
        this.activeItems = () => el.find('.status-toggle__item').find('.y-label').toArray().map(item => item.textContent.trim());
        this.items = () => el.find('.status-toggle__item').toArray().map(item => item.textContent.trim());
        this.passed = () => el.find('.status-toggle__item').find('.n-label_status_passed, .y-label_status_passed');
    }

    beforeEach(() => {
        settings.setVisibleStatuses({failed: true, broken: true, passed: false, skipped: false, unknown: false});
        this.view = new StatusToggleView({settings, statistic});
        this.view.render();
        this.el = new StatusElement(this.view.$el);
    });

    it('should render buttons according to settings and statistics', () => {
        expect(this.el.items()).toEqual(Object.values(statistic));
        expect(this.el.activeItems()).toEqual(['4', '3']);
    });

    it('should update model on click', () => {
        this.el.passed().click();
        expect(settings.getVisibleStatuses()).toEqual({failed: true, broken: true, passed: true, skipped:false, unknown: false});

        this.el.passed().click();
        expect(settings.getVisibleStatuses()).toEqual({failed: true, broken: true, passed: false, skipped:false, unknown: false});
    });
});
