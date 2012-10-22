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
