package br.ufrn.dimap.configuration.sources;

import br.ufrn.dimap.configuration.IConfigurationProvider;
import br.ufrn.dimap.configuration.IConfigurationSource;
import br.ufrn.dimap.configuration.providers.EnvConfigurationProvider;

public class EnvConfigurationSource implements IConfigurationSource {
    private final String prefix;

    public EnvConfigurationSource() {
        this(null);
    }

    public EnvConfigurationSource(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public IConfigurationProvider build() {
        return new EnvConfigurationProvider(prefix);
    }
}
