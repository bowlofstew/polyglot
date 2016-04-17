package polyglot.config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.ConfigurationSet;
import polyglot.ConfigProto.OutputConfiguration.Destination;

/** Unit tests for {@link ConfigurationLoader}. */
public class ConfigurationLoaderTest {
  @Rule public MockitoRule mockitoJunitRule = MockitoJUnit.rule();
  @Mock private CommandLineArgs mockOverrides;

  @Test
  public void loadsDefaultConfig() {
    Configuration defaultConfig =
        ConfigurationLoader.forDefaultConfigSet().getDefaultConfiguration();
    assertThat(defaultConfig).isEqualTo(Configuration.getDefaultInstance());

    assertThat(defaultConfig.getCallConfig().getUseTls()).isFalse();
    assertThat(defaultConfig.getOutputConfig().getDestination()).isEqualTo(Destination.LOG);
  }

  @Test(expected = IllegalStateException.class)
  public void throwsIfAskedToLoadNamedFromDefaultSet() {
    ConfigurationLoader.forDefaultConfigSet().getNamedConfiguration("asdf");
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsIfNamedConfigMissing() {
    ConfigurationLoader.forConfigSet(ConfigurationSet.getDefaultInstance())
        .getNamedConfiguration("asfd");
  }

  @Test
  public void loadsNamedConfig() {
    ConfigurationLoader loader = ConfigurationLoader.forConfigSet(ConfigurationSet.newBuilder()
        .addConfigurations(namedConfig("foo"))
        .addConfigurations(namedConfig("bar"))
        .build());
    assertThat(loader.getNamedConfiguration("foo").getName()).isEqualTo("foo");
  }

  @Test
  public void appliesOverrides() {
    when(mockOverrides.useTls()).thenReturn(Optional.of(true));
    when(mockOverrides.outputFilePath()).thenReturn(Optional.of(Paths.get("asdf")));
    ConfigurationLoader loader = ConfigurationLoader
        .forDefaultConfigSet()
        .withOverrides(mockOverrides);
    assertThat(loader.getDefaultConfiguration().getCallConfig().getUseTls()).isTrue();
    assertThat(loader.getDefaultConfiguration().getOutputConfig().getDestination())
        .isEqualTo(Destination.FILE);
  }

  private static Configuration namedConfig(String name) {
    return Configuration.newBuilder()
        .setName(name)
        .build();
  }
}