package io.github.janhicken.surefire.scalatest

import org.apache.maven.surefire.booter.ProviderParameterNames._
import org.apache.maven.surefire.providerapi.{ProviderParameters, SurefireProvider}
import org.apache.maven.surefire.suite.RunResult
import org.scalatest.tools.{Runner, SurefireReporter}
import org.scalatest.{DoNotDiscover, Suite, WrapWith}

import java.lang.reflect.Modifier
import scala.jdk.CollectionConverters._
import scala.util.Try

class ScalaTestProvider(parameters: ProviderParameters) extends SurefireProvider {

  import ScalaTestProvider._

  private var thread: Thread = _

  override def getSuites: java.lang.Iterable[Class[_]] = parameters.getScanResult.applyFilter(
    testClass => isDiscoverableSuite(testClass) && (isAccessibleSuite(testClass) || isRunnable(testClass)),
    parameters.getTestClassLoader
  )

  override def invoke(forkTestSet: AnyRef): RunResult = {
    val argsBuilder = Array.newBuilder[String]

    Option(forkTestSet)
      .collect { case testClass: Class[_] => Iterable(testClass) }
      .getOrElse(getSuites.asScala)
      .foreach(testClass => argsBuilder ++= Seq("-s", testClass.getName))

    val properties = parameters.getProviderProperties.asScala
    (properties.get(PARALLEL_PROP), properties.get(THREADCOUNT_PROP)) match {
      case (Some("suites"), Some("0"))      => argsBuilder += "-P"
      case (Some("suites"), Some(nThreads)) => argsBuilder += s"-P$nThreads"
      case _                                =>
    }

    // Sadly we cannot pass an instance of SurefireReporter to the runner, so we must configure statically
    SurefireReporter.consoleStream = parameters.getConsoleLogger
    SurefireReporter.runListener = parameters.getReporterFactory.createReporter()
    argsBuilder += "-C"
    argsBuilder += classOf[SurefireReporter].getName

    val args = argsBuilder.result()
    thread = new Thread(() => Runner.run(args))
    thread.setContextClassLoader(parameters.getTestClassLoader)
    thread.start()
    thread.join()

    parameters.getReporterFactory.close()
  }

  override def cancel(): Unit = thread.interrupt()
}

object ScalaTestProvider {
  def isRunnable(testClass: Class[_]): Boolean = Option(testClass.getAnnotation(classOf[WrapWith]))
    .exists(
      _.value.getDeclaredConstructors
        .exists(_.getParameterTypes.headOption.fold(false)(_ == classOf[Class[_]]))
    )

  def isDiscoverableSuite(testClass: Class[_]): Boolean = !testClass.isAnnotationPresent(classOf[DoNotDiscover])

  def isAccessibleSuite(testClass: Class[_]): Boolean = classOf[Suite].isAssignableFrom(testClass) &&
    Modifier.isPublic(testClass.getModifiers) &&
    !Modifier.isAbstract(testClass.getModifiers) &&
    Try(testClass.getConstructor()).map(c => Modifier.isPublic(c.getModifiers)).getOrElse(false)

}
