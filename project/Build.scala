import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-aws"
    val appVersion      = "0.1"

    val appDependencies = Seq(
      "com.amazonaws" % "aws-java-sdk" % "1.3.9"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
         
    )

}