import './styles.scss';
import {on, className} from '../../decorators';
import template from './StatusToggleView.hbs';
import {values} from '../../utils/statuses';
import {View} from 'backbone.marionette';
import translate from '../../helpers/t';

@className('status-toggle')
class StatusToggleView extends View {
    template = template;

    initialize({settings, statistic}) {
        this.settings = settings;
        this.statistic = statistic;
        this.listenTo(settings, 'change', this.render);
    }

    serializeData() {
        const statuses = this.settings.getVisibleStatuses();
        return {
            statuses: values.map(status => ({
                status,
                statusName: translate(`status.${status}`, {}),
                active: !!statuses[status],
                count: this.statistic ? this.statistic[status.toLowerCase()] : 0
            }))
        };
    }

    @on('click .y-label, .n-label')
    onToggleStatus(e) {
        const el = this.$(e.currentTarget);
        const name = el.data('status');
        const checked = el.hasClass('n-label');
        const statuses = this.settings.getVisibleStatuses();
        this.settings.setVisibleStatuses(Object.assign({}, statuses, {[name]: checked}));
    }
}

export default StatusToggleView;
