def log = new File(basedir, "build.log")
assert log.exists()
boolean wrongPatternFound = false
log.eachLine {
    if (it.contains("Includes/excludes pattern must not start with")) {
        wrongPatternFound = true
    }
}
assert wrongPatternFound