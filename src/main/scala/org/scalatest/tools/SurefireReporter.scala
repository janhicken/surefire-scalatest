package org.scalatest.tools

import org.apache.maven.surefire.report.{ConsoleStream, RunListener, SimpleReportEntry}
import org.scalatest.events._
import org.scalatest.tools.SurefireReporter.{ConsoleStreamWriter, consoleStream, runListener}

import java.io.{PrintWriter, Writer}
import java.nio.CharBuffer
import scala.io.Codec

class SurefireReporter
    extends PrintReporter(
      new PrintWriter(new ConsoleStreamWriter(consoleStream)),
      presentAllDurations = false,
      presentInColor = true,
      presentShortStackTraces = false,
      presentFullStackTraces = true,
      presentUnformatted = false,
      presentReminder = false,
      presentReminderWithShortStackTraces = false,
      presentReminderWithFullStackTraces = false,
      presentReminderWithoutCanceledTests = false,
      presentFilePathname = false,
      presentJson = false
    ) {

  override def apply(event: Event): Unit = {
    super.apply(event)

    event match {
      case TestStarting(_, suiteName, _, _, testName, _, _, _, _, _, _, _) =>
        runListener.testStarting(new SimpleReportEntry(suiteName, testName))
      case TestSucceeded(_, suiteName, _, _, testName, _, _, _, _, _, _, _, _, _) =>
        runListener.testSucceeded(new SimpleReportEntry(suiteName, testName))
      case TestFailed(_, message, suiteName, _, _, testName, _, _, _, _, _, _, _, _, _, _, _) =>
        runListener.testFailed(new SimpleReportEntry(suiteName, testName, message))
      case TestIgnored(_, suiteName, _, _, testName, _, _, _, _, _, _) =>
        runListener.testSkipped(new SimpleReportEntry(suiteName, testName))
      case TestCanceled(_, message, suiteName, _, _, testName, _, _, _, _, _, _, _, _, _, _) =>
        runListener.testError(new SimpleReportEntry(suiteName, testName, message))
      case SuiteCompleted(_, suiteName, _, _, _, _, _, _, _, _, _) =>
        runListener.testSetCompleted(new SimpleReportEntry(null, suiteName))
      case _ =>
    }

  }
}

object SurefireReporter {

  var consoleStream: ConsoleStream = _
  var runListener: RunListener = _

  class ConsoleStreamWriter(consoleStream: ConsoleStream) extends Writer {

    private val codec = Codec.UTF8

    override def write(chars: Array[Char], off: Int, len: Int): Unit = {
      val charBuffer = CharBuffer.wrap(chars, off, len)
      val byteBuffer = codec.encoder.encode(charBuffer)
      val bytes = byteBuffer.array()
      consoleStream.println(bytes, 0, bytes.length)
    }

    override def write(str: String): Unit = consoleStream.println(str)

    override def flush(): Unit = ()

    override def close(): Unit = ()
  }
}
