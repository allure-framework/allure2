import {Behavior} from 'backbone.marionette';
import {on} from '../decorators';
import copy from '../utils/clipboard';
import TooltipView from '../components/tooltip/TooltipView';
import translate from '../helpers/t';

export default class ClipboardBehavior extends Behavior {

    initialize() {
        this.tooltip = new TooltipView({
            position: 'left'
        });
    }

    @on('mouseenter [data-copy]')
    onTipHover(e) {
        const el = this.$(e.currentTarget);
        this.tooltip.show(translate('controls.clipboard'), el);
    }

    @on('mouseleave [data-copy]')
    onTipLeave() {
        this.tooltip.hide();
    }

    @on('click [data-copy]')
    onCopyableClick(e) {
        const el = this.$(e.currentTarget);
        if (copy(el.data('copy'))) {
            this.tooltip.show(translate('controls.clipboardSuccess'), el);
        } else {
            this.tooltip.show(translate('controls.clipboardError'), el);
        }
    }
}
