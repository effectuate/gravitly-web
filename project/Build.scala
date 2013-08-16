import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "gravitly-web"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "jp.t2v" %% "play2.auth"      % "0.10.1",
    "jp.t2v" %% "play2.auth.test" % "0.10.1" % "test"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
