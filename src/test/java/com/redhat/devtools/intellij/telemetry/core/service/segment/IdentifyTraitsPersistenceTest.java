/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.service.segment;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IdentifyTraitsPersistenceTest {

    private IdentifyTraits identifyTraits;

    private TestableIdentifyTraitsPersistence persistence;

    @BeforeEach
    void beforeEach() {
        this.identifyTraits = createIdentifyTraits();
        this.persistence = spy(new TestableIdentifyTraitsPersistence(new Gson().toJson(identifyTraits)));
    }

    @Test
    void get_should_return_stored_traits() {
        // given
        // when
        IdentifyTraits stored = persistence.get();
        // then
        assertThat(stored).isEqualTo(identifyTraits);
    }


    @Test
    void get_should_return_null_if_file_cannot_be_loaded() throws IOException {
        // given
        doThrow(IOException.class)
                .when(persistence).getLines(any());
        // when
        IdentifyTraits stored = persistence.get();
        // then
        assertThat(stored).isNull();
    }

    @Test
    void get_should_load_file_only_once() throws IOException {
        // given
        // when
        persistence.get();
        persistence.get();
        // then
        verify(persistence, times(1)).getLines(any());
    }

    @Test
    void set_should_NOT_write_to_file_if_traits_are_equal() throws IOException {
        // given
        IdentifyTraits identifyTraits = createIdentifyTraits();
        persistence.get(); // initialize stored traits
        // when
        persistence.set(identifyTraits);
        // then
        verify(persistence, never()).writeFile(any(), any());
    }

    @Test
    void set_should_write_to_file() throws IOException {
        // given
        IdentifyTraits identifyTraits = new IdentifyTraits("en-US", "GMT+2:00", "Linux", "42", "Fedora");
        // when
        persistence.set(identifyTraits);
        // then
        verify(persistence).writeFile(any(), any());
    }

    private IdentifyTraits createIdentifyTraits() {
        return new IdentifyTraits(
                "ewokese-Endor locale",
                "AMC+2:00",
                "AlderaanOS",
                "v42",
                "blue green planet distribution");
    }

    private static final class TestableIdentifyTraitsPersistence extends IdentifyTraitsPersistence {

        private final String fileContent;

        private TestableIdentifyTraitsPersistence(String fileContent) {
            this.fileContent = fileContent;
        }

        @Override
        public Stream<String> getLines(Path file) throws IOException {
            return Stream.of(fileContent);
        }

        @Override
        public void createFileAndParent(Path file) throws IOException {
            super.createFileAndParent(file);
        }

        @Override
        public void writeFile(String event, Path file) throws IOException {
            super.writeFile(event, file);
        }
    }
}
