// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `aws-zio-s3` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      commonDeps,
      zioDeps,
      awsDeps
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

val zioVersion = "1.0.0-RC17"
val awsVersion = "2.10.42"

lazy val awsDeps = libraryDependencies ++= Seq("software.amazon.awssdk" % "s3" % awsVersion)

lazy val zioDeps = libraryDependencies ++= Seq(
  "dev.zio" %% "zio"          % zioVersion,
  "dev.zio" %% "zio-test"     % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
)

lazy val commonDeps = libraryDependencies ++= Seq()

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
    scalafmtSettings ++
    sonatypeSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    organization := "com.github.branislavlazic",
    organizationName := "Branislav Lazic",
    startYear := Some(2019),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/BranislavLazic/aws-zio-s3")),
    scalacOptions --= Seq(
      // "-Xfatal-warnings",
      "-Ywarn-value-discard"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val sonatypeSettings =
  Seq(
    version := "0.1.0",
    sonatypeProfileName := "com.github.branislavlazic",
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/BranislavLazic/aws-zio-s3"),
        "scm:git@github.com:BranislavLazic/aws-zio-s3.git"
      )
    ),
    developers := List(
      Developer(
        id = "BranislavLazic",
        name = "Branislav Lazic",
        email = "brano2411@hotmail.com",
        url = url("http://github.com/BranislavLazic")
      )
    ),
    description := "ZIO wrapper for AWS S3 SDK async client.",
    pomIncludeRepository := { _ =>
      false
    },
    publishTo := sonatypePublishToBundle.value,
    publishMavenStyle := true
  )

// Aliases
addCommandAlias("rel", "reload")
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
