# Export events log from Sentry

Simple Java command line program to export events from sentry and store on file system. 

## To execute program using java

- Download [executable/sentryexport-1.0.jar](executable/sentryexport-1.0.jar)
- Execute

    java -jar sentryexport-1.0.jar -f=data.json -h=sentry.aashish.io -p=sentry/ioaashishios -d=2019-08-10 -a=<AUTH_TOKEN>

## To execute on linux 

- Download [executable/sentryexport-1.0](executable/sentryexport-1.0)
- Execute 

    ./sentryexport-1.0 -f=data.json -h=sentry.aashish.io -p=sentry/ioaashishios -d=2019-08-10 -a=<AUTH_TOKEN>

## To execute on Windows
- Download [executable/sentryexport-1.0.exe](executable/sentryexport-1.0.exe)
- Execute 

    sentryexport-1.0 -f=data.json -h=sentry.aashish.io -p=sentry/ioaashishios -d=2019-08-10 -a=<AUTH_TOKEN>

## All options
    java -jar executable/sentryexport-1.0.jar help
    Missing required options [-p=<sentryProject>, -h=<sentryHost>, -d=<tillDate>, -a=<authToken>]
    Usage: sentryexport -a=<authToken> -d=<tillDate> [-f=<fileName>]
                    -h=<sentryHost> -p=<sentryProject> [-t=<tillTime>]
    -a=<authToken>        Auth Token of Sentry
    -d=<tillDate>         Date (YYYY-MM-DD) till when data needs to be exported
    -f=<fileName>         Json file in which data to be exported, e.g. data.json
    -h=<sentryHost>       Sentry Host
    -p=<sentryProject>    Sentry Project
    -t=<tillTime>         Time (HH:MI:SS) till when data needs to be exported, by
                          default 00:00:00
