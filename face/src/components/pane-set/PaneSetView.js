import './styles.css';
import {LayoutView} from 'backbone.marionette';
import $ from 'jquery';
import {className} from '../../decorators';

const paneTpl = `<div class="pane"></div>`;

@className('pane-set')
class PaneSetView extends LayoutView {
    panes = {};
    expanded = false;

    template() {
        return '';
    }

    updatePane(name, changed, factory) {
        if(changed.hasOwnProperty(name)) {
            if(changed[name]) {
                this.addPane(name, factory());
            } else {
                this.removePane(name);
            }
        }
    }

    addPane(name, view) {
        if(!this.getRegion(name)) {
            const pane = $(paneTpl);
            this.fadeInPane(pane);
            this.panes[name] = pane;
            this.addRegion(name, {el: pane});
        }
        this.getRegion(name).show(view);
    }

    removePane(name) {
        if(this.getRegion(name)) {
            this.fadeOutPane(this.panes[name], () => this.removeRegion(name));
            delete this.panes[name];
        }
    }

    updatePanesPositions() {
        const paneNames = Object.keys(this.panes);
        const last = paneNames.length - 1;
        paneNames.forEach((paneName, index) => {
            const pane = this.panes[paneName];
            var width;
            var left;
            pane.toggleClass('pane_overlaid', this.expanded ? index < last : index < last - 1);
            if(index === last) {
                const expanded = index === 0 || this.expanded;
                width = expanded ? 100 - 5 * index : 50;
                left = 100 - width;
                pane.toggleClass('pane_expanded', expanded);
            } else {
                const leftOffset = 5 * index;
                left = leftOffset;
                width = 50 - leftOffset;
                pane.removeClass('pane_expanded');
            }
            pane.css({left: left + '%', width: width + '%'});
        });
    }

    fadeInPane(pane) {
        pane.addClass('pane_enter');
        this.$el.append(pane);
        window.requestAnimationFrame(() => {
            pane.removeClass('pane_enter');
        });
    }

    fadeOutPane(pane, callback) {
        pane.addClass('pane_leave');
        pane.one('transitionend', () => {
            pane.remove();
            callback();
        });
    }
}

export default PaneSetView;
