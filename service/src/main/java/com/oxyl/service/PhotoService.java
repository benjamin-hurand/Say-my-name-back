package com.oxyl.service;

import com.oxyl.core.model.game.options.GameOptions;
import com.oxyl.core.model.people.Person;
import com.oxyl.core.model.people.Photo;
import com.oxyl.persistence.dao.PhotoDao;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PhotoService {

    private final PhotoDao photoDao;

    public PhotoService(PhotoDao photoDao) {
        this.photoDao = photoDao;
    }

    public Photo getRandomPhoto() {
        List<Long> ids = photoDao.findAllPhotoIds();
        if (ids.isEmpty()) return null;  // Return null if no photos are available
        Long randomId = ids.get(new Random().nextInt(ids.size()));  // Pick a random ID
        return photoDao.findById(randomId).orElse(null);
    }

    public Photo getNextPhotoWithCriteria(GameOptions gameOptions, List<Long> personIdsHistoric) {
        // Fetch photo IDs based on the gameOptions (sorted or unsorted)
        List<Long> ids = photoDao.findAllPhotoIdsWithCriteria(gameOptions);

        if (ids.isEmpty()) {
            return null;  // Return null if no photos are available
        }

        // Create a mutable copy of the personIdsHistoric to allow modification
        List<Long> filteredIds = new ArrayList<>(ids);

        // Remove already seen personIds from the ids list
        filteredIds.removeAll(personIdsHistoric);

        if (filteredIds.isEmpty()) {
            return null;  // Return null if no photos are available after filtering
        }

        Long nextId;

        // If there are no sorting methods, pick a random ID
        if (gameOptions.getSortBy() == null || gameOptions.getSortBy().isEmpty()) {
            nextId = filteredIds.get(new Random().nextInt(filteredIds.size()));  // Pick a random ID
        } else {
            // With sorting applied, get the first ID in the sorted list
            nextId = filteredIds.getFirst();  // Select the first ID from the sorted list
        }

        return photoDao.findById(nextId).orElse(null);
    }


    public Person findPersonByPhotoId(Long id) {
        return photoDao.findPersonByPhotoId(id);
    }
}
