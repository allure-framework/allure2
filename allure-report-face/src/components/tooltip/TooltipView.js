import './styles.css';
import {View} from 'backbone';
import {className} from '../../decorators';
import bem from 'b_';
import $ from 'jquery';
import {defaults} from 'underscore';

export const POSITION = {
    'center': function({top, left, height, width}, {offset}, tipSize) {
        return {
            top: top + height / 2,
            left: left + width / 2 - tipSize.width / 2
        };
    },
    'right': function({top, left, height, width}, {offset}, tipSize) {
        return {
            top: top + height / 2 - tipSize.height / 2,
            left: left + width + offset
        };
    },
    'bottom': function({top, left, height, width}, {offset}, tipSize) {
        return {
            top: top + height + offset,
            left: left + width / 2 - tipSize.width / 2
        };
    }
};

@className('tooltip')
class TooltipView extends View {
    static container = $(document.body);

    initialize(options) {
        this.options = options;
        defaults(this.options, {offset: 10});
    }

    render() {
        this.constructor.container.append(this.$el);
    }

    isVisible() {
        return this.$el.is(':visible');
    }

    setContent(text) {
        this.$el.html(text);
    }

    show(text, anchor) {
        const {position} = this.options;
        this.setContent(text);
        this.$el.addClass(bem(this.className, {position}));
        this.render();
        this.$el.css(POSITION[position](
            anchor[0].getBoundingClientRect(),
            {offset: this.options.offset},
            this.$el[0].getBoundingClientRect()
        ));
    }

    hide() {
        this.$el.remove();
    }
}

export default TooltipView;
