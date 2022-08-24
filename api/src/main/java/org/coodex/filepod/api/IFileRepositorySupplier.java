package org.coodex.filepod.api;

import org.coodex.filerepository.api.IFileRepository;

import java.util.function.Supplier;

public interface IFileRepositorySupplier<T> {
    String getRepositoryName();

    Class<T> getArgumentType();

    Supplier<IFileRepository> getSupplier(T arg);
}
