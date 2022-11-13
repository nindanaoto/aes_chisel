
scalaVersion := "2.13.8"

addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % "3.5.3" cross CrossVersion.full)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
    "edu.berkeley.cs" %% "chisel3" % "3.5.3",
    "edu.berkeley.cs" %% "chiseltest" % "0.5.4"
)

// POM settings for Sonatype
// Sonatype Username: smosanu (same as github username)
// Full name: Sergiu Mosanu
organization := "com.github.hplp"
organizationName := "hplp"
homepage := Some(url("http://hplp.ece.virginia.edu/home"))
scmInfo := Some(ScmInfo(url("https://github.com/hplp/aes_chisel"), "git@github.com:hplp/aes_chisel.git"))
developers := List(Developer("smosanu", "Sergiu Mosanu", "sm7ed@virginia.edu", url("https://github.com/smosanu")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

publishMavenStyle := true
publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)