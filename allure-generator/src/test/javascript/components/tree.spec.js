import {Model} from 'backbone';
import TreeView from 'components/tree/TreeView';
import TreeCollection from 'data/tree/TreeCollection';
import settings from 'util/settings';


describe('Tree', function () {
    const tabName = 'Tab Name';
    const sorterSettingsKey = tabName + '.treeSorting';
    const filterSettingsKey = tabName + '.visibleStatuses';
    const infoSettingsKey = 'showGroupInfo';
    let view;
    let page;

    function fakeUid() {
        return Math.random().toString(36).substring(2);
    }

    function groupNode({name = 'group', children = [], uid = fakeUid()} = {}) {
        return {
            uid: uid,
            name: name,
            time: {
                duration: children.reduce((acc, curr) => { return acc + curr.time.duration; }, 0)
            },
            children: children
        };
    }

    function caseNode({name = 'node', status = 'passed', uid = fakeUid(), duration = 1} = {}) {
        return {
            name: name,
            uid: uid,
            status: status,
            time: {
                duration: duration
            }
        };
    }

    function PageObject(el) {
        this.nodes = () => el.find('.node__name');
        this.node = (i) => this.nodes().eq(i);
        this.infos = () => el.find('.node__info');
    }

    function sortTree({sorter = 'sorter.name', ascending = true}) {
        settings.save(sorterSettingsKey, {sorter, ascending});
    }

    function filterTree({failed = true, broken = true, passed = true, skipped = true, unknown = true}) {
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

        view = new TreeView({
            collection: items,
            state: new Model(),
            treeState: new Model(),
            tabName: tabName,
            baseUrl: 'XUnit',
        }).render();
        view.onRender();
        const page = new PageObject(view.$el);

        return {view, page};
    }

    const data = groupNode({
        children: [
            groupNode({
                name: 'A group node',
                children: [
                    caseNode({name: 'First node', status: 'passed', duration: 4}),
                    caseNode({name: 'Second node', status: 'failed', duration: 2}),
                    caseNode({name: 'Third node', status: 'skipped', duration: 3})
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

    beforeEach(() => {
        settings.unset(sorterSettingsKey);
        settings.unset(filterSettingsKey);
        settings.unset(infoSettingsKey);
        ({view, page} = renderView(data));
    });

    afterEach(() => {
        view.destroy();
    });

    describe('empty data', () => {

        it('should render correctly', () => {
            const {view, page} = renderView({children: []});
            expect(page.nodes().length).toBe(0);
            sortTree({ascending: false});
            filterTree({failed: true, broken: false, passed: false, skipped: false, unknown: false});
            settings.save(infoSettingsKey, true);
            expect(page.nodes().length).toBe(0);
            view.destroy();
        });
    });

    describe('sorting', () => {

        it('should render all nodes', () => {
            expect(page.nodes().length).toBe(7);
        });

        it('should be sorted by name by default', () => {
            expect(page.node(0).text()).toMatch(/A group node/);
            expect(page.node(1).text()).toMatch(/First node/);
            expect(page.node(3).text()).toMatch(/Third node/);
        });

        it('should be able to sort by name', () => {
            sortTree({ascending: false});
            expect(page.node(0).text()).toMatch(/B group node/);
            expect(page.node(2).text()).toMatch(/A group node/);
            expect(page.node(3).text()).toMatch(/Third node/);

            sortTree({ascending: true});
            expect(page.node(0).text()).toMatch(/A group node/);
            expect(page.node(1).text()).toMatch(/First node/);
        });

        it('should be able to sort by duration', () => {
            sortTree({sorter: 'sorter.duration', ascending: false});
            expect(page.node(0).text()).toMatch(/A group node/);
            expect(page.node(1).text()).toMatch(/First node/);

            sortTree({sorter: 'sorter.duration', ascending: true});
            expect(page.node(0).text()).toMatch(/B group node/);
        });

        it('should be able to sort by status', () => {
            sortTree({sorter: 'sorter.status', ascending: false});
            expect(page.node(0).text()).toMatch(/A group node/);
            expect(page.node(1).text()).toMatch(/Second node/);

            sortTree({sorter: 'sorter.status', ascending: true});
            expect(page.node(0).text()).toMatch(/B group node/);
            expect(page.node(1).text()).toMatch(/Node in B group/);
        });
    });

    describe('filtering', () => {

        it('should hiding nodes', () => {
            filterTree({failed: false, broken: false, passed: false, skipped: false, unknown: false});
            expect(page.nodes().length).toBe(0);

            filterTree({failed: false, broken: false, passed: false, skipped: false, unknown: true});
            expect(page.nodes().length).toBe(3);
            expect(page.node(0).text()).toMatch(/B group node/);
            expect(page.node(2).text()).toMatch(/Other node/);

            filterTree({failed: true, broken: false, passed: false, skipped: false, unknown: false});
            expect(page.nodes().length).toBe(2);
            expect(page.node(0).text()).toMatch(/A group node/);
            expect(page.node(1).text()).toMatch(/Second node/);
        });

    });

    describe('groupInfo', () => {

        it('should showing and hiding the group node info', () => {
            settings.save(infoSettingsKey, true);
            expect(page.infos().length).toBe(2);
            settings.save(infoSettingsKey, false);
            expect(page.infos().length).toBe(0);
        });
    });
});
