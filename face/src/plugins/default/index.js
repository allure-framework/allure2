import OverviewLayout from '../../layouts/overview/OverivewLayout';

allure.api.addTab('', {
    title: 'Overview', icon: 'fa fa-home',
    route: '',
    onEnter: () => new OverviewLayout()
});
