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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * @author hhildebrand
 *
 */
public class KeystoreTool {

    public static void create(char[] keyStorePassword,
                              KeystoreConfiguration configuration)
                                                                  throws GeneralSecurityException,
                                                                  IOException {
        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(null, keyStorePassword);
        PasswordProtection keyStorePP = new PasswordProtection(keyStorePassword);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        for (Map.Entry<String, String> entry : configuration.keys.entrySet()) {
            SecretKey generatedSecret = factory.generateSecret(new PBEKeySpec(
                                                                              entry.getValue().toCharArray()));
            ks.setEntry(entry.getKey(), new SecretKeyEntry(generatedSecret),
                        keyStorePP);
        }
        try (FileOutputStream os = new FileOutputStream(
                                                        configuration.outputFile)) {
            ks.store(os, keyStorePassword);
        }
    }

    public static char[] getKey(String key, File keyStoreFile,
                                char[] keyStorePassword)
                                                        throws FileNotFoundException,
                                                        IOException,
                                                        GeneralSecurityException {
        try (InputStream is = new FileInputStream(keyStoreFile)) {
            return getKey(key, is, keyStorePassword);
        }
    }

    public static char[] getKey(String key, InputStream is,
                                char[] keyStorePassword)
                                                        throws GeneralSecurityException,
                                                        IOException {
        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(is, keyStorePassword);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        SecretKeyEntry ske = (SecretKeyEntry) ks.getEntry(key,
                                                          new PasswordProtection(
                                                                                 keyStorePassword));
        PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(ske.getSecretKey(),
                                                             PBEKeySpec.class);
        return keySpec.getPassword();
    }
}
