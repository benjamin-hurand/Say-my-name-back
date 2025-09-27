// src/main/java/com/saymyname/persistence/storage/SmallPhotoStorage.java
package com.saymyname.persistence.storage;

import java.awt.image.BufferedImage;

public interface SmallPhotoStorage {
    /** Écrit/écrase la miniature pour ce key (même nom que l’original). */
    void storeSmall(String originalKey, BufferedImage image);

    /** Existe-t-elle ? */
    boolean exists(String originalKey);

    /** URL publique de la miniature (ex: /photos/small/{key}). */
    String publicSmallUrl(String originalKey);

    /** Supprime silencieusement. */
    void deleteQuietly(String originalKey);
}
