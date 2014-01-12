def log = new File(basedir, "build.log")
assert log.exists()
def missed = []
boolean missedStarted = false
log.eachLine {
    if (missedStarted) {
        if (it.startsWith("[ERROR] *")) {
            missed << it
        } else {
            missedStarted = false
        }
    } else if (it.endsWith("Some modules are missing:")) {
        missedStarted = true
    }
}
assert 2 == missed.size()
assert "[ERROR] * platform/features/platform-feature-2".equals(missed.get(0))
assert "[ERROR] * platform/plugins/platform-plugin-2".equals(missed.get(1))