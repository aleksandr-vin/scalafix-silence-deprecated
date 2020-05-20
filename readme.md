# Scalafix rules for Scalafix Silence Deprecated

To develop rule:
```
sbt ~tests/test
# edit rules/src/main/scala/fix/ScalafixSilenceDeprecated.scala
```

## How to run

1. Remove `-deprecation` & `-Xfatal-warnings` from `scalacOptions` to allow compilation (scalafix rules are applied after compilation). 

1. Clean to run against all the sources

1. Check how `scalaVersion` changes with `scalafixEnable` -- some `cross CrossVersion.full` deps may stop resolving, then you need to update their version.
   ```
   show scalaVersion
   scalafixEnable
   show scalaVersion
   ```

1. Run rule
   ```
   scalafix dependency:ScalafixSilenceDeprecated@com.xvygyjau:scalafix-silence-deprecated:0.1.0-SNAPSHOT
   ```
