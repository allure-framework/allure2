import './styles.css';
import {View} from 'backbone.marionette';
import template from './RetryView.hbs';
import {on} from '../../decorators';
import router from '../../router';
import $ from 'jquery';

class RetryView extends View {
    template = template;

    serializeData() {
        const extra = this.model.get('extra');
        const retries = extra ? extra.retries : null;
        return {
            retries: retries
        };
    }

    @on('click .retry-row')
    onRetryClick(e) {
        const uid = $(e.currentTarget).data('uid');
        router.toUrl('#testresult/' + uid);
    }

}

export default RetryView;