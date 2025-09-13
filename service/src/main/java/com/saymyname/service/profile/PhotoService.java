// src/main/java/com/saymyname/service/profile/PhotoService.java
package com.saymyname.service.profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
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
import com.saymyname.security.CustomUserDetails;
import com.saymyname.security.Roles;
import com.saymyname.service.PersonService;

@Service
public class PhotoService {

    private final PhotoDao photoDao;
    private final PhotoStorage photoStorage;
    private final PersonService personService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 Mo
    private static final int MIN_WIDTH = 200; // px
    private static final int MIN_HEIGHT = 200; // px

    public PhotoService(PhotoDao photoDao, PhotoStorage photoStorage, PersonService personService) {
        this.photoDao = photoDao;
        this.photoStorage = photoStorage;
        this.personService = personService;
    }

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

        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) {
                throw new ValidationException("Le fichier n'est pas une image valide");
            }
            if (img.getWidth() < MIN_WIDTH || img.getHeight() < MIN_HEIGHT) {
                throw new ValidationException(
                        String.format("La photo est trop petite : minimum %dx%d requis", MIN_WIDTH, MIN_HEIGHT));
            }
        } catch (IOException e) {
            throw new ValidationException("Impossible de lire l'image téléchargée", e);
        }

        var stored = photoStorage.store(file);

        try {
            photoDao.deletePendingByPersonId(personId);

            Photo toCreate = new Photo.Builder()
                    .withPerson(new Person.Builder().withId(personId).build())
                    .withStorageKey(stored.key())
                    .withStatus(PhotoStatus.PENDING)
                    .withSubmittedBy(new User.Builder().withId(principal.getId()).build())
                    .build();

            return photoDao.save(toCreate);

        } catch (RuntimeException ex) {
            photoStorage.deleteQuietly(stored.key());
            throw ex;
        }
    }

    private void checkAuthorization(CustomUserDetails principal, Long personId) {
        if (principal.hasRole(Roles.ADMIN))
            return;

        Person person = personService.findById(personId)
                .orElseThrow(() -> new NotFoundException("Person introuvable"));
        if (!Objects.equals(person.getUser().getId(), principal.getId())) {
            throw new ForbiddenException("Vous n'avez pas le droit de soumettre une photo pour cette personne");
        }
    }
}
