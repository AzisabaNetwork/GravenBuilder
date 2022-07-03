package net.azisaba.gravenbuilder.util;

public interface ThrowableSupplier<T, X extends Throwable> {
    T get() throws X;
}
