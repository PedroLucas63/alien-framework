package br.ufrn.dimap.spawning_pool;

import br.ufrn.dimap.dependency_injection.IServicePool;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InstancePool implements IServicePool {
    private final int poolSize;
    private final Object[] specimens;
    private final Lock pointerLock = new ReentrantLock();

    private int currentSize = 0;
    private int pointer = 0;

    public InstancePool(int poolSize) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be positive: " + poolSize);
        }
        this.poolSize = poolSize;
        this.specimens = new Object[poolSize];
    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }

    @Override
    public int getCurrentSize() {
        pointerLock.lock();
        try {
            return currentSize;
        } finally {
            pointerLock.unlock();
        }
    }

    @Override
    public Object Get() {
        pointerLock.lock();
        try {
            if (currentSize < poolSize) {
                throw new IllegalStateException("The pool is not full.");
            }
            pointer %= poolSize;
            return specimens[pointer++];
        } finally {
            pointerLock.unlock();
        }
    }

    @Override
    public Object Register(Object instance) {
        pointerLock.lock();
        try {
            if (currentSize == poolSize) {
                throw new IllegalStateException("The pool is full.");
            }
            specimens[currentSize++] = instance;
            return instance;
        } finally {
            pointerLock.unlock();
        }
    }
}
