// src/main/java/com/saymyname/persistence/storage/PhotoStorageReadable.java
package com.saymyname.persistence.storage;

import java.io.InputStream;

public interface PhotoStorageReadable extends PhotoStorage {
    InputStream open(String key);

    boolean exists(String key);
}
