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
import io.qameta.allure.entity.Parameter;
import io.qameta.allure.entity.TestResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultResultsVisitorTest {

    /**
     * Verifies the storage boundary fills missing identity hashes from a result's full name and parameters.
     */
    @Description
    @Test
    void shouldEnsureTestResultIdentityHashes() {
        final DefaultResultsVisitor visitor = new DefaultResultsVisitor(ConfigurationBuilder.empty().build());
        final TestResult result = new TestResult()
                .setFullName("org.example.ExampleTest.test")
                .setParameters(Collections.singletonList(new Parameter().setName("argument").setValue("value")));

        visitor.visitTestResult(result);

        assertThat(result.getTestCaseHash()).isEqualTo("f103a4030d01bbae3f83d8a344f63a47");
        assertThat(result.getParametersHash()).isEqualTo("310bf7d9fc9765b03f3a78f1816f40a8");
        assertThat(result.getRetryHash())
                .isEqualTo("f103a4030d01bbae3f83d8a344f63a47.310bf7d9fc9765b03f3a78f1816f40a8");
    }

    /**
     * Verifies a result without a stable test case identity cannot join a retry group.
     */
    @Description
    @Test
    void shouldNotCreateRetryHashWithoutFullName() {
        final DefaultResultsVisitor visitor = new DefaultResultsVisitor(ConfigurationBuilder.empty().build());
        final TestResult result = new TestResult();

        visitor.visitTestResult(result);

        assertThat(result.getTestCaseHash()).isNull();
        assertThat(result.getParametersHash()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
        assertThat(result.getRetryHash()).isNull();
    }

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
        Allure.addAttachment(
                "Visited attachment metadata", "text/plain", String.format(
                        "name=%s%ntype=%s%nsource=%s%ncontent=%s%n",
                        attachment.getName(),
                        attachment.getType(),
                        attachment.getSource(),
                        Files.readString(attachmentFile)
                )
        );

        assertThat(attachment.getName()).isEqualTo("custom-attachment.foobar");
        assertThat(attachment.getType()).isEqualTo(DefaultResultsVisitor.APPLICATION_OCTET_STREAM);
        assertThat(attachment.getSource()).endsWith(".foobar");
        assertThat(attachment.getSize()).isEqualTo(14L);
    }
}
