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

    show() {
        this.constructor.container.append(this.$el);
        this.showChildView('content', this.options.childView);
        $('#content').toggleClass('blur');
    }

    @on('click .modal__close')
    onClose() {
        $('#content').toggleClass('blur');
        this.destroy();
    }

    serializeData() {
        return {
            cls: this.className,
            title: this.options.title,
        };
    }
}

export default ModalView;