package com.saymyname.service.profile;

import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.dao.PhotoDao;
import com.saymyname.persistence.storage.PhotoStorage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class PhotoService {

    private final PhotoDao photoDao;
    private final PhotoStorage photoStorage;

    public PhotoService(PhotoDao photoDao, PhotoStorage photoStorage) {
        this.photoDao = photoDao;
        this.photoStorage = photoStorage;
    }

    private static final long MIN_UPLOAD_SIZE = 1 * 1024L; // 1 KB
    private static final long MAX_UPLOAD_SIZE = 5 * 1024 * 1024L; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Transactional
    public Photo replaceForPerson(Long personId, MultipartFile file) {
        if (personId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId manquant");
        if (file == null || file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun fichier reçu");
        if (file.getSize() < MIN_UPLOAD_SIZE)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier trop petit (<50KB)");
        if (file.getSize() > MAX_UPLOAD_SIZE)
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Fichier trop volumineux (>5MB)");
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Type de fichier non supporté");

        // 1) écrire le fichier -> key
        PhotoStorage.StoredFile stored = photoStorage.store(file);

        // 2) rebind DB avec la nouvelle key (DAO pur DB)
        PhotoDao.ReplaceResult result = photoDao.rebindForPerson(personId, stored.key());

        // 3) supprimer l’ancienne clé APRES COMMIT pour éviter les incohérences en
        // rollback
        String oldKey = result.oldStorageKey();
        if (oldKey != null) {
            TransactionSynchronizationManager
                    .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            photoStorage.deleteQuietly(oldKey);
                        }
                    });
        }

        return result.newPhoto();
    }

    @Transactional
    public void deleteForPerson(Long personId) {
        if (personId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "personId manquant");

        String oldKey = photoDao.unlinkForPerson(personId);

        if (oldKey != null) {
            TransactionSynchronizationManager
                    .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            photoStorage.deleteQuietly(oldKey);
                        }
                    });
        }
    }
}
