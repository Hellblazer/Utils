/** (C) Copyright 2013 Hal Hildebrand, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.hellblazer.utils;

import java.io.File;
import java.io.IOException;

/**
 * Provides a temporary directory that will be auto magically cleaned up in Java
 * 1.7 try-with-resources blocks.
 * 
 * @author hhildebrand
 * 
 */
public class TemporaryDirectory implements AutoCloseable {
	public final File directory;

	/**
	 * Creates a new empty file in the specified directory, using the given
	 * prefix and suffix strings to generate its name.
	 * 
	 * 
	 * @param prefix
	 *            The prefix string to be used in generating the file's name;
	 *            must be at least three characters long
	 * 
	 * @param suffix
	 *            The suffix string to be used in generating the file's name;
	 *            may be <code>null</code>, in which case the suffix
	 *            <code>".tmp"</code> will be used
	 * 
	 * 
	 * @throws IllegalArgumentException
	 *             If the <code>prefix</code> argument contains fewer than three
	 *             characters
	 * 
	 * @throws IOException
	 *             If a file could not be created
	 * 
	 * @throws SecurityException
	 *             If a security manager exists and its <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *             method does not allow a file to be created
	 * 
	 * @see java.io.file.File#createTempFile(String, String)
	 */
	public TemporaryDirectory(String prefix, String suffix) throws IOException {
		this(prefix, suffix, null);
	}

	/**
	 * Creates a new empty file in the specified directory, using the given
	 * prefix and suffix strings to generate its name.
	 * 
	 * 
	 * @param prefix
	 *            The prefix string to be used in generating the file's name;
	 *            must be at least three characters long
	 * 
	 * @param suffix
	 *            The suffix string to be used in generating the file's name;
	 *            may be <code>null</code>, in which case the suffix
	 *            <code>".tmp"</code> will be used
	 * 
	 * @param directory
	 *            The directory in which the file is to be created, or
	 *            <code>null</code> if the default temporary-file directory is
	 *            to be used
	 * 
	 * 
	 * @throws IllegalArgumentException
	 *             If the <code>prefix</code> argument contains fewer than three
	 *             characters
	 * 
	 * @throws IOException
	 *             If a file could not be created
	 * 
	 * @throws SecurityException
	 *             If a security manager exists and its <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *             method does not allow a file to be created
	 * 
	 * @see java.io.file.File#createTempFile(String, String, File)
	 */
	public TemporaryDirectory(String prefix, String suffix, File dir)
			throws IOException {
		directory = File.createTempFile(prefix, suffix, dir);
		Utils.initializeDirectory(directory);
	}

	@Override
	public void close() throws Exception {
		if (directory != null) {
			Utils.remove(directory);
		}
	}

	public String toString() {
		return String.format("temp dir[%s]", directory);
	}

}
