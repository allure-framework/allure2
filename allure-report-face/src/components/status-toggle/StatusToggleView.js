import './styles.css';
import {on, className} from '../../decorators';
import settings from '../../util/settings';
import template from './StatusToggleView.hbs';
import PopoverView from '../popover/PopoverView';
import {states} from '../../util/statuses';

@className('status-toggle popover')
class StatusToggleView extends PopoverView {
    template = template;

    initialize() {
        super.initialize({position: 'bottom-left', offset: -1});
    }
    
    setContent() {
        this.$el.html(template(this.serializeData()));
    }

    show(anchor) {
        super.show(null, anchor);
        this.delegateEvents();
    }

    serializeData() {
        const statuses = settings.get('visibleStatuses');
        return {
            statuses: states.map(status => ({
                status,
                active: !!statuses[status]
            }))
        };
    }

    @on('click .status-toggle__item')
    onCheckChange(e) {
        const el = this.$(e.currentTarget);
        el.toggleClass('status-toggle__item_active');
        const name = el.data('status');
        const checked = el.hasClass('status-toggle__item_active');
        const statuses = settings.get('visibleStatuses');
        settings.save('visibleStatuses', Object.assign({}, statuses, {[name]: checked}));
    }
}

export default StatusToggleView;
