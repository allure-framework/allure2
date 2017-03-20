import './styles.css';
import $ from 'jquery';
import {on, className} from '../../decorators';
import settings from '../../util/settings';
import template from './StatusToggleView.hbs';
import PopoverView from '../popover/PopoverView';
import {values} from '../../util/statuses';

@className('status-toggle popover')
class StatusToggleView extends PopoverView {
    template = template;

    initialize({statusesKey}) {
        this.statusesKey = statusesKey;
        super.initialize({position: 'bottom-left', offset: -1});
        this.onDocumentClick = this.onDocumentClick.bind(this);
    }

    onDocumentClick(e) {
        if(!this.$(e.target).length) {
            this.hide();
        }
    }
    
    setContent() {
        this.$el.html(template(this.serializeData()));
    }

    show(anchor) {
        super.show(null, anchor);
        this.delegateEvents();
        setTimeout(() => {
            $(document).on('click', this.onDocumentClick);
        });
    }

    hide() {
        $(document).off('click', this.onDocumentClick);
        super.hide();
    }

    serializeData() {
        const statuses = settings.getVisibleStatuses(this.statusesKey);
        return {
            statuses: values.map(status => ({
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
        const statuses = settings.getVisibleStatuses(this.statusesKey);
        settings.save(this.statusesKey, Object.assign({}, statuses, {[name]: checked}));
    }
}

export default StatusToggleView;
