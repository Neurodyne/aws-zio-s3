// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `aws-zio-s3` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.awsS3Async,
        library.scalazZio,
        library.scalaCheck % Test,
        library.scalaTest  % Test,
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val awsS3      = "2.5.29" 
      val scalazZio  = "1.0-RC4"
      val scalaCheck = "1.14.0"
      val scalaTest  = "3.0.6"
    }
    val awsS3Async = "software.amazon.awssdk" %  "s3"         % Version.awsS3
    val scalazZio  = "org.scalaz"             %% "scalaz-zio" % Version.scalazZio
    val scalaCheck = "org.scalacheck"         %% "scalacheck" % Version.scalaCheck
    val scalaTest  = "org.scalatest"          %% "scalatest"  % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  scalafmtSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.8",
    organization := "com.github.branislavlazic",
    organizationName := "Branislav Lazic",
    startYear := Some(2019),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import",
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    Compile / compile / wartremoverWarnings ++= Warts.unsafe,
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
  )