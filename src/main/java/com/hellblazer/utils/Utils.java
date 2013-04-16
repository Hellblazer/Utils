/** 
 * (C) Copyright 2009 Hal Hildebrand, All Rights Reserved
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class Utils {
	private static enum ParsingState {
		BRACKET, DOLLAR, PASS_THROUGH
	}

	public static Object accessField(String fieldName, Object target)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field field;
		try {
			field = target.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = target.getClass().getSuperclass();
			if (superClass == null) {
				throw e;
			}
			return accessField(fieldName, target, superClass);
		}
		field.setAccessible(true);
		return field.get(target);
	}

	public static Object accessField(String fieldName, Object target,
			Class<?> targetClass) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Field field;
		try {
			field = targetClass.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = targetClass.getSuperclass();
			if (superClass == null) {
				throw e;
			}
			return accessField(fieldName, target, superClass);
		}
		field.setAccessible(true);
		return field.get(target);
	}

	/**
	 * Find a free port for any local address
	 * 
	 * @return the port number or -1 if none available
	 */
	public static int allocatePort() {
		return allocatePort(null);
	}

	/**
	 * Find a free port on the interface with the given local address
	 * 
	 * @return the port number or -1 if none available
	 */
	public static int allocatePort(InetAddress host) {
		InetSocketAddress address = host == null ? new InetSocketAddress(0)
				: new InetSocketAddress(host, 0);
		ServerSocket socket = null;
		try {
			socket = new ServerSocket();
			socket.bind(address);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}

	public static void copy(File sourceFile, File destFile) throws IOException {
		copy(sourceFile, destFile, 4096);
	}

	public static void copy(File sourceFile, File destFile, byte[] buffer)
			throws IOException {
		InputStream is = new FileInputStream(sourceFile);
		OutputStream os = new FileOutputStream(destFile);
		copy(is, os, buffer);

	}

	public static void copy(File sourceFile, File destFile, int bufferSize)
			throws IOException {
		copy(sourceFile, destFile, new byte[bufferSize]);
	}

	public static void copy(InputStream is, OutputStream os) throws IOException {
		copy(is, os, 4096);
	}

	public static void copy(InputStream is, OutputStream os, byte[] buffer)
			throws IOException {
		int len;
		while ((len = is.read(buffer)) > 0) {
			os.write(buffer, 0, len);
		}
		is.close();
		os.close();
	}

	public static void copy(InputStream is, OutputStream os, int bufferSize)
			throws IOException {
		copy(is, os, new byte[bufferSize]);
	}

	public static void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (String element : children) {
				copyDirectory(new File(sourceLocation, element), new File(
						targetLocation, element));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	/**
	 * Expand the zip resource into the destination, replacing any ${propName}
	 * style properties with the corresponding values in the substitutions map
	 * 
	 * @param zip
	 *            - the zip file to expand
	 * @param extensions
	 *            - the list of file extensions targeted for property
	 *            substitution
	 * @param substitutions
	 *            - the map of substitutions
	 * @param destination
	 *            - the destination directory for the expansion
	 * 
	 * @throws IOException
	 * @throws ZipException
	 */
	public static void expandAndReplace(File zip, File dest,
			Map<String, String> substitutions, Collection<String> extensions)
			throws ZipException, IOException {
		initializeDirectory(dest);
		if (!dest.exists() && !dest.mkdir()) {
			throw new IOException(String.format(
					"Cannot create destination directory: %s",
					dest.getAbsolutePath()));
		}
		ZipFile zippy = new ZipFile(zip);
		Enumeration<?> e = zippy.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = (ZipEntry) e.nextElement();
			expandAndReplace(dest, zippy, ze, substitutions, extensions);
		}
	}

	/**
	 * 
	 * Copy and transform the zip entry to the destination. If the
	 * transformation extensions contains the entry's extension, then ${xxx}
	 * style parameters are replace with the supplied properties or
	 * System.getProperties()
	 * 
	 * @param dest
	 * @param zf
	 * @param ze
	 * @param extensions
	 * @param properties
	 * @throws IOException
	 */
	public static void expandAndReplace(File dest, ZipFile zf, ZipEntry ze,
			Map<String, String> properties, Collection<String> extensions)
			throws IOException {
		InputStream is = zf.getInputStream(ze);
		try {
			File outFile = new File(dest, ze.getName());
			if (ze.isDirectory()) {
				outFile.mkdirs();
			} else {
				transform(properties, extensions, is, outFile);
			}
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				Logger.getAnonymousLogger().log(Level.FINEST,
						String.format("Error closing %s", ze), e);
			}
		}
	}

	public static byte[] getBits(File classFile) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream fis = new FileInputStream(classFile);
		copy(fis, baos);
		return baos.toByteArray();
	}

	/**
	 * Answer the string representation of the inputstream
	 * 
	 * @param openStream
	 *            - ye olde stream
	 * @return the string the stream represents
	 * @throws IOException
	 *             - if we're boned
	 */
	public static String getString(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos);
		return baos.toString();
	}

	public static void initializeDirectory(File directory) {
		remove(directory);
		if (!directory.mkdirs()) {
			throw new IllegalStateException("Cannot create directtory: "
					+ directory);
		}
	}

	public static void initializeDirectory(String dir) {
		initializeDirectory(new File(dir));
	}

	public static boolean isClose(IOException ioe) {
		return ioe instanceof ClosedChannelException
				|| "Broken pipe".equals(ioe.getMessage())
				|| "Connection reset by peer".equals(ioe.getMessage());
	}

	public static void remove(File directory) {
		if (directory.exists()) {
			for (File file : directory.listFiles()) {
				if (file.isDirectory()) {
					remove(file);
				} else {
					if (!file.delete()) {
						throw new IllegalStateException("Cannot delete file: "
								+ file);
					}
				}
			}
			if (!directory.delete()) {
				throw new IllegalStateException("Cannot delete directory: "
						+ directory);
			}
		}
	}

	/**
	 * Go through the input stream and replace any occurance of ${p} with the
	 * props.get(p) value. If there is no such property p defined, then the ${p}
	 * reference will remain unchanged.
	 * 
	 * If the property reference is of the form ${p:v} and there is no such
	 * property p, then the default value v will be returned.
	 * 
	 * If the property reference is of the form ${p1,p2} or ${p1,p2:v} then the
	 * primary and the secondary properties will be tried in turn, before
	 * returning either the unchanged input, or the default value.
	 * 
	 * @param in
	 *            - the stream with possible ${x} references
	 * @param out
	 *            - the output for the transformed input
	 * @param props
	 *            - the source for ${x} property ref values, null means use
	 *            System.getProperty()
	 */
	public static void replaceProperties(final InputStream in,
			final OutputStream out, final Map<String, String> props)
			throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(in));
		Writer writer = new BufferedWriter(new OutputStreamWriter(out));
		ParsingState state = ParsingState.PASS_THROUGH;

		StringBuffer keyBuffer = null;
		for (int next = reader.read(); next != -1; next = reader.read()) {
			char c = (char) next;
			switch (state) {
			case PASS_THROUGH: {
				if (c == '$') {
					state = ParsingState.DOLLAR;
				} else {
					writer.append(c);
				}
				break;
			}
			case DOLLAR: {
				if (c == '{') {
					state = ParsingState.BRACKET;
					keyBuffer = new StringBuffer();
				} else if (c == '$') {
					writer.append('$'); // just saw $$
				} else {
					state = ParsingState.PASS_THROUGH;
					writer.append('$');
					writer.append(c);
				}
				break;
			}
			case BRACKET: {
				if (c == '}') {
					state = ParsingState.PASS_THROUGH;
					if (keyBuffer.length() == 0) {
						writer.append("${}");
					} else {
						String value = null;
						String key = keyBuffer.toString();
						value = findValue(key, props);

						if (value != null) {
							writer.append(value);
						} else {
							writer.append("${");
							writer.append(key);
							writer.append('}');
						}
					}
					keyBuffer = null;
				} else if (c == '$') {
					// We're inside of a ${ already, so bail and reset
					state = ParsingState.DOLLAR;
					writer.append("${");
					writer.append(keyBuffer.toString());
					keyBuffer = null;
				} else {
					keyBuffer.append(c);
				}
			}
			}
		}
		writer.flush();
	}

	public static boolean waitForCondition(int maxWaitTime, Condition condition) {
		return waitForCondition(maxWaitTime, 100, condition);
	}

	public static boolean waitForCondition(int maxWaitTime,
			final int sleepTime, Condition condition) {
		long endTime = System.currentTimeMillis() + maxWaitTime;
		while (System.currentTimeMillis() < endTime) {
			if (condition.isTrue()) {
				return true;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		return false;
	}

	/**
	 * @param properties
	 * @param extensions
	 * @param is
	 * @param outFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void transform(Map<String, String> properties,
			Collection<String> extensions, InputStream is, File outFile)
			throws FileNotFoundException, IOException {
		File parent = outFile.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			if (extensions.contains(getExtension(outFile.getName()))) {
				replaceProperties(is, fos, properties);
			} else {
				copy(is, fos);
			}
		} finally {
			try {
				fos.close();
			} catch (IOException ioe) {
			}
		}
	}

	/**
	 * @param key
	 * @param props
	 * @return
	 */
	protected static String findValue(final String key,
			final Map<String, String> props) {
		String value;
		// check from the properties
		if (props != null) {
			value = props.get(key.toString());
		} else {
			value = System.getProperty(key);
		}
		if (value == null) {
			// Check for a default value ${key:default}
			int colon = key.indexOf(':');
			if (colon > 0) {
				String realKey = key.substring(0, colon);
				if (props != null) {
					value = props.get(realKey);
				} else {
					value = System.getProperty(realKey);
				}

				if (value == null) {
					// Check for a composite key, "key1,key2"
					value = resolveCompositeKey(realKey, props);

					// Not a composite key either, use the specified default
					if (value == null) {
						value = key.substring(colon + 1);
					}
				}
			} else {
				// No default, check for a composite key, "key1,key2"
				value = resolveCompositeKey(key, props);
			}
		}
		return value;
	}

	protected static String getExtension(String destFile) {
		int index = destFile.lastIndexOf('.');
		if (index == -1) {
			return "";
		}
		return destFile.substring(index + 1);
	}

	/**
	 * Try to resolve a "key" from the provided properties by checking if it is
	 * actually a "key1,key2", in which case try first "key1", then "key2". If
	 * all fails, return null.
	 * 
	 * It also accepts "key1," and ",key2".
	 * 
	 * @param key
	 *            the key to resolve
	 * @param props
	 *            the properties to use
	 * @return the resolved key or null
	 */
	protected static String resolveCompositeKey(final String key,
			Map<String, String> props) {
		String value = null;

		// Look for the comma
		int comma = key.indexOf(',');
		if (comma > -1) {
			// If we have a first part, try resolve it
			if (comma > 0) {
				// Check the first part
				String key1 = key.substring(0, comma);
				if (props != null) {
					value = props.get(key1);
				} else {
					value = System.getProperty(key1);
				}
			}
			// Check the second part, if there is one and first lookup failed
			if (value == null && comma < key.length() - 1) {
				String key2 = key.substring(comma + 1);
				if (props != null) {
					value = props.get(key2);
				} else {
					value = System.getProperty(key2);
				}
			}
		}
		// Return whatever we've found or null
		return value;
	}
}
