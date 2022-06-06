package io.github.janhicken.surefire.scalatest;

import org.apache.maven.surefire.booter.BaseProviderFactory;
import org.apache.maven.surefire.booter.ForkingReporterFactory;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ConsoleStream;
import org.apache.maven.surefire.report.DefaultDirectConsoleReporter;
import org.apache.maven.surefire.report.ReporterConfiguration;
import org.apache.maven.surefire.util.DefaultScanResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.scalatest.tools.SurefireReporter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

public class ScalaTestProviderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ScalaTestProvider provider;
    private ByteArrayOutputStream output;

    @Before
    public void setUp() throws Exception {
        this.output = new ByteArrayOutputStream();
        final ProviderParameters providerParameters = new TestProviderParameters(
            new PrintStream(output),
            Arrays.asList(
                ScalaTestProvider.class, SurefireReporter.class,
                ScalaTestProviderTest.class, ExampleSpec.class, HiddenSpec.class
            ),
            temporaryFolder.newFolder()
        );

        provider = new ScalaTestProvider(providerParameters);
    }

    @Test
    public void test_findExampleSpec() {
        final Iterable<Class<?>> suites = provider.getSuites();
        final List<Class<?>> list = StreamSupport.stream(suites.spliterator(), false)
            .collect(Collectors.toList());
        assertEquals("The discovery suites list must have exactly one entry",
            1, list.size());
        assertEquals("The discovery suites list must contain only the ExampleSpec",
            ExampleSpec.class, list.get(0));
    }

    @Test
    public void test_run() throws UnsupportedEncodingException {
        provider.invoke(null);

        final String outputString = output.toString("UTF-8");
        assertTrue(outputString.contains("- should do something without exception"));
        assertTrue(outputString.contains("- should ignore something !!! IGNORED !!!"));
        assertTrue(outputString.contains("- should fail dividing by 0 *** FAILED ***"));
        assertFalse(outputString.contains("- not be discovered"));
    }

    static class TestProviderParameters extends BaseProviderFactory {

        private final PrintStream printStream;

        public TestProviderParameters(final PrintStream printStream, final List<Class<?>> discoveredClasses, final File tmpDir) {
            super(new ForkingReporterFactory(true, printStream), false);
            this.printStream = printStream;

            setClassLoaders(getClass().getClassLoader());
            setProviderProperties(new HashMap<>());
            setReporterConfiguration(new ReporterConfiguration(tmpDir, true));

            final List<String> classNames =
                discoveredClasses.stream().map(Class::getName).collect(Collectors.toList());
            new DefaultScanResult(classNames).writeTo(getProviderProperties());
        }

        @Override
        public ConsoleStream getConsoleLogger() {
            return new DefaultDirectConsoleReporter(printStream);
        }
    }
}
