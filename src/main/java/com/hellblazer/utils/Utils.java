/**
 * Copyright (C) 2009 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Stax event driven communications framework.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class Utils {

    public static Object accessField(String fieldName, Object target)
                                                                     throws SecurityException,
                                                                     NoSuchFieldException,
                                                                     IllegalArgumentException,
                                                                     IllegalAccessException {
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
                                     Class<?> targetClass)
                                                          throws SecurityException,
                                                          NoSuchFieldException,
                                                          IllegalArgumentException,
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

    public static void copy(File sourceFile, File destFile) throws IOException {
        InputStream is = new FileInputStream(sourceFile);
        OutputStream os = new FileOutputStream(destFile);
        // Copy the bits from instream to outstream
        copy(is, os);

    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        is.close();
        os.close();
    }

    public static void copyDirectory(File sourceLocation, File targetLocation)
                                                                              throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (String element : children) {
                copyDirectory(new File(sourceLocation, element),
                              new File(targetLocation, element));
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

    public static byte[] getBits(File classFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream fis = new FileInputStream(classFile);
        copy(fis, baos);
        return baos.toByteArray();
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

    public static boolean waitForCondition(int maxWaitTime, Condition condition) {
        return waitForCondition(maxWaitTime, 100, condition);
    }

    public static boolean waitForCondition(int maxWaitTime,
                                           final int sleepTime,
                                           Condition condition) {
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
}
