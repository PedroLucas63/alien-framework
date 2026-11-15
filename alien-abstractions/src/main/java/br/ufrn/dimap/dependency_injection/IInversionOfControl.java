package br.ufrn.dimap.dependency_injection;

import br.ufrn.dimap.configuration.IConfiguration;
import br.ufrn.dimap.dependency_injection.types.LifecycleType;

public interface IInversionOfControl {
    void autoRegister();
    void passivateIdleUsers();

    boolean contains(Class<?> type, String name);
    default boolean contains(Class<?> type) {
        return contains(type, null);
    }

    <T> T spawn(Class<T> type, String name);
    default <T> T spawn(Class<T> type) {
        return spawn(type, null);
    }

    <TInstance> void registerConfig(TInstance instance, String name);
    default <TInstance> void registerConfig(TInstance instance) {
        registerConfig(instance, null);
    }

    <TInterface, TInstance extends TInterface> void registerConfig(
            Class<TInterface> iType, TInstance instance, String name
    );
    default <TInterface, TInstance extends TInterface> void registerConfig(
            Class<TInterface> iType, TInstance instance
    ) {
        registerConfig(iType, instance, null);
    }

    void registerConfig(Class<?> type, IConfiguration configuration, String prefix, String name);
    default void registerConfig(Class<?> type, IConfiguration configuration, String prefix) {
        registerConfig(type, configuration, prefix, null);
    }
    default void registerConfig(Class<?> type, IConfiguration configuration) {
        registerConfig(type, configuration, null, null);
    }

    <TInterface, TInstance extends TInterface> void registerConfig(
            Class<TInterface> iType,
            Class<TInstance> type,
            IConfiguration configuration,
            String prefix,
            String name
    );
    default <TInterface, TInstance extends TInterface> void registerConfig(
            Class<TInterface> iType,
            Class<TInstance> type,
            IConfiguration configuration,
            String prefix
    ) {
        registerConfig(iType, type, configuration, prefix, null);
    }
    default <TInterface, TInstance extends TInterface> void registerConfig(
            Class<TInterface> iType,
            Class<TInstance> type,
            IConfiguration configuration
    ) {
        registerConfig(iType, type, configuration, null, null);
    }

    <TInstance> void registerEternal(TInstance instance, String name);
    default <TInstance> void registerEternal(TInstance instance) {
        registerEternal(instance, null);
    }

    <TInterface, TInstance extends TInterface> void registerEternal(
            Class<TInterface> iType, TInstance instance, String name
    );
    default <TInterface, TInstance extends TInterface> void registerEternal(
            Class<TInterface> iType, TInstance instance
    ) {
        registerEternal(iType, instance, null);
    }

    void registerEternal(Class<?> type, String name);
    default void registerEternal(Class<?> type) {
        registerEternal(type, (String) null);
    }

    <TInterface, TInstance extends TInterface> void registerEternal(
            Class<TInterface> iType, Class<TInstance> type, String name
    );
    default <TInterface, TInstance extends TInterface> void registerEternal(
            Class<TInterface> iType, Class<TInstance> type
    ) {
        registerEternal(iType, type, null);
    }

    void registerPerRequest(Class<?> type, String name);
    default void registerPerRequest(Class<?> type) {
        registerPerRequest(type, (String) null);
    }

    <TInterface, TInstance extends TInterface> void registerPerRequest(
            Class<TInterface> iType, Class<TInstance> type, String name
    );
    default <TInterface, TInstance extends TInterface> void registerPerRequest(
            Class<TInterface> iType, Class<TInstance> type
    ) {
        registerPerRequest(iType, type, null);
    }

    void registerPerUser(Class<?> type, String name);
    default void registerPerUser(Class<?> type) {
        registerPerUser(type, (String) null);
    }

    <TInterface, TInstance extends TInterface> void registerPerUser(
            Class<TInterface> iType, Class<TInstance> type, String name
    );
    default <TInterface, TInstance extends TInterface> void registerPerUser(
            Class<TInterface> iType, Class<TInstance> type
    ) {
        registerPerUser(iType, type, null);
    }

    void register(Class<?> type);
    <TInterface, TInstance extends TInterface> void register(
            Class<TInterface> iType, Class<TInstance> type
    );
    void register(Class<?> type, String name, LifecycleType lifecycleType);
    <TInterface, TInstance extends TInterface> void register(
            Class<TInterface> iType, Class<TInstance> type, String name, LifecycleType lifecycleType
    );
}
