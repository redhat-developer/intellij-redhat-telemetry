/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.telemetry.core.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AnonymizeUtilsTest {

    @Test
    public void anonymizeEmail_should_replace_email() {
        // given
        String email = "adietish@redhat.com";
        String msgWithEmail = "This is the email address " + email + " within a message.";
        // when
        String anonymized = AnonymizeUtils.anonymizeEmail(msgWithEmail);
        // then
        assertThat(anonymized).doesNotContain(email);
        assertThat(anonymized).contains(AnonymizeUtils.ANONYMOUS_EMAIL);
    }

    @Test
    public void anonymizeEmail_should_NOT_replace_bogus_email() {
        // given
        String bogusEmail = "adietish@redhat";
        String msgWithBogusEmail = "This is the email address " + bogusEmail + " within a message.";
        // when
        String anonymized = AnonymizeUtils.anonymizeEmail(msgWithBogusEmail);
        // then
        assertThat(anonymized).contains(bogusEmail);
        assertThat(anonymized).doesNotContain(AnonymizeUtils.ANONYMOUS_EMAIL);
    }

    @Test
    public void anonymizeUserName_should_replace_username() {
        // given
        String username = AnonymizeUtils.USER_NAME;
        String msgWithUsername = "This is the username " + username + " within a message.";
        // when
        String anonymized = AnonymizeUtils.anonymizeUserName(msgWithUsername);
        // then
        assertThat(anonymized).doesNotContain(username);
        assertThat(anonymized).contains(AnonymizeUtils.ANONYMOUS_USER_NAME);
    }

    @Test
    public void anonymizeHomeDir_should_replace_homedir() {
        // given
        String homeDir = AnonymizeUtils.HOME_DIR;
        String msgWithHomeDir = "This is the path to the " + homeDir + " within a message.";
        // when
        String anonymized = AnonymizeUtils.anonymizeHomeDir(msgWithHomeDir);
        // then
        assertThat(anonymized).doesNotContain(homeDir);
        assertThat(anonymized).contains(AnonymizeUtils.ANONYMOUS_HOMEDIR);
    }

    @Test
    public void anonymizeHomeDir_should_NOT_replace_other_directory() {
        // given
        String otherDir = "C:\\\\Documents and Settings\\\\All Users";
        String msgWithOtherDir = "This is the path to the " + otherDir + " within a message.";
        // when
        String anonymized = AnonymizeUtils.anonymizeHomeDir(msgWithOtherDir);
        // then
        assertThat(anonymized).contains(otherDir);
        assertThat(anonymized).doesNotContain(AnonymizeUtils.ANONYMOUS_HOMEDIR);
    }

    @Test
    public void anonymizeTmpDir_should_replace_tmpDir() {
        // given
        String tmpDir = AnonymizeUtils.TMP_DIR;
        String msgWithTmpDir = "This is the path to the " + tmpDir + " within a message.";
        // when
        String anonymized = AnonymizeUtils.anonymizeTmpDir(msgWithTmpDir);
        // then
        assertThat(anonymized).doesNotContain(tmpDir);
        assertThat(anonymized).contains(AnonymizeUtils.ANONYMOUS_TMPDIR);
    }

    @Test
    public void replaceIP_should_replace_IP() {
        // given
        String ip = "192.168.0.1";
        String msgWithIp = "This is the ip " + ip + " within a message.";
        // when
        String anonymized = AnonymizeUtils.anonymizeIP(msgWithIp);
        // then
        assertThat(anonymized).doesNotContain(ip);
        assertThat(anonymized).contains(AnonymizeUtils.ANONYMOUS_IP);
    }

    @Test
    public void replaceIP_should_NOT_replace_bogus_IP() {
        // given
        String bogusIP = "10.0.12";
        String msgWithIp = "This is the ip " + bogusIP + " within a message.";
        // when
        String anonymized = AnonymizeUtils.anonymizeIP(msgWithIp);
        // then
        assertThat(anonymized).contains(bogusIP);
        assertThat(anonymized).doesNotContain(AnonymizeUtils.ANONYMOUS_IP);
    }

    @Test
    public void anonymizeResource_should_anonymize_resource_and_namespace() {
        // given
        String resource = "smurf village";
        String namespace = "blue county";
        String withResourceAndNamespace = resource + " is located in " + namespace;
        assertThat(withResourceAndNamespace).contains(resource).contains(namespace);
        // when
        String anonymized = AnonymizeUtils.anonymizeResource(resource, namespace, withResourceAndNamespace);
        // then
        assertThat(anonymized)
                .doesNotContain(resource)
                .doesNotContain(namespace);
        assertThat(anonymized)
                .contains(AnonymizeUtils.ANONYMOUS_RESOURCENAME)
                .contains(AnonymizeUtils.ANONYMOUS_NAMESPACE);
    }

}
