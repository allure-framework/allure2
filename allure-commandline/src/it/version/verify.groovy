def log = new File(basedir as File, "command.log")
assert log.text.trim() != 'null'