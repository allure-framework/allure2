import TestcaseTableView from 'components/testcase-table/TestcaseTableView';
import router from 'router';
import jQuery from 'jquery';
import settings from 'util/settings';

describe('TestcaseTable', function() {
    function TableElement(el) {
        function getRowTitle(node) {
            return node.querySelector('.testcase-table__name').textContent;
        }

        this.activeRow = () => el.find('.table__row_active').toArray().map(getRowTitle)[0];
        this.rows = () => el.find('.table__row').toArray().map(getRowTitle);
        this.column = (sortField) => el.find(`.table__col[data-sort="${sortField}"]`);
    }

    beforeEach(function() {
        spyOn(router, 'toUrl');
        settings.clear();
        settings.set('visibleStatuses', {PASSED: true, FAILED: true});
        this.view = new TestcaseTableView({
            testCases: [
                {uid: 1, name: 'case 1', time: {duration: 432}, status: 'PASSED'},
                {uid: 2, name: 'case 2', time: {duration: 145}, status: 'PASSED'},
                {uid: 3, name: 'case 3', time: {duration: 370}, status: 'FAILED'}
            ],
            baseUrl: 'xUnit/56'
        }).render();
        this.el = new TableElement(this.view.$el);
    });

    afterEach(function() {
        this.view.destroy();
    });

    it('should not highlight any testcase by default', function() {
        expect(this.el.activeRow()).toBeUndefined();
    });

    it('should render test cases list', function() {
        expect(this.el.rows()).toEqual([
            'case 1',
            'case 2',
            'case 3'
        ]);
    });

    it('should filter test cases by status', function() {
        settings.set('visibleStatuses', {PASSED: true});
        expect(this.el.rows()).toEqual([
            'case 1',
            'case 2'
        ]);
    });

    it('should change test case sorting by click', function() {
        this.el.column('time.duration').click();
        expect(this.el.rows()).toEqual([
            'case 2',
            'case 3',
            'case 1'
        ]);
        this.el.column('time.duration').click();
        expect(this.el.rows()).toEqual([
            'case 1',
            'case 3',
            'case 2'
        ]);
    });

    it('should highlight test case by id', function() {
        this.view.highlightItem(2);
        expect(this.el.activeRow()).toEqual('case 2');
    });

    it('should ignore wrong key-codes', function() {
        this.view.highlightItem(2);
        this.view.$el.trigger(new jQuery.Event('keydown', {keyCode: 27 /*ESC*/}));
        expect(router.toUrl).not.toHaveBeenCalled();
    });

    it('should not navigate out of the bounds', function() {
        this.view.highlightItem(1);
        this.view.$el.trigger(new jQuery.Event('keydown', {keyCode: 38 /*UP*/}));
        expect(router.toUrl).not.toHaveBeenCalled();
    });

    it('should navigate on down key', function() {
        this.view.highlightItem(1);
        this.view.$el.trigger(new jQuery.Event('keydown', {keyCode: 40 /*DOWN*/}));
        expect(router.toUrl).toHaveBeenCalledWith('#xUnit/56/2');
    });

    it('should navigate on up key', function() {
        this.view.highlightItem(2);
        this.view.$el.trigger(new jQuery.Event('keydown', {keyCode: 38 /*UP*/}));
        expect(router.toUrl).toHaveBeenCalledWith('#xUnit/56/1');
    });
});
