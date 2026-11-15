package br.ufrn.dimap.configuration.sources;

import br.ufrn.dimap.configuration.IConfigurationProvider;
import br.ufrn.dimap.configuration.IConfigurationSource;
import br.ufrn.dimap.configuration.providers.InternalConfigurationProvider;

public class InternalConfigurationSource implements IConfigurationSource {
    @Override
    public IConfigurationProvider build() {
        return new InternalConfigurationProvider();
    }
}
