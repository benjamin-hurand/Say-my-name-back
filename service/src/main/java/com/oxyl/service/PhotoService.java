package com.oxyl.service;

import com.oxyl.core.model.Photo;
import com.oxyl.persistence.dao.PhotoDao;
import org.springframework.stereotype.Service;

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
}
