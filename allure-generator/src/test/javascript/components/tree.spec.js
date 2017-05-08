import {Model} from 'backbone';
import TreeView from 'components/tree/TreeView';
import TreeCollection from 'data/tree/TreeCollection';
import settings from 'util/settings';
import {values} from 'util/statuses';


describe('Tree', function () {
    const tabName = 'Tab Name';
    const sorterSettingsKey = tabName + '.treeSorting';
    const filterSettingsKey = tabName + '.visibleStatuses';
    const infoSettingsKey = 'showGroupInfo';

    function fakeUid() {
        return Math.random().toString(36).substring(2);
    }

    function rootNode({children=[]} = {}) {
        return {
            statistic: children.reduce((acc, curr) => {
                values.forEach(status => {
                    acc[status] = (acc[status] || 0) +
                        (curr.statistic ? curr.statistic[status] : (curr.status === status ? 1 : 0));
                });
                return acc;
            }, {}),
            time: {
                minDuration: 0,
                maxDuration: 0,
                sumDuration: 0,
                duration: children.reduce((acc, curr) => { return acc + curr.time.duration; }, 0)
            },
            children: children
        };
    }

    function groupNode({name='', children=[], uid=fakeUid()} = {}) {
        return Object.assign(
            rootNode({children: children}),
            {
                type: 'TestGroupNode',
                uid: uid,
                name: name
            }
        );
    }

    function caseNode({name='TestCaseNode', status='passed', uid=fakeUid(), duration=1} = {}) {
        return {
            type : 'TestCaseNode',
            name: name,
            uid: uid,
            status: status,
            time: {
                duration: duration
            }
        };
    }

    function PageObject(el) {
        this.nodes = () => el.find('.node');
        this.node = (i) => this.nodes().eq(i);
    }

    function sortTree({sorter=0, ascending=true} = {}){
        settings.save(sorterSettingsKey, {
            sorter: sorter,
            ascending: ascending
        });
    }

    function filterTree({failed=true, broken=true, passed=true, skipped=true, unknown=true}) {
        settings.save(filterSettingsKey, {
            failed: failed,
            broken: broken,
            passed: passed,
            skipped: skipped,
            unknown: unknown
        });
    }

    function renderView(data) {
        const items = new TreeCollection([], {});
        items.set(data, {parse: true});

        const view = new TreeView({
            collection: items,
            state: new Model(),
            treeState: new Model(),
            tabName: tabName,
            baseUrl: 'XUnit',
        });
        view.render();
        view.onRender();

        return new PageObject(view.$el);
    }

    const data = rootNode({
        children: [
            groupNode({
                name: 'A group node',
                children: [
                    caseNode({name: 'First node', status: 'passed', duration: 3}),
                    caseNode({name: 'Second node', status: 'failed', duration: 1}),
                    caseNode({name: 'Third node', status: 'skipped', duration: 2})
                ]
            }),
            groupNode({
                name: 'B group node',
                children: [
                    caseNode({name: 'Node in B group', status: 'unknown', duration: 1})
                ]
            }),
            caseNode({name: 'Other node', status: 'unknown', duration: 5}),
        ]
    });

    describe('empty data', function () {
        beforeEach(function () {
            this.el = renderView({});
        });

        it('should render correctly', function () {
            expect(this.el.nodes().length).toBe(0);
            sortTree({ascending: false});
            filterTree({failed: true, broken: false, passed: false, skipped: false, unknown: false});
            settings.save(infoSettingsKey, true);
            expect(this.el.nodes().length).toBe(0);
        });
    });

    describe('sorting', function () {

        beforeEach(function () {
            settings.unset(sorterSettingsKey);
            settings.unset(filterSettingsKey);
            settings.unset(infoSettingsKey);
            this.el = renderView(data);
        });

        it('should render all nodes', function () {
            expect(this.el.nodes().length).toBe(7);
        });

        it('should be sorted by name by default', function () {
            expect(this.el.node(0).text()).toMatch(/A group node/);
            expect(this.el.node(1).text()).toMatch(/First node/);
            expect(this.el.node(3).text()).toMatch(/Third node/);
        });

        it('should be able to sort by name', function () {
            sortTree({ascending: false});
            expect(this.el.node(0).text()).toMatch(/B group node/);
            expect(this.el.node(2).text()).toMatch(/A group node/);
            expect(this.el.node(3).text()).toMatch(/Third node/);

            sortTree({ascending: true});
            expect(this.el.node(0).text()).toMatch(/A group node/);
            expect(this.el.node(1).text()).toMatch(/First node/);
        });

        it('should be able to sort by duration', function () {
            sortTree({sorter: 1, ascending: false});
            expect(this.el.node(0).text()).toMatch(/A group node/);
            expect(this.el.node(1).text()).toMatch(/First node/);

            sortTree({sorter: 1, ascending: true});
            expect(this.el.node(0).text()).toMatch(/B group node/);
        });

        it('should be able to sort by status', function () {
            sortTree({sorter: 2, ascending: false});
            expect(this.el.node(0).text()).toMatch(/A group node/);
            expect(this.el.node(1).text()).toMatch(/Second node/);

            sortTree({sorter: 2, ascending: true});
            expect(this.el.node(0).text()).toMatch(/A group node/);
            expect(this.el.node(1).text()).toMatch(/Third node/);
        });
    });

    describe('filtering', function () {

        beforeEach(function () {
            settings.unset(sorterSettingsKey);
            settings.unset(filterSettingsKey);
            settings.unset(infoSettingsKey);
            this.el = renderView(data);
        });

        it('should hiding nodes', function () {
            filterTree({failed: false, broken: false, passed: false, skipped: false, unknown: false});
            expect(this.el.nodes().length).toBe(0);

            filterTree({failed: false, broken: false, passed: false, skipped: false, unknown: true});
            expect(this.el.nodes().length).toBe(3);
            expect(this.el.node(0).text()).toMatch(/B group node/);
            expect(this.el.node(2).text()).toMatch(/Other node/);

            filterTree({failed: true, broken: false, passed: false, skipped: false, unknown: false});
            expect(this.el.nodes().length).toBe(2);
            expect(this.el.node(0).text()).toMatch(/A group node/);
            expect(this.el.node(1).text()).toMatch(/Second node/);
        });

    });

    describe('groupInfo', function () {

        beforeEach(function () {
            settings.unset(sorterSettingsKey);
            settings.unset(filterSettingsKey);
            this.el = renderView(data);
        });

        it('should hiding nodes', function () {
            settings.save(infoSettingsKey, true);
            expect(this.el.nodes().length).toBe(9);

        });
    });
});