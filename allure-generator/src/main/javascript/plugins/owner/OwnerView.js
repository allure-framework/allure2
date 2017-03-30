import {View} from 'backbone.marionette';
import template from './OwnerView.hbs';
import {className} from '../../decorators/index';

@className('pane__section')
class OwnerView extends View {
    template = template;

    serializeData() {
        var extra = this.model.get('extra');
        return {
            owner: extra ? extra.owner : null
        };
    }
}

export default OwnerView;