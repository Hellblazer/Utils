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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author hhildebrand
 * 
 */
public class TestPropertyReplace {
    @Test
    public void testreplaceProperties() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("test.1", "AAAAAAAAA");
        properties.put("test.2", "BBBBBBBBB");
        properties.put("test.3", "CCCCCCCCC");

        File temp = File.createTempFile("proptest", ".txt");
        temp.deleteOnExit();
        InputStream in = getClass().getResourceAsStream("test.txt");
        OutputStream out = new FileOutputStream(temp);
        Utils.replaceProperties(in, out, properties);

        in.close();
        out.close();

        FileInputStream resultStream = new FileInputStream(temp);
        InputStream expectedStream = getClass().getResourceAsStream("expected.txt");
        String expectedDocument = Utils.getString(expectedStream);
        String resultDocument = Utils.getString(resultStream);
        assertEquals(expectedDocument, resultDocument);

    }
}
