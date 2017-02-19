import './styles.css';
import {View} from 'backbone.marionette';
import {on, regions, behavior} from '../../decorators';
import pluginsRegistry from '../../util/pluginsRegistry';
import template from './TestcaseView.hbs';
import ExecutionView from '../execution/ExecutionView';
import clipboard from 'clipboard-js';

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
    }

    onRender() {
        this.showTestcasePlugins(this.$('.testcase__content_before'), pluginsRegistry.testcaseBlocks.before);
        this.showChildView('execution', new ExecutionView({
            baseUrl: '#testcase/' + this.model.id,
            state: this.state,
            model: this.model
        }));
        this.showTestcasePlugins(this.$('.testcase__content_after'), pluginsRegistry.testcaseBlocks.after);
    }

    onDestroy() {
        this.plugins.forEach(plugin => plugin.destroy());
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
            severityIcon: SEVERITY_ICONS[this.model.get('severity')]
        }, super.serializeData());
    }

    @on('dblclick .testcase__failure')
    @on('click .testcase__trace-toggle')
    onStacktraceClick() {
        this.$('.testcase__failure').toggleClass('testcase__failure_expanded');
    }

    @on('click .fullname__body')
    onFillNameBopyClick() {
        this.$('.pane__subtitle').toggleClass('line-ellipsis', false);
    }

    @on('click .fullname__copy')
    onFullNameCopyClick() {
        clipboard.copy(this.model.get('fullName'));
    }
}

export default TestcaseView;
