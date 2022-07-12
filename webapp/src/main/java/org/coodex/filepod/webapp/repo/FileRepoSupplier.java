package org.coodex.filepod.webapp.repo;

import org.coodex.filerepository.api.IFileRepository;

import java.util.function.Supplier;
@FunctionalInterface
public interface FileRepoSupplier extends Supplier<IFileRepository> {

}
