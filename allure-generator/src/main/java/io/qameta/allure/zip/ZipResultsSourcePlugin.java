/*
 *  Copyright 2019 Qameta Software OÜ
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.qameta.allure.zip;

import io.qameta.allure.Reader;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ZipResultsSourcePlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipResultsSourcePlugin.class);

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        if (!isZip(directory)) {
            return;
        }


        try (FileSystem zipFile = FileSystems.newFileSystem(directory, (ClassLoader) null)) {
            final Stream<Reader> otherReaders = configuration
                    .getReaders()
                    .stream()
                    .filter(this::isNotZipResultsSourcePlugin);

            zipFile.getRootDirectories().forEach(directoryInZip ->
                    otherReaders.forEach(reader -> reader.readResults(configuration, visitor, directoryInZip))
            );

        } catch (IOException e) {
            LOGGER.warn("Failed to create zip file system from {}", directory, e);
        }
    }

    private boolean isNotZipResultsSourcePlugin(final Reader reader) {
        return !getClass().isInstance(reader);
    }

    public static boolean isZip(final Path path) {
        boolean isZip = false;

        try {
            isZip = "application/zip".equals(Files.probeContentType(path));
        } catch (IOException e) {
            LOGGER.trace("Failed to probe content type", e);
        }

        if (!isZip) {
            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
                final int fileSignature = raf.readInt();
                isZip = fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
            } catch (IOException | UnsupportedOperationException e) {
                LOGGER.trace("Failed to read {}", path, e);
            }
        }


        return isZip;
    }
}
