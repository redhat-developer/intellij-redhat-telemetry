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
package com.redhat.devtools.intellij.telemetry.core.configuration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class ConsumerClasspathConfiguration extends FileConfiguration {

	public ConsumerClasspathConfiguration(Path file, IConfiguration parent) {
		super(file, parent);
	}

	@Override
	protected InputStream createFileInputStream(Path path) throws FileNotFoundException {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(path.toString());
	}
}
