import createSettingsModel from '../data/settings/createSettingsModel';

const GlobalSettingsModel = createSettingsModel(null, {
  language: 'en',
  testResultSorting: {
    field: 'index',
    order: 'asc'
  },
  sidebarCollapsed: false,
  visibleStatuses: {
    failed: true,
    broken: true,
    skipped: true,
    unknown: true,
    passed: true
  },
  showGroupInfo: false
});
const settings = new GlobalSettingsModel();
settings.fetch();

export default settings;
