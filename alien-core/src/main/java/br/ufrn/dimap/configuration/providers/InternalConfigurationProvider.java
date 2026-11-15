package br.ufrn.dimap.configuration.providers;

import br.ufrn.dimap.configuration.IConfigurationProvider;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class InternalConfigurationProvider implements IConfigurationProvider {
    private final Map<String, String> configuration = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public void load() {
        InetAddress address = InetAddress.getLoopbackAddress();
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            for (InetAddress candidate : InetAddress.getAllByName(hostName)) {
                if (candidate instanceof Inet4Address) {
                    address = candidate;
                    break;
                }
            }
        } catch (UnknownHostException ignored) {
            // Fallback to loopback address
        }

        String hostAddress = address.getHostAddress();
        configuration.put("IP", hostAddress);
        configuration.put("HOSTNAME", hostAddress);
        configuration.put("PORT", "8080");
    }

    @Override
    public Optional<String> tryGet(String key) {
        return Optional.ofNullable(configuration.get(key));
    }
}
