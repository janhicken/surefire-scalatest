package io.github.janhicken.surefire.scalatest;

import org.apache.maven.surefire.booter.BaseProviderFactory;
import org.apache.maven.surefire.booter.ForkingReporterFactory;
import org.apache.maven.surefire.report.ConsoleStream;
import org.apache.maven.surefire.report.DefaultDirectConsoleReporter;
import org.apache.maven.surefire.report.ReporterConfiguration;
import org.apache.maven.surefire.util.DefaultScanResult;
import org.junit.*;
import org.scalatest.tools.SurefireReporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScalaTestProviderTest {

    private static Path _tmpDir;

    private ScalaTestProvider provider;
    private ByteArrayOutputStream output;

    @BeforeClass
    public static void beforeClass() throws IOException {
        _tmpDir = Files.createTempDirectory("surefire-scalatest");
        _tmpDir.toFile().deleteOnExit();
    }

    @Before
    public void setUp() {
        this.output = new ByteArrayOutputStream();
        final var providerParameters = new TestProviderParameters(
            new PrintStream(output),
            List.of(
                ScalaTestProvider.class, SurefireReporter.class,
                ScalaTestProviderTest.class, ExampleSpec.class
            ),
            _tmpDir
        );

        provider = new ScalaTestProvider(providerParameters);
    }

    @Test
    public void test_findExampleSpec() {
        final var suites = provider.getSuites();
        final var list = StreamSupport.stream(suites.spliterator(), false)
            .collect(Collectors.toList());
        assertEquals("The discovery suites list must have exactly one entry",
            1, list.size());
        assertEquals("The discovery suites list must contain only the ExampleSpec",
            ExampleSpec.class, list.get(0));
    }

    @Test
    public void test_run() {
        provider.invoke(null);

        final var outputString = output.toString(StandardCharsets.UTF_8);
        assertTrue(outputString.contains("- should do something without exception"));
        assertTrue(outputString.contains("- should ignore something !!! IGNORED !!!"));
        assertTrue(outputString.contains("- should fail dividing by 0 *** FAILED ***"));
    }

    static class TestProviderParameters extends BaseProviderFactory {

        private final PrintStream printStream;

        public TestProviderParameters(final PrintStream printStream, final List<Class<?>> discoveredClasses, final Path tmpDir) {
            super(new ForkingReporterFactory(true, printStream), false);
            this.printStream = printStream;

            setClassLoaders(getClass().getClassLoader());
            setProviderProperties(new HashMap<>());
            setReporterConfiguration(new ReporterConfiguration(tmpDir.toFile(), true));

            final var classNames = discoveredClasses.stream().map(Class::getName).collect(Collectors.toList());
            new DefaultScanResult(classNames).writeTo(getProviderProperties());
        }

        @Override
        public ConsoleStream getConsoleLogger() {
            return new DefaultDirectConsoleReporter(printStream);
        }
    }
}
