# atmosphere-issue-1988

This is a minimalistic example to demonstrate [Atmosphere Issue 1988](https://github.com/Atmosphere/atmosphere/issues/1988)

### Getting Started
This is a simple Dropwizard service that will publish at localhost:8080.
To start server run `./gradlew run` from main project directory (gradlew.bat for Windows).
Once started open http://localhost:8080 for simple client, make sure JavaScript debug console is up.

To reproduce issue click Connect, Ping and Disconnect for `/ws/pass` and then same for `/ws/fail` while observing log output.
If you do this cycle again more errors get logged.

### Key files
- ExampleAtmosphereHandler - actual handler with 2 paths /ws/pass using string ID and /ws/fail with UUID
- config.yml - dropwizard service config
- index.html - includes atmosphere.js and shows the buttons (served from assets)
- build.gradle - build file, has 2 versions of atmosphere as dependency, just move comments around
