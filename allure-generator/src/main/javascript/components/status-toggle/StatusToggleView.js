import './styles.scss';
import {on, className} from '../../decorators';
import settings from '../../util/settings';
import template from './StatusToggleView.hbs';
import {values} from '../../util/statuses';
import {View} from 'backbone.marionette';
import translate from '../../helpers/t';


@className('status-toggle')
class StatusToggleView extends View {
    template = template;

    initialize({statusesKey, statistic}) {
        this.statusesKey = statusesKey;
        this.statistic = statistic;
        this.listenTo(settings, 'change:' + this.statusesKey, this.render);
    }

    serializeData() {
        const statuses = settings.getVisibleStatuses(this.statusesKey);
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
        const statuses = settings.getVisibleStatuses(this.statusesKey);
        settings.save(this.statusesKey, Object.assign({}, statuses, {[name]: checked}));
    }
}

export default StatusToggleView;
