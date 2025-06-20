/*
 *  Copyright 2016-2024 Qameta Software Inc
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
package io.qameta.allure.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClasspathEntityResolver only allows to resolve dtd schemas, available in classpath.
 * For all other entities it returns empty schema.
 *
 * @author charlie (Dmitry Baev).
 */
public class ClasspathEntityResolver implements EntityResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathEntityResolver.class);

    private static final Pattern PATTERN = Pattern.compile("^.*/(.+\\.[dD][tT][dD]])$");

    private static final byte[] EMPTY_SCHEMA_BYTES = "".getBytes(StandardCharsets.UTF_8);

    @Override
    public InputSource resolveEntity(final String publicId,
                                     final String systemId) {
        if (Objects.nonNull(systemId)) {
            final Matcher matcher = PATTERN.matcher(systemId);
            if (matcher.matches()) {
                final String schemaName = matcher.group();
                final String resourceName = "dtd/" + schemaName;

                return classpathInputSource(publicId, systemId, resourceName);
            }
        }

        return getInputSource(publicId, systemId, EMPTY_SCHEMA_BYTES);
    }

    private InputSource classpathInputSource(final String publicId,
                                             final String systemId,
                                             final String resourceName) {
        final byte[] schema = getBytes(resourceName);
        return getInputSource(publicId, systemId, schema);
    }

    private static InputSource getInputSource(final String publicId, final String systemId, final byte[] schema) {
        final InputSource inputSource = new InputSource(new ByteArrayInputStream(schema));
        inputSource.setPublicId(publicId);
        inputSource.setSystemId(systemId);
        inputSource.setEncoding(StandardCharsets.UTF_8.name());
        return inputSource;
    }

    @SuppressWarnings("AssignmentInOperand")
    private static byte[] getBytes(final String resourceName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (Objects.isNull(is)) {
                LOGGER.debug("schema resource {} not found", resourceName);
                return EMPTY_SCHEMA_BYTES;
            }
            final byte[] buffer = new byte[1000];

            final ByteArrayOutputStream byteArrayOutputStream
                    = new ByteArrayOutputStream();

            int temp;

            while ((temp = is.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, temp);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.debug("can't read schema resource {}", resourceName, e);
            return EMPTY_SCHEMA_BYTES;
        }
    }
}
