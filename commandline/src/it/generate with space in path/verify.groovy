def report = new File(basedir as File, 'report')
assert report.exists()
assert report.isDirectory()

['index.html', 'app.js', 'favicon.ico', 'styles.css'].each {
    def file = new File(report, it)
    assert file.exists()
    assert file.isFile()
}

def data = new File(report, 'data')
assert data.exists()
assert data.isDirectory()

['xunit.json', 'behaviors.json', 'packages.json', 'widgets.json', 'graph.json', 'defects.json'].each {
    def file = new File(data, it)
    assert file.exists()
    assert file.isFile()
}

def testCases = new File(data, 'test-cases')
assert testCases.exists()
assert testCases.isDirectory()

def plugins = new File(report, 'plugins')
assert plugins.exists()
assert plugins.isDirectory()

['xunit-plugin', 'behaviors-plugin', 'packages-plugin'].each {
    def plugin = new File(plugins, it)
    assert plugin.exists()
    assert plugin.isDirectory()

    def index = new File(plugin, 'index.js')
    assert index.exists()
    assert index.isFile()
}

return