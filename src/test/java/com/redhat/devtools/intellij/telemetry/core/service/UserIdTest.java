/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class UserIdTest {

    private static final String UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String INVALID_UUID = "bogus";

    @Test
    void get_should_load_and_not_write_if_file_exists_and_has_valid_UUID() {
        // given
        TestableUserId user = new TestableUserId(true, UUID);
        // when
        user.get();
        // then
        assertThat(user.loaded).isTrue();
        assertThat(user.written).isFalse();
    }

    @Test
    void get_should_not_load_if_file_doesnt_exist() {
        // given
        TestableUserId user = new TestableUserId(false, UUID);
        // when
        user.get();
        // then
        assertThat(user.loaded).isFalse();
    }

    @Test
    void get_should_write_if_file_doesnt_exist() {
        // given
        TestableUserId user = new TestableUserId(false, null);
        // when
        user.get();
        // then
        assertThat(user.written).isTrue();
    }

    @Test
    void get_should_write_if_file_exists_but_is_invalid() {
        // given
        TestableUserId user = new TestableUserId(true, INVALID_UUID);
        // when
        user.get();
        // then
        assertThat(user.written).isTrue();
    }

    private static class TestableUserId extends UserId {

        private final boolean exists;
        private final String loadedUUID;
        boolean loaded = false;
        boolean written = false;

        public TestableUserId(boolean exists, String loadedUUID) {
            this.exists = exists;
            this.loadedUUID = loadedUUID;
        }

        @Override
        public boolean exists(Path file) {
            return exists;
        }

        @Override
        public String load(Path file) {
            this.loaded = true;
            return loadedUUID;
        }

        @Override
        protected void write(String uuid, Path uuidFile) {
            this.written = true;
        }
    }

}
