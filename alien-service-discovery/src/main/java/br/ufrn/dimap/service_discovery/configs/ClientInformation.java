package br.ufrn.dimap.service_discovery.configs;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;

@ConfigurationProperties
public class ClientInformation {
    public String hostname;
    public int port;
}
