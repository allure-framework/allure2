/*
 *  Copyright 2016-2026 Qameta Software Inc
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
package io.qameta.allure;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.entity.Attachment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultResultsVisitorTest {

    /**
     * Verifies falling back to application/octet-stream for unknown attachment types.
     */
    @Description
    @Test
    void shouldFallbackToOctetStreamForUnknownAttachmentTypes(@TempDir final Path temp) throws Exception {
        final Path attachmentFile = temp.resolve("custom-attachment.foobar");
        Files.writeString(attachmentFile, "custom payload");

        final Configuration configuration = ConfigurationBuilder.empty().build();
        final DefaultResultsVisitor visitor = new DefaultResultsVisitor(configuration);

        final Attachment attachment = Allure.step(
                "Visit attachment file with an unknown extension",
                () -> visitor.visitAttachmentFile(attachmentFile)
        );
        Allure.addAttachment(attachment.getName(), "text/plain", Files.readString(attachmentFile));
        Allure.addAttachment("Visited attachment metadata", "text/plain", String.format(
                "name=%s%ntype=%s%nsource=%s%ncontent=%s%n",
                attachment.getName(),
                attachment.getType(),
                attachment.getSource(),
                Files.readString(attachmentFile)
        ));

        assertThat(attachment.getName()).isEqualTo("custom-attachment.foobar");
        assertThat(attachment.getType()).isEqualTo(DefaultResultsVisitor.APPLICATION_OCTET_STREAM);
        assertThat(attachment.getSource()).endsWith(".foobar");
        assertThat(attachment.getSize()).isEqualTo(14L);
    }
}
