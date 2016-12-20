import './styles.css';
import {View} from 'backbone.marionette';
import {on, regions, behavior} from '../../decorators';
import pluginsRegistry from '../../util/pluginsRegistry';
import template from './TestcaseView.hbs';
import ExecutionView from '../execution/ExecutionView';

const SEVERITY_ICONS = {
    blocker: 'fa fa-exclamation-triangle',
    critical: 'fa fa-exclamation',
    normal: 'fa fa-file-o',
    minor: 'fa fa-arrow-down',
    trivial: 'fa fa-long-arrow-down'
};

@behavior('TooltipBehavior', {position: 'bottom'})
@regions({
    execution: '.testcase__execution'
})
class TestcaseView extends View {
    template = template;

    initialize({state}) {
        this.state = state;
        this.plugins = [];
        this.listenTo(this.state, 'change:attachment', this.highlightSelectedAttachment, this);
    }

    onRender() {
        this.showTestcasePlugins(this.$('.testcase__content_before'), pluginsRegistry.testcaseBlocks.before);
        this.showChildView('execution', new ExecutionView({
            baseUrl: this.options.baseUrl + '/' + this.model.id,
            model: this.model
        }));
        // this.highlightSelectedAttachment();
        this.showTestcasePlugins(this.$('.testcase__content_after'), pluginsRegistry.testcaseBlocks.after);
    }

    onDestroy() {
        this.plugins.forEach(plugin => plugin.destroy());
    }

    highlightSelectedAttachment() {
        const currentAttachment = this.state.get('attachment');
        this.$('.attachment-row').removeClass('attachment-row_selected');
        this.$(`.attachment-row[data-uid="${currentAttachment}"]`).addClass('attachment-row_selected');
    }

    showTestcasePlugins(container, plugins) {
        plugins.forEach((Plugin) => {
            const plugin = new Plugin({model: this.model});
            plugin.$el.appendTo(container);
            this.plugins.push(plugin);
            plugin.render();
        });
    }

    serializeData() {
        return Object.assign({
            severityIcon: SEVERITY_ICONS[this.model.get('severity')],
            route: {
                baseUrl: this.options.baseUrl
            }
        }, super.serializeData());
    }

    @on('dblclick .testcase__failure')
    @on('click .testcase__trace-toggle')
    onStacktraceClick() {
        this.$('.testcase__failure').toggleClass('testcase__failure_expanded');
    }

    @on('click .pane__subtitle')
    onSubtitleClick() {
        this.$('.pane__subtitle').toggleClass('line-ellipsis', false);
    }
}

export default TestcaseView;
