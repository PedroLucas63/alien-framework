package br.ufrn.dimap.configuration;

public interface IFileConfigurationSource extends IConfigurationSource {
    String extension();
    IConfigurationSource create(String path);
}
