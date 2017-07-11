(function () {
    var settings = allure.getPluginSettings('screen-diff', {diffType: 'diff'});

    function renderImage(src) {
        return '<div class="screen-diff__container">' +
            '<img class="screen-diff__image" src="data/attachments/' + src + '">' +
            '</div>';
    }

    function renderDiffContent(type, data) {
        function findImage(name) {
            if (data.testStage && data.testStage.attachments) {
                return data.testStage.attachments.filter(function (attachment) {
                    return attachment.name === name;
                })[0];
            }
            return null;
        }

        var diffImage = findImage('diff');
        var actualImage = findImage('actual');
        var expectedImage = findImage('expected');

        if (!diffImage && !actualImage && !expectedImage) {
            return '<span>Diff, actual and expected image have not been provided.</span>';
        }

        if (type === 'diff') {
            if (!diffImage) {
                return renderImage(actualImage.source);
            }
            return renderImage(diffImage.source);
        }
        if (type === 'overlay') {
            return '<div class="screen-diff__overlay screen-diff__container">' +
                '<img class="screen-diff__image" src="data/attachments/' + expectedImage.source + '">' +
                '<div class="screen-diff__image-over">' +
                '<img class="screen-diff__image" src="data/attachments/' + actualImage.source + '">' +
                '</div>' +
                '</div>';
        }
    }

    var ScreenDiffView = Backbone.Marionette.View.extend({
        className: 'pane__section',
        events: {
            'click [name="screen-diff-type"]': 'onDiffTypeChange',
            'mousemove .screen-diff__overlay': 'onOverlayMove'
        },
        templateContext: function () {
            return {
                diffType: settings.get('diffType')
            }
        },
        template: function (data) {
            var testType = data.labels.filter(function (label) {
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
            var overImage = this.$(event.target);
            overImage.width(overImage.width());
        },
        onRender: function () {
            const diffType = settings.get('diffType');
            this.$('[name="screen-diff-type"][value="' + diffType + '"]').prop('checked', true);
            if (diffType === 'overlay') {
                this.$('.screen-diff__image-over img').on('load', this.adjustImageSize.bind(this));
            }
        },
        onOverlayMove: function (event) {
            var pageX = event.pageX;
            var containerScroll = this.$('.screen-diff__container').scrollLeft();
            var elementX = event.currentTarget.getBoundingClientRect().left;
            var delta = pageX - elementX + containerScroll;
            this.$('.screen-diff__image-over').width(delta);
        },
        onDiffTypeChange: function (event) {
            settings.save('diffType', event.target.value);
            this.render();
        }
    });
    allure.api.addTestResultBlock(ScreenDiffView, {position: 'before'});
})();
