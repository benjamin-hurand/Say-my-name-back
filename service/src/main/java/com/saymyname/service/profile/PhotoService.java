package com.saymyname.service.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.dao.PhotoDao;
import com.saymyname.persistence.storage.PhotoStorage;
import com.saymyname.security.CustomUserDetails;
import com.saymyname.security.Roles;
import com.saymyname.service.PersonService;
import com.saymyname.service.UserService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Service
public class PhotoService {

    private final PhotoDao photoDao;
    private final PhotoStorage photoStorage;
    private final UserService userService;
    private final PersonService personService;

    // Règles de validation (constants)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 Mo
    private static final int MIN_WIDTH = 200; // px
    private static final int MIN_HEIGHT = 200; // px

    public PhotoService(PhotoDao photoDao, PhotoStorage photoStorage, UserService userService,
            PersonService personService) {
        this.photoDao = photoDao;
        this.photoStorage = photoStorage;
        this.userService = userService;
        this.personService = personService;
    }

    @Transactional
    public Photo submitPhotoForApproval(Long personId, MultipartFile file, CustomUserDetails principal) {
        if (personId == null)
            throw new IllegalArgumentException("personId est requis");
        if (principal == null)
            throw new IllegalArgumentException("Utilisateur non authentifié");
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Fichier manquant");

        // Vérification droits (via roles + ownership)
        checkAuthorization(principal, personId);

        // --- Validation de taille ---
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La photo dépasse la taille maximale autorisée (5 Mo)");
        }

        // --- Validation des dimensions et du format réel ---
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) {
                throw new IllegalArgumentException("Le fichier n'est pas une image valide");
            }
            if (img.getWidth() < MIN_WIDTH || img.getHeight() < MIN_HEIGHT) {
                throw new IllegalArgumentException(
                        String.format("La photo est trop petite : minimum %dx%d requis", MIN_WIDTH, MIN_HEIGHT));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Impossible de lire l'image téléchargée", e);
        }

        // 1) Stocker le fichier et obtenir la storageKey
        var stored = photoStorage.store(file);

        try {
            // 2) Remplacer l’éventuelle PENDING existante
            photoDao.deletePendingByPersonId(personId);

            // 3) Créer la nouvelle PENDING
            Photo toCreate = new Photo.Builder()
                    .withPerson(new Person.Builder().withId(personId).build())
                    .withStorageKey(stored.key())
                    .withStatus(PhotoStatus.PENDING)
                    .withSubmittedBy(new User.Builder().withId(principal.getId()).build())
                    .build();

            // 4) Sauvegarder et retourner le modèle persistant
            return photoDao.save(toCreate);

        } catch (RuntimeException ex) {
            // Compensation si la persistance échoue après stockage
            photoStorage.deleteQuietly(stored.key());
            throw ex;
        }
    }

    private void checkAuthorization(CustomUserDetails principal, Long personId) {
        // 1) Vérifier si ADMIN
        if (principal.hasRole(Roles.ADMIN)) {
            return;
        }

        // 2) Vérifier propriétaire de la Person
        Person person = personService.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Person introuvable"));
        if (!Objects.equals(person.getUser().getId(), principal.getId())) {
            throw new SecurityException("Vous n'avez pas le droit de soumettre une photo pour cette personne");
        }
    }

}
