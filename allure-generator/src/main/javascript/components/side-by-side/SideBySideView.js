import './styles.scss';
import split from 'split.js';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators';
import template from './SideBySideView.hbs';

@className('side-by-side')
@regions({
    left: '.side-by-side__left',
    right: '.side-by-side__right'
})
class SideBySideView extends View {
    template = template;

    onAttach() {
        split(['.side-by-side__left', '.side-by-side__right'], {gutterSize: 7});
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