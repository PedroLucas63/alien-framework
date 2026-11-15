package br.ufrn.dimap.dependency_injection;

public interface IServicePool {
    int getPoolSize();
    int getCurrentSize();
    default boolean isFull() {
        return getCurrentSize() == getPoolSize();
    }

    Object Get();
    Object Register(Object instance);
}
