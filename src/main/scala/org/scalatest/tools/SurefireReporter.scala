package org.scalatest.tools

import org.apache.maven.surefire.api.report.RunMode.NORMAL_RUN
import org.apache.maven.surefire.api.report.{SimpleReportEntry, TestOutputReportEntry, TestReportListener}
import org.scalatest.events._
import org.scalatest.tools.SurefireReporter.runListener

class SurefireReporter
    extends StandardOutReporter(
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
      case TestStarting(ord, suiteName, _, suiteClassName, testName, testText, _, _, _, _, _, _) =>
        runListener.testStarting(
          new SimpleReportEntry(NORMAL_RUN, ord.runStamp, suiteClassName.orNull, suiteName, testName, testText)
        )
      case TestSucceeded(ord, suiteName, _, suiteClassName, testName, testText, _, _, _, _, _, _, _, _) =>
        runListener.testSucceeded(
          new SimpleReportEntry(NORMAL_RUN, ord.runStamp, suiteClassName.orNull, suiteName, testName, testText)
        )
      case TestFailed(ord, message, suiteName, _, suiteClassName, testName, testText, _, _, _, _, _, _, _, _, _, _) =>
        runListener.testFailed(
          new SimpleReportEntry(NORMAL_RUN, ord.runStamp, suiteClassName.orNull, suiteName, testName, testText, message)
        )
      case TestIgnored(ord, suiteName, _, suiteClassName, testName, testText, _, _, _, _, _) =>
        runListener.testSkipped(
          new SimpleReportEntry(NORMAL_RUN, ord.runStamp, suiteClassName.orNull, suiteName, testName, testText)
        )
      case TestCanceled(ord, message, suiteName, _, suiteClassName, testName, testText, _, _, _, _, _, _, _, _, _) =>
        runListener.testError(
          new SimpleReportEntry(NORMAL_RUN, ord.runStamp, suiteClassName.orNull, suiteName, testName, testText, message)
        )
      case SuiteCompleted(ord, suiteName, _, suiteClassName, _, _, _, _, _, _, _) =>
        runListener.testSetCompleted(
          new SimpleReportEntry(NORMAL_RUN, ord.runStamp, suiteClassName.orNull, suiteName, null, null)
        )
      case _ =>
    }

  }
}

object SurefireReporter {
  var runListener: TestReportListener[TestOutputReportEntry] = _
}
