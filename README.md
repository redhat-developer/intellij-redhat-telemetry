# Red Hat Telemetry

This library provides Telemetry APIs specifically meant to be used by IDEA plugins developped by Red Hat.
## Telemetry reporting
With your approval, extensions published by Red Hat collect anonymous 
[usage data](https://github.com/redhat-developer/intellij-redhat-telemetry/blob/master/USAGE_DATA.md) 
and send it to Red Hat servers to help improve our products and services. Read our 
[privacy statement](https://developers.redhat.com/article/tool-data-collection) to learn more about it.

The first time one of Red Hat extension engaging in telemetry collection runs, you will be asked to opt-in Red Hat's 
telemetry collection program:

![Opt-in request](images/optin-request.png)

Whether you accept or deny the request, this pop up will not show again.

You can also opt-in later, by enabling it in the preferences at Tools > Red Hat Telemetry.
![Opt-in preferences](optin-preferences.png)

This will enable all telemetry events from Red Hat plugins going forward.

## How to disable telemetry reporting?
If you want to stop sending usage data to Red Hat, you can set disable it in the preferences at Tools > Red Hat Telemetry.
This will silence all telemetry events from Red Hat plugins going forward.



