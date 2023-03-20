package io.github.janhicken.surefire.scalatest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.maven.surefire.api.booter.BaseProviderFactory;
import org.apache.maven.surefire.api.booter.ForkingReporterFactory;
import org.apache.maven.surefire.api.booter.MasterProcessChannelEncoder;
import org.apache.maven.surefire.api.report.ReportEntry;
import org.apache.maven.surefire.api.report.ReporterConfiguration;
import org.apache.maven.surefire.api.report.StackTraceWriter;
import org.apache.maven.surefire.api.report.TestOutputReportEntry;
import org.apache.maven.surefire.api.report.TestSetReportEntry;
import org.apache.maven.surefire.api.testset.RunOrderParameters;
import org.apache.maven.surefire.api.util.DefaultScanResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.scalatest.tools.SurefireReporter;

public class ScalaTestProviderTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private ScalaTestProvider provider;
  private MemoizingEventChannel eventChannel;

  @Before
  public void setUp() throws Exception {
    eventChannel = new MemoizingEventChannel();
    final BaseProviderFactory providerParameters = new BaseProviderFactory(false);

    providerParameters.setClassLoaders(getClass().getClassLoader());
    providerParameters.setProviderProperties(new HashMap<>());
    providerParameters.setReporterFactory(new ForkingReporterFactory(true, eventChannel));
    providerParameters.setReporterConfiguration(
        new ReporterConfiguration(temporaryFolder.newFolder(), true));
    providerParameters.setRunOrderParameters(RunOrderParameters.alphabetical());

    final List<String> classNames =
        Stream.of(
                ScalaTestProvider.class,
                SurefireReporter.class,
                ScalaTestProviderTest.class,
                ExampleSpec.class,
                HiddenSpec.class)
            .map(Class::getName)
            .collect(Collectors.toList());
    new DefaultScanResult(classNames).writeTo(providerParameters.getProviderProperties());

    provider = new ScalaTestProvider(providerParameters);
  }

  @Test
  public void test_findExampleSpec() {
    final Iterable<Class<?>> suites = provider.getSuites();
    final List<Class<?>> list =
        StreamSupport.stream(suites.spliterator(), false).collect(Collectors.toList());
    assertEquals("The discovery suites list must have exactly one entry", 1, list.size());
    assertEquals(
        "The discovery suites list must contain only the ExampleSpec",
        ExampleSpec.class,
        list.get(0));
  }

  @Test
  public void test_run() {
    provider.invoke(null);

    assertFalse(eventChannel.reportEntries.isEmpty());
    assertTrue(eventChannel.containsMessage("- should do something without exception"));
    assertTrue(eventChannel.containsMessage("- should ignore something !!! IGNORED !!!"));
    assertTrue(eventChannel.containsMessage("- should fail dividing by 0 *** FAILED ***"));
    assertFalse(eventChannel.containsMessage("- not be discovered"));
  }

  static class MemoizingEventChannel implements MasterProcessChannelEncoder {

    private final List<TestOutputReportEntry> reportEntries = new ArrayList<>();

    public boolean containsMessage(final String message) {
      for (final TestOutputReportEntry entry : reportEntries) {
        if (entry.getLog().contains(message)) return true;
      }
      return false;
    }

    @Override
    public boolean checkError() {
      return false;
    }

    @Override
    public void onJvmExit() {}

    @Override
    public void testSetStarting(
        final TestSetReportEntry reportEntry, final boolean trimStackTraces) {}

    @Override
    public void testSetCompleted(
        final TestSetReportEntry reportEntry, final boolean trimStackTraces) {}

    @Override
    public void testStarting(final ReportEntry reportEntry, final boolean trimStackTraces) {}

    @Override
    public void testSucceeded(final ReportEntry reportEntry, final boolean trimStackTraces) {}

    @Override
    public void testFailed(final ReportEntry reportEntry, final boolean trimStackTraces) {}

    @Override
    public void testSkipped(final ReportEntry reportEntry, final boolean trimStackTraces) {}

    @Override
    public void testError(final ReportEntry reportEntry, final boolean trimStackTraces) {}

    @Override
    public void testAssumptionFailure(
        final ReportEntry reportEntry, final boolean trimStackTraces) {}

    @Override
    public void testOutput(final TestOutputReportEntry reportEntry) {
      reportEntries.add(reportEntry);
    }

    @Override
    public void consoleInfoLog(final String msg) {}

    @Override
    public void consoleErrorLog(final String msg) {}

    @Override
    public void consoleErrorLog(final Throwable t) {}

    @Override
    public void consoleErrorLog(final String msg, final Throwable t) {}

    @Override
    public void consoleErrorLog(
        final StackTraceWriter stackTraceWriter, final boolean trimStackTraces) {}

    @Override
    public void consoleDebugLog(final String msg) {}

    @Override
    public void consoleWarningLog(final String msg) {}

    @Override
    public void bye() {}

    @Override
    public void stopOnNextTest() {}

    @Override
    public void acquireNextTest() {}

    @Override
    public void sendExitError(
        final StackTraceWriter stackTraceWriter, final boolean trimStackTraces) {}
  }
}
