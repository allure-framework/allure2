import './styles.css';
import {LayoutView} from 'backbone.marionette';
import {on, region, behavior} from '../../decorators';
import allurePlugins from '../../pluginApi';
import StepsView from '../steps/StepsView';
import template from './TestcaseView.hbs';

const SEVERITY_ICONS = {
    BLOCKER: 'fa fa-exclamation-triangle',
    CRITICAL: 'fa fa-exclamation',
    NORMAL: 'fa fa-file-o',
    MINOR: 'fa fa-arrow-down',
    TRIVIAL: 'fa fa-long-arrow-down'
};

@behavior('TooltipBehavior', {position: 'bottom'})
class TestcaseView extends LayoutView {
    template = template;

    @region('.testcase__steps')
    steps;

    initialize({state}) {
        this.state = state;
        this.plugins = [];
        this.listenTo(this.state, 'change:attachment', this.highlightSelectedAttachment, this);
    }

    onRender() {
        this.showTestcasePlugins(this.$('.testcase__content_before'), allurePlugins.testcaseBlocks.before);
        this.steps.show(new StepsView({
            baseUrl: this.options.baseUrl + '/' + this.model.id,
            model: this.model
        }));
        this.highlightSelectedAttachment();
        this.showTestcasePlugins(this.$('.testcase__content_after'), allurePlugins.testcaseBlocks.after);
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
        this.$('.testcase__trace').toggleClass('testcase__trace_visible');
    }
}

export default TestcaseView;
