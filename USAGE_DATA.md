## Usage data being collected by Red Hat Plugins
Only anonymous data is being collected by Red Hat plugins leveraging 'Red Hat Telemetry' facilities. The IP address of telemetry requests is not even stored on Red Hat servers.

### Common data
Telemetry requests may contain:

* a random anonymous user id (UUID v4), that is stored locally on `~/.redhat/anonymousId`
* the client name (IntelliJ IDE) and its version
* the name and version of the plugin that sends the event (eg. `Tekton Pipelines by Red Hat`)
* the OS name and version
* the user locale (eg. en_US)
* the user timezone
* the country id (as determined by the current timezone)

Common events are reported:

* when the plugin is started
* when the plugin is shut down
    - duration of the session

### Other extensions
Red Hat extensions' specific telemetry collection details can be found there:

* [Tekton Pipelines by Red Hat](https://github.com/redhat-developer/intellij-tekton/blob/master/USAGE_DATA.md)