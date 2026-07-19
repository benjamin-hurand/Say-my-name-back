package com.saymyname.service.course;

import org.springframework.stereotype.Service;

import com.saymyname.persistence.dao.course.KnowledgeStatsDao;

@Service
public class KnowledgeStatsService {

    @SuppressWarnings("unused")
    private final KnowledgeStatsDao dao;

    public KnowledgeStatsService(KnowledgeStatsDao dao) {
        this.dao = dao;
    }

}
