(function () {
    function renderImage(src) {
        return '<img class="screen-diff__image" src="data/attachments/' + src + '">';
    }

    function renderDiffContent(type, data) {
        function findImage(name) {
            return data.testStage.attachments.filter(function(attachment) {
                return attachment.name === name;
            })[0];
        }
        let diffImage = findImage('diff');
        const actualImage = findImage('actual');
        const expectedImage = findImage('expected');
        if(type === 'diff') {
            if(!diffImage) {
                return renderImage(actualImage.source);
            }
            return renderImage(diffImage.source);
        }
        if(type === 'overlay') {
            return '<div class="screen-diff__overlay">' +
                '<img class="screen-diff__image" src="data/attachments/' + expectedImage.source + '">' +
                '<div class="screen-diff__image-over">' +
                '<img class="screen-diff__image" src="data/attachments/' + actualImage.source + '">' +
                '</div>' +
                '</div>';
        }
    }

    const ScreenDiffView = Backbone.Marionette.View.extend({
        className: 'pane__section',
        events: {
            'click [name="screen-diff-type"]': 'onDiffTypeChange',
            'mousemove .screen-diff__overlay': 'onOverlayMove'
        },
        initialize: function () {
            this.diffType = 'diff';
        },
        templateContext: function () {
            return {
                diffType: this.diffType
            }
        },
        template: function (data) {
            let testType = data.labels.filter(function (label) {
                return label.name === 'testType'
            })[0];

            if (!testType || testType.value !== 'screenshotDiff') {
                return '';
            }

            return '<h3 class="pane__section-title">Screen Diff</h3>' +
                '<div class="screen-diff__content">' +
                '<div class="screen-diff__switchers">' +
                '<label><input type="radio" name="screen-diff-type" value="diff"> Show diff</label>' +
                '<label><input type="radio" name="screen-diff-type" value="overlay"> Show overlay</label>' +
                '</div>' +
                renderDiffContent(data.diffType, data) +
                '</div>';
        },
        adjustImageSize: function (event) {
            const overImage = this.$(event.target);
            overImage.width(overImage.width());
        },
        onRender: function () {
            this.$('[name="screen-diff-type"][value="' + this.diffType + '"]').prop('checked', true);
            if (this.diffType === 'overlay') {
                this.$('.screen-diff__image-over img').on('load', this.adjustImageSize.bind(this));
            }
        },
        onOverlayMove: function (event) {
            const pageX = event.pageX;
            const elementX = event.currentTarget.getBoundingClientRect().left;
            const delta = pageX - elementX;
            this.$('.screen-diff__image-over').width(delta);
        },
        onDiffTypeChange: function (event) {
            this.diffType = event.target.value;
            this.render();
        }
    });
    allure.api.addTestcaseBlock(ScreenDiffView, {position: 'before'});
})();