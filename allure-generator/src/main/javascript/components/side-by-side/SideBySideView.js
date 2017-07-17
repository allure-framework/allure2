import './styles.scss';
import split from 'split.js';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators';
import template from './SideBySideView.hbs';

@className('side-by-side')
@regions({
    left: '.panel-left__content',
    right: '.panel-right__content'
})
class SideBySideView extends View {
    template = template;

    onAttach() {
        split(['.panel-left', '.panel-right']);
    }

    onRender() {
        const {left, right} = this.options;
        this.showChildView('left', left);
        this.showChildView('right', right);
    }

    templateContext() {
        return {
            cls: 'side-by-side'
        };
    }

}

export default SideBySideView;