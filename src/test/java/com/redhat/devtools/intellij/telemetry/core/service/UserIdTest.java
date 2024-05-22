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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("percentile_for_hashCode")
    public void getPercentile_should_return_value_for_hashCode(int hashCode, float expectedPercentile) {
        // given
        UserId userId = new FixedHashCodeUserId(hashCode);
        // when
        float percentile = userId.getPercentile();
        // then
        assertThat(percentile).isEqualTo(expectedPercentile);
    }

    @ParameterizedTest
    @MethodSource("hashCode_for_uuid")
    public void hashCode_should_return_value_for_uuid(String uuid, int expectedHashCode) {
        // given
        UserId userId = new TestableUserId(true, uuid);
        // when
        int hashCode = userId.hashCode();
        // then
        assertThat(hashCode).isEqualTo(expectedHashCode);
    }

    public static Stream<Arguments> hashCode_for_uuid() {
        return Stream.of(
                Arguments.of("6c4698ed-85f3-4448-9b0f-10897b8b4178", 349419899), // uuid -> hashCode
                Arguments.of("870c8e59-9299-437f-a4dd-5bd331352ec7", -2018427608),
                Arguments.of("c020f453-6811-4545-a3aa-3c5cc17d6fe8", -252979871),
                Arguments.of("db3f9e5e-2dd5-4d81-aac8-aa75333c105c", 1140739481),
                Arguments.of("8abd3beb-c930-46a0-b244-7f1c6f9857da", 82715988),
                Arguments.of("d839a99f-6afc-4309-bcb7-5d1e78eb0241", 1829289193),
                Arguments.of("08f87a61-077a-4cb3-b9f9-4e5751d4dc96", 1602451551),
                Arguments.of("72f09a0e-1fa6-46d1-8322-48ac0ffa4252", -633581890),
                Arguments.of("c1d68afc-a39e-4b89-bb95-e9d7684efe7c", -1103007680),
                Arguments.of("a52ec11d-35bf-4579-88fd-72de5c6a0467", 158094785),
                Arguments.of("d136d7b4-518a-43b6-bb1d-1dafd9e1e52b", 2110423401),
                Arguments.of("ddf95114-333d-41e0-b1ba-d84bc6293634", 1889783579),
                Arguments.of("fb833841-75de-435e-98d2-ab0988712340", 1464118621),
                Arguments.of("71f327fa-e8ed-4fdc-92d9-5de6a1f47229", -367676488),
                Arguments.of("82b4c9f4-73e3-4e4a-b243-dc4aff91b9f6", 224204832),
                Arguments.of("16d122ec-9122-4392-a90f-71504ef40c6f", 1020229945),
                Arguments.of("570447c7-168e-4d3d-be40-e5559dd4f86b", -690069930),
                Arguments.of("cc4ee6ef-6862-4468-ac51-a64f237f84f5", -1247454805),
                Arguments.of("b2ee8320-4dff-44a1-87e2-ca9daa9e24ed", -1381801037),
                Arguments.of("4e97382d-6042-4001-889d-ecc0cb4e8862", -1911346601)
        );
    }

    public static Stream<Arguments> percentile_for_hashCode() {
        return Stream.of(
                Arguments.of(349419899, 0.9899f), // hashCode -> percentile
                Arguments.of(-2018427608, 0.7608f),
                Arguments.of(-252979871, 0.9871f),
                Arguments.of(1140739481, 0.9481f),
                Arguments.of(82715988, 0.5988f),
                Arguments.of(1829289193, 0.9193f),
                Arguments.of(1602451551, 0.1551f),
                Arguments.of(-633581890, 0.189f),
                Arguments.of(-1103007680, 0.768f),
                Arguments.of(158094785, 0.4785f),
                Arguments.of(2110423401, 0.3401f),
                Arguments.of(1889783579, 0.3579f),
                Arguments.of(1464118621, 0.8621f),
                Arguments.of(-367676488, 0.6488f),
                Arguments.of(224204832, 0.4832f),
                Arguments.of(1020229945, 0.9945f),
                Arguments.of(-690069930, 0.993f),
                Arguments.of(-1247454805, 0.4805f),
                Arguments.of(-1381801037, 0.1037f),
                Arguments.of(-1911346601, 0.6601f)
        );
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

    private static class FixedHashCodeUserId extends UserId {

        private final int hashCode;

        public FixedHashCodeUserId(int hashCode) {
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
