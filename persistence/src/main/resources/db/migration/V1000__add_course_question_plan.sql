ALTER TABLE course_question_history
    ADD COLUMN planned_format VARCHAR(32) NOT NULL DEFAULT 'TEXT_INPUT',
    ADD COLUMN planned_timed TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN planned_time_limit_ms INT NULL,
    ADD COLUMN planned_target_count INT NOT NULL DEFAULT 1,
    ADD COLUMN planned_target_knowledge_ids_json LONGTEXT NULL,
    ADD COLUMN planned_params_json LONGTEXT NULL,
    ADD COLUMN planned_reason VARCHAR(255) NULL;
