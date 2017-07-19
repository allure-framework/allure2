import './styles.scss';
import $ from 'jquery';
import {View} from 'backbone.marionette';
import template from './ModalView.hbs';
import {className, regions, on} from '../../decorators/index';


@className('modal')
@regions({
    content: '.modal__content'
})
class ModalView extends View {
    template = template;
    static container = $(document.body);

    onRender() {
        this.constructor.container.append(this.$el);
        this.showChildView('content', this.options.childView);
    }

    @on('click .modal__close')
    onClose() {
        this.destroy();
    }

    serializeData() {
        return {
            cls: this.className,
            title: this.options.title,
            backUrl: this.options.backUrl
        };
    }
}

export default ModalView;