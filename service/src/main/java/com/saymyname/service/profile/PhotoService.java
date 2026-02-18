// src/main/java/com/saymyname/service/profile/PhotoService.java
package com.saymyname.service.profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.saymyname.core.exception.common.ForbiddenException;
import com.saymyname.core.exception.common.NotFoundException;
import com.saymyname.core.exception.common.ValidationException;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.dao.PhotoDao;
import com.saymyname.persistence.storage.PhotoStorage;
import com.saymyname.persistence.storage.PhotoStorageReadable;
import com.saymyname.persistence.storage.SmallPhotoStorage;
import com.saymyname.security.CustomUserDetails;
import com.saymyname.security.Roles;
import com.saymyname.service.UserOrganizationService;
import com.saymyname.service.person.PersonService;

@Service
public class PhotoService {

    private final PhotoDao photoDao;
    private final PhotoStorage photoStorage;
    private final SmallPhotoStorage smallPhotoStorage;

    private final PersonService personService;
    private final UserOrganizationService userOrganizationService;

    // Si disponible, permet de lire l’original pour régénérer les miniatures
    private final PhotoStorageReadable readableStorage; // peut être null

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 Mo
    private static final int MIN_WIDTH = 200; // px
    private static final int MIN_HEIGHT = 200; // px

    // Taille cible de la miniature du trombi
    private static final int THUMB_SIZE = 256;

    public PhotoService(
            PhotoDao photoDao,
            PhotoStorage photoStorage,
            PersonService personService,
            UserOrganizationService userOrganizationService,
            SmallPhotoStorage smallPhotoStorage) {

        this.photoDao = photoDao;
        this.photoStorage = photoStorage;
        this.personService = personService;
        this.userOrganizationService = userOrganizationService;
        this.smallPhotoStorage = smallPhotoStorage;
        this.readableStorage = (photoStorage instanceof PhotoStorageReadable psr) ? psr : null;
    }

    /**
     * Upload d’une photo (PENDING) + génération immédiate de la miniature.
     */
    @Transactional
    public Photo submitPhotoForApproval(Long personId, MultipartFile file, CustomUserDetails principal) {
        if (personId == null)
            throw new ValidationException("personId est requis");
        if (principal == null)
            throw new ValidationException("Utilisateur non authentifié");
        if (file == null || file.isEmpty())
            throw new ValidationException("Fichier manquant");

        checkAuthorization(principal, personId);

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("La photo dépasse la taille maximale autorisée (5 Mo)");
        }

        final BufferedImage img;
        try {
            img = ImageIO.read(file.getInputStream());
            if (img == null)
                throw new ValidationException("Le fichier n'est pas une image valide");
            if (img.getWidth() < MIN_WIDTH || img.getHeight() < MIN_HEIGHT) {
                throw new ValidationException(
                        String.format("La photo est trop petite : minimum %dx%d requis", MIN_WIDTH, MIN_HEIGHT));
            }
        } catch (IOException e) {
            throw new ValidationException("Impossible de lire l'image téléchargée", e);
        }

        var stored = photoStorage.store(file);

        try {
            // 1) Génère la miniature (depuis l'image déjà en mémoire)
            BufferedImage thumb = ImageResize.squareThumbnail(img, THUMB_SIZE);
            smallPhotoStorage.storeSmall(stored.key(), thumb);

            // 2) Supprime toute éventuelle PENDING existante pour cette personne
            photoDao.deletePendingByPersonId(personId);

            // 3) Persiste la nouvelle PENDING
            Photo toCreate = Photo.builder()
                    .person(Person.builder().id(personId).build())
                    .storageKey(stored.key())
                    .status(PhotoStatus.PENDING)
                    .submittedBy(User.builder().id(principal.getId()).build())
                    .build();

            return photoDao.save(toCreate);

        } catch (RuntimeException ex) {
            // rollback storage si la suite échoue
            smallPhotoStorage.deleteQuietly(stored.key());
            photoStorage.deleteQuietly(stored.key());
            throw ex;
        }
    }

    /**
     * Tente de (ré)générer la miniature pour la storageKey donnée s’il en manque
     * une.
     *
     * @return true si la miniature existe (déjà ou régénérée), false sinon (ex: pas
     *         de lecture dispo).
     */
    @Transactional(readOnly = true)
    public boolean ensureSmallExists(String storageKey) {
        if (storageKey == null || storageKey.isBlank())
            return false;

        if (smallPhotoStorage.exists(storageKey)) {
            return true;
        }

        // Si on peut lire l’original → on régénère
        if (readableStorage != null && readableStorage.exists(storageKey)) {
            try (InputStream in = readableStorage.open(storageKey)) {
                BufferedImage img = ImageIO.read(in);
                if (img == null)
                    return false;
                BufferedImage thumb = ImageResize.squareThumbnail(img, THUMB_SIZE);
                smallPhotoStorage.storeSmall(storageKey, thumb);
                return true;
            } catch (IOException e) {
                return false;
            } catch (RuntimeException re) {
                return false;
            }
        }

        // Pas de miniature et pas de lecture du storage → impossible ici
        return false;
    }

    private void checkAuthorization(CustomUserDetails principal, Long personId) {
        if (principal.hasRole(Roles.ADMIN)) {
            return;
        }

        // 1) La personne doit exister dans l'orga courante (filtre tenant Hibernate)
        personService.findById(personId)
                .orElseThrow(() -> new NotFoundException("Person introuvable"));

        // 2) Le user connecté doit être lié à CE personId via
        // user_organizations.person_id (orga courante)
        Long myPersonId = userOrganizationService.findPersonIdByUserId(principal.getId())
                .orElseThrow(() -> new ForbiddenException(
                        "Aucun profil (person) n'est lié à votre compte dans cette organisation"));

        if (!Objects.equals(myPersonId, personId)) {
            throw new ForbiddenException("Vous n'avez pas le droit de soumettre une photo pour cette personne");
        }
    }
}
