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
assert 1 == missed.size()
assert "[ERROR] * missed".equals(missed.get(0))