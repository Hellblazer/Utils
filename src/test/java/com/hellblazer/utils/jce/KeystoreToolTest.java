/** (C) Copyright 2014 Chiral Behaviors, All Rights Reserved
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

package com.hellblazer.utils.jce;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author hhildebrand
 *
 */
public class KeystoreToolTest {

    private static final String KLATUU_BARADA_NIKTO = "Klatuu Barada Nikto";
    private static final String NECRONOMICON        = "Necronomicon";
    private static final String WHAT_D_YOU_SAY      = "What'd you say?";
    private static final String HELLO_WORLD         = "Hello World";

    @Test
    public void testPasswords() throws Exception {
        KeystoreConfiguration configuration = new KeystoreConfiguration();
        char[] keyStorePassword = "Of course".toCharArray();
        configuration.keys.put(HELLO_WORLD, WHAT_D_YOU_SAY);
        configuration.keys.put(NECRONOMICON, KLATUU_BARADA_NIKTO);
        configuration.outputFile = File.createTempFile("keystore-", ".keys");
        configuration.outputFile.deleteOnExit();
        KeystoreTool.create(keyStorePassword, configuration);
        assertEquals(WHAT_D_YOU_SAY,
                     new String(KeystoreTool.getKey(HELLO_WORLD,
                                                    configuration.outputFile,
                                                    keyStorePassword)));
        assertEquals(KLATUU_BARADA_NIKTO,
                     new String(KeystoreTool.getKey(NECRONOMICON,
                                                    configuration.outputFile,
                                                    keyStorePassword)));
    }
}
