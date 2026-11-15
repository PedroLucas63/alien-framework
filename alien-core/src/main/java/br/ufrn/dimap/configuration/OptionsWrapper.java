package br.ufrn.dimap.configuration;

public class OptionsWrapper<TOptions> implements IOptions<TOptions> {
    private final TOptions options;

    public OptionsWrapper(TOptions options) {
        this.options = options;
    }

    @Override
    public TOptions get() {
        return options;
    }
}
