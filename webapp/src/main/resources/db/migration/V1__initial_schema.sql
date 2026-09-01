-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: saymyname
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attribute_enum_options`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attribute_enum_options` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `attribute_id` bigint NOT NULL,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_index` int NOT NULL,
  `active` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_oa_enum_code` (`attribute_id`,`code`),
  UNIQUE KEY `uq_enum_tenant_attr_code` (`tenant_id`,`attribute_id`,`code`),
  KEY `idx_oa_enum_attr` (`attribute_id`),
  KEY `idx_enum_tenant_attr` (`tenant_id`,`attribute_id`),
  CONSTRAINT `fk_enum_attr_tenant` FOREIGN KEY (`tenant_id`, `attribute_id`) REFERENCES `attributes` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attributes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attributes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `concept_id` bigint DEFAULT NULL,
  `attribute_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `display_order` int NOT NULL,
  `is_identity_source` tinyint(1) NOT NULL DEFAULT '0',
  `max_values` int NOT NULL,
  `filter` tinyint(1) NOT NULL,
  `sort` tinyint(1) NOT NULL,
  `required` tinyint(1) NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT',
  `edit_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `casing_strategy` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `constraint_kind` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `constraint_payload` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tenant_attr_name` (`tenant_id`,`attribute_name`),
  UNIQUE KEY `uq_attributes_tenant_id` (`tenant_id`,`id`),
  UNIQUE KEY `uq_attributes_tenant_concept` (`tenant_id`,`concept_id`),
  KEY `idx_attr_tenant` (`tenant_id`),
  KEY `fk_attributes_concept` (`concept_id`),
  CONSTRAINT `fk_attributes_concept` FOREIGN KEY (`concept_id`) REFERENCES `concepts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_attributes_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_attributes_max_values` CHECK ((`max_values` >= 1)),
  CONSTRAINT `chk_org_attributes_casing_strategy` CHECK ((`casing_strategy` in (_utf8mb4'NONE',_utf8mb4'TITLE_CASE',_utf8mb4'UPPERCASE',_utf8mb4'SENTENCE_PRESERVE'))),
  CONSTRAINT `chk_org_attributes_constraint_kind` CHECK ((`constraint_kind` in (_utf8mb4'NONE',_utf8mb4'RANGE',_utf8mb4'REGEX'))),
  CONSTRAINT `chk_org_attributes_edit_policy` CHECK ((`edit_policy` in (_utf8mb4'FREE',_utf8mb4'RESTRICTED',_utf8mb4'DERIVED'))),
  CONSTRAINT `chk_org_attributes_type` CHECK ((`type` in (_utf8mb4'TEXT',_utf8mb4'NUMBER',_utf8mb4'ENUM',_utf8mb4'DATE',_utf8mb4'DATETIME',_utf8mb4'BOOLEAN')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `change_request_items`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `change_request_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `change_request_id` bigint NOT NULL,
  `action` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fact_id` bigint DEFAULT NULL,
  `proposed_value` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `resolution_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `resolution_comment` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cri_request` (`change_request_id`),
  KEY `idx_cri_tenant` (`tenant_id`),
  KEY `idx_cri_fact` (`fact_id`),
  KEY `fk_cri_fact_tenant` (`tenant_id`,`fact_id`),
  CONSTRAINT `fk_cri_change_request` FOREIGN KEY (`change_request_id`) REFERENCES `change_requests` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cri_fact_tenant` FOREIGN KEY (`tenant_id`, `fact_id`) REFERENCES `facts` (`tenant_id`, `id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_cri_tenant_org` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_orgs` (`tenant_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_change_request_items_action` CHECK ((`action` in (_utf8mb4'CREATE',_utf8mb4'UPDATE',_utf8mb4'DELETE'))),
  CONSTRAINT `chk_change_request_items_resolution_status` CHECK ((`resolution_status` in (_utf8mb4'PENDING',_utf8mb4'APPROVED',_utf8mb4'REJECTED',_utf8mb4'CANCELED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `change_requests`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `change_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `person_id` bigint NOT NULL,
  `requester_id` bigint NOT NULL,
  `attribute_id` bigint NOT NULL,
  `request_reason` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `resolved_by` bigint DEFAULT NULL,
  `resolved_at` datetime DEFAULT NULL,
  `resolution_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cr_requester` (`requester_id`),
  KEY `fk_cr_resolved_by` (`resolved_by`),
  KEY `fk_cr_org_attr` (`attribute_id`),
  KEY `idx_cr_tenant_status` (`tenant_id`,`status`,`created_at`),
  KEY `idx_cr_tenant_person` (`tenant_id`,`person_id`),
  KEY `idx_cr_tenant_attr` (`tenant_id`,`attribute_id`),
  CONSTRAINT `fk_cr_attribute_tenant` FOREIGN KEY (`tenant_id`, `attribute_id`) REFERENCES `attributes` (`tenant_id`, `id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_cr_person_tenant` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cr_requester` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_cr_resolved_by` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_cr_tenant_org` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_orgs` (`tenant_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_change_requests_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'APPROVED',_utf8mb4'PARTIALLY_APPROVED',_utf8mb4'REJECTED',_utf8mb4'CANCELED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `concepts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `concepts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `value_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_derived` tinyint(1) NOT NULL DEFAULT '0',
  `portability_kind` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `identity_component_eligible` tinyint(1) NOT NULL DEFAULT '0',
  `required_max_values` int DEFAULT NULL,
  `default_casing_strategy` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_concepts_code` (`code`),
  CONSTRAINT `chk_concepts_default_casing_strategy` CHECK (((`default_casing_strategy` is null) or (`default_casing_strategy` in (_utf8mb4'NONE',_utf8mb4'TITLE_CASE',_utf8mb4'UPPERCASE',_utf8mb4'SENTENCE_PRESERVE')))),
  CONSTRAINT `chk_concepts_portability_kind` CHECK ((`portability_kind` in (_utf8mb4'NONE',_utf8mb4'VALUE_ONLY',_utf8mb4'WITH_CONTEXT'))),
  CONSTRAINT `chk_concepts_required_max_values` CHECK (((`required_max_values` is null) or (`required_max_values` >= 1))),
  CONSTRAINT `chk_concepts_value_type` CHECK ((`value_type` in (_utf8mb4'TEXT',_utf8mb4'ENUM',_utf8mb4'DATE',_utf8mb4'DATETIME',_utf8mb4'NUMBER',_utf8mb4'BOOLEAN')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_question_attempts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_question_attempts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `question_round` int NOT NULL,
  `asked_at` datetime NOT NULL,
  `answered_at` datetime DEFAULT NULL,
  `response_time_ms` int NOT NULL,
  `raw_submission` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `normalized_audit` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `global_correct` tinyint(1) NOT NULL,
  `pool_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `help_used` tinyint(1) NOT NULL,
  `question_format` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `snapshot_schema_version` int NOT NULL,
  `generator_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalizer_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `question_snapshot_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `step_state_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `planned_format` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `planned_timed` tinyint(1) NOT NULL,
  `planned_time_limit_ms` int DEFAULT NULL,
  `planned_target_count` int NOT NULL,
  `planned_target_knowledge_ids_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `planned_params_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `planned_reason_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `planned_reason_details_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cqa_tenant_id` (`tenant_id`,`id`),
  KEY `idx_cqh_course_round` (`course_id`,`question_round`),
  KEY `idx_cqh_course_answered` (`course_id`,`answered_at`),
  KEY `idx_cqh_answered_at` (`answered_at`),
  KEY `idx_cqh_course_correct` (`course_id`,`global_correct`),
  KEY `idx_cqa_tenant_course_round` (`tenant_id`,`course_id`,`question_round`),
  KEY `idx_cqa_tenant_answered` (`tenant_id`,`answered_at`),
  CONSTRAINT `fk_cqa_course_tenant` FOREIGN KEY (`tenant_id`, `course_id`) REFERENCES `courses` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_cqa_planned_format` CHECK ((`planned_format` in (_utf8mb4'AUTO',_utf8mb4'TEXT_INPUT',_utf8mb4'CLOZE',_utf8mb4'HANGMAN',_utf8mb4'WORD_PUZZLE',_utf8mb4'MCQ',_utf8mb4'BINARY_SWIPE',_utf8mb4'ASSOCIATION',_utf8mb4'ORDERING',_utf8mb4'SPEED'))),
  CONSTRAINT `chk_cqa_planned_target_count` CHECK ((`planned_target_count` >= 1)),
  CONSTRAINT `chk_cqa_planned_timed_time_limit` CHECK (((`planned_timed` = 0) or ((`planned_time_limit_ms` is not null) and (`planned_time_limit_ms` >= 1000)))),
  CONSTRAINT `chk_cqa_pool_type` CHECK ((`pool_type` in (_utf8mb4'ERROR_RECENT',_utf8mb4'NEW',_utf8mb4'DISCOVERED',_utf8mb4'SRS_DUE',_utf8mb4'REVISION'))),
  CONSTRAINT `chk_cqa_question_format` CHECK ((`question_format` in (_utf8mb4'TEXT_INPUT',_utf8mb4'CLOZE',_utf8mb4'HANGMAN',_utf8mb4'MCQ',_utf8mb4'BINARY_SWIPE',_utf8mb4'ASSOCIATION',_utf8mb4'ORDERING'))),
  CONSTRAINT `chk_cqa_step_state_json_only_for_multi_step` CHECK (((`step_state_json` is null) or (`question_format` in (_utf8mb4'HANGMAN',_utf8mb4'WORD_PUZZLE'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_question_items`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_question_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `attempt_id` bigint NOT NULL,
  `position` int NOT NULL,
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `knowledge_id` bigint DEFAULT NULL,
  `person_id` bigint DEFAULT NULL,
  `answered` tinyint(1) NOT NULL,
  `correct` tinyint(1) DEFAULT NULL,
  `normalized_answer` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cqi_history_position` (`attempt_id`,`position`),
  KEY `idx_cqi_history` (`attempt_id`),
  KEY `fk_cqi_knowledge` (`knowledge_id`),
  KEY `idx_cqi_tenant_attempt` (`tenant_id`,`attempt_id`),
  KEY `idx_cqi_tenant_person` (`tenant_id`,`person_id`),
  KEY `idx_cqi_tenant_knowledge` (`tenant_id`,`knowledge_id`),
  CONSTRAINT `fk_cqi_attempt_tenant` FOREIGN KEY (`tenant_id`, `attempt_id`) REFERENCES `course_question_attempts` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cqi_knowledge_tenant` FOREIGN KEY (`tenant_id`, `knowledge_id`) REFERENCES `knowledges` (`tenant_id`, `id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_cqi_person_tenant` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_course_question_items_role` CHECK ((`role` in (_utf8mb4'TARGET',_utf8mb4'DISTRACTOR')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_recent_stats`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_recent_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `error_streak` int NOT NULL,
  `help_streak` int NOT NULL,
  `last_format` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `format_streak` int NOT NULL,
  `avg_rt_recent` double NOT NULL,
  `last_answer_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_crs_tenant_course` (`tenant_id`,`course_id`),
  CONSTRAINT `fk_crs_course_tenant` FOREIGN KEY (`tenant_id`, `course_id`) REFERENCES `courses` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_course_recent_stats_last_format` CHECK (((`last_format` is null) or (`last_format` in (_utf8mb4'TEXT_INPUT',_utf8mb4'CLOZE',_utf8mb4'HANGMAN',_utf8mb4'MCQ',_utf8mb4'BINARY_SWIPE',_utf8mb4'ASSOCIATION',_utf8mb4'ORDERING'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `courses`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `target_attribute_id` bigint NOT NULL,
  `status` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `current_round` int NOT NULL,
  `population_scope` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_accessed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_courses_tenant_id` (`tenant_id`,`id`),
  KEY `fk_courses_user` (`user_id`),
  KEY `idx_courses_tenant_user` (`tenant_id`,`user_id`),
  KEY `idx_courses_tenant_status` (`tenant_id`,`status`),
  KEY `idx_courses_tenant_target_attr` (`tenant_id`,`target_attribute_id`),
  KEY `idx_courses_tenant_user_attr` (`tenant_id`,`user_id`,`target_attribute_id`),
  KEY `idx_courses_tenant_user_status` (`tenant_id`,`user_id`,`status`),
  CONSTRAINT `fk_courses_target_attr` FOREIGN KEY (`tenant_id`, `target_attribute_id`) REFERENCES `attributes` (`tenant_id`, `id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_courses_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_courses_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_courses_population_scope` CHECK ((`population_scope` in (_utf8mb4'FOLLOWED',_utf8mb4'ALL'))),
  CONSTRAINT `chk_courses_status` CHECK ((`status` in (_utf8mb4'IN_PROGRESS',_utf8mb4'ARCHIVED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `email_verification_tokens`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_verification_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` binary(16) NOT NULL,
  `user_id` bigint NOT NULL,
  `email` varchar(320) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token_hash` varbinary(32) NOT NULL,
  `code_hash_phc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `purpose` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `make_primary_now` tinyint(1) NOT NULL,
  `attempts` int NOT NULL,
  `resend_count` int NOT NULL,
  `expires_at` datetime NOT NULL,
  `consumed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_sent_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_evt_token` (`token_hash`),
  UNIQUE KEY `uq_evt_public_id` (`public_id`),
  KEY `ix_evt_user_email` (`user_id`,`email`),
  KEY `ix_evt_expires` (`expires_at`,`consumed_at`),
  KEY `ix_evt_last_sent` (`last_sent_at`),
  CONSTRAINT `fk_evt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_email_verification_tokens_purpose` CHECK ((`purpose` in (_utf8mb4'ADD_EMAIL',_utf8mb4'REGISTER_EMAIL')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `facts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `facts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `scope_kind` enum('TENANT','WORKSPACE','TEAM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `workspace_id` bigint DEFAULT NULL,
  `team_id` bigint DEFAULT NULL,
  `person_id` bigint NOT NULL,
  `attribute_id` bigint NOT NULL,
  `value` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `valid_from` datetime NOT NULL,
  `valid_to` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_facts_tenant_id` (`tenant_id`,`id`),
  KEY `idx_facts_tenant` (`tenant_id`),
  KEY `idx_facts_tenant_person_attr` (`tenant_id`,`person_id`,`attribute_id`),
  KEY `idx_facts_tenant_attr` (`tenant_id`,`attribute_id`),
  KEY `idx_facts_workspace` (`workspace_id`),
  KEY `idx_facts_team` (`team_id`),
  KEY `idx_facts_tenant_workspace` (`tenant_id`,`workspace_id`),
  KEY `idx_facts_ws_team` (`workspace_id`,`team_id`),
  CONSTRAINT `fk_facts_attribute` FOREIGN KEY (`tenant_id`, `attribute_id`) REFERENCES `attributes` (`tenant_id`, `id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_facts_person` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_facts_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_facts_team_ws` FOREIGN KEY (`workspace_id`, `team_id`) REFERENCES `teams` (`workspace_id`, `id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_facts_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_facts_workspace` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_facts_workspace_tenant` FOREIGN KEY (`tenant_id`, `workspace_id`) REFERENCES `workspaces` (`tenant_id`, `id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_facts_scope_consistency` CHECK ((((`scope_kind` = _utf8mb4'TENANT') and (`workspace_id` is null) and (`team_id` is null)) or ((`scope_kind` = _utf8mb4'WORKSPACE') and (`workspace_id` is not null) and (`team_id` is null)) or ((`scope_kind` = _utf8mb4'TEAM') and (`team_id` is not null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `import_batches`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import_batches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_kind` enum('CSV','XLSX','HTML','API','MANUAL') NOT NULL,
  `source_label` varchar(255) DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `status` enum('PENDING','MAPPING','REVIEW','APPLIED','FAILED') NOT NULL DEFAULT 'PENDING',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `applied_at` datetime DEFAULT NULL,
  `workspace_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_imp_user` (`created_by`),
  KEY `idx_imp_ws_status` (`workspace_id`,`status`,`created_at`),
  CONSTRAINT `fk_imp_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_imp_workspace` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `import_mappings`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import_mappings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` bigint NOT NULL,
  `source_key` varchar(255) NOT NULL,
  `target_attr_id` bigint DEFAULT NULL,
  `transform_kind` enum('NONE','SPLIT_NAME','REGEX','DATE_PARSE','LOWER','UPPER','TRIM','ENUM_MAP') NOT NULL DEFAULT 'NONE',
  `transform_payload` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_imp_map_batch` (`batch_id`),
  CONSTRAINT `fk_imp_map_batch` FOREIGN KEY (`batch_id`) REFERENCES `import_batches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `import_rows`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import_rows` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` bigint NOT NULL,
  `raw_payload` json NOT NULL,
  `normalized` json DEFAULT NULL,
  `status` enum('NEW','MAPPED','CONFLICT','READY','SKIPPED','ERROR') NOT NULL DEFAULT 'NEW',
  `error_message` text,
  PRIMARY KEY (`id`),
  KEY `fk_imp_rows_batch` (`batch_id`),
  CONSTRAINT `fk_imp_rows_batch` FOREIGN KEY (`batch_id`) REFERENCES `import_batches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invitation_usages`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invitation_usages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `invitation_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `person_id` bigint DEFAULT NULL,
  `used_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `used_ip` varbinary(16) DEFAULT NULL,
  `user_agent` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_usage_user` (`user_id`),
  KEY `idx_usage_tenant` (`tenant_id`),
  KEY `fk_usage_invitation` (`invitation_id`),
  CONSTRAINT `fk_usage_invitation` FOREIGN KEY (`invitation_id`) REFERENCES `invitations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usage_tenant_org` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_orgs` (`tenant_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usage_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invitations`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invitations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `label` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `constraints_json` json DEFAULT NULL,
  `role` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(320) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `person_id` bigint DEFAULT NULL,
  `token_hash` varbinary(32) NOT NULL,
  `pin_hash_phc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `max_uses` int DEFAULT NULL,
  `uses_count` int NOT NULL,
  `expires_at` datetime NOT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `accepted_by` bigint DEFAULT NULL,
  `accepted_at` datetime DEFAULT NULL,
  `last_used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_invit_token` (`token_hash`),
  KEY `fk_invit_accepted_by` (`accepted_by`),
  KEY `fk_invit_created_by` (`created_by`),
  KEY `idx_invit_tenant` (`tenant_id`),
  CONSTRAINT `fk_invit_accepted_by` FOREIGN KEY (`accepted_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_invit_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_invit_tenant_org` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_orgs` (`tenant_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_invitations_role` CHECK ((`role` in (_utf8mb4'VIEWER',_utf8mb4'EDITOR',_utf8mb4'ADMIN',_utf8mb4'OWNER'))),
  CONSTRAINT `chk_invitations_type` CHECK ((`type` in (_utf8mb4'EMAIL',_utf8mb4'SELF_SERVICE')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `knowledge_stats`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `fact_id` bigint NOT NULL,
  `knowledge_id` bigint NOT NULL,
  `attempts_recent` double NOT NULL,
  `correct_recent` double NOT NULL,
  `help_recent` double NOT NULL,
  `avg_rt_recent` double NOT NULL,
  `last_answer_at` datetime DEFAULT NULL,
  `last_correct` tinyint(1) DEFAULT NULL,
  `last_help_used` tinyint(1) DEFAULT NULL,
  `last_response_time_ms` int NOT NULL,
  `error_streak` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ks_tenant_knowledge` (`tenant_id`,`knowledge_id`),
  KEY `fk_ks_user` (`user_id`),
  KEY `fk_ks_fact` (`fact_id`),
  KEY `fk_ks_knowledge` (`knowledge_id`),
  KEY `idx_ks_select` (`tenant_id`,`user_id`,`fact_id`,`error_streak`,`avg_rt_recent`,`last_answer_at`),
  KEY `idx_ks_fact_tenant` (`tenant_id`,`fact_id`),
  KEY `idx_ks_knowledge_tenant` (`tenant_id`,`knowledge_id`),
  CONSTRAINT `fk_ks_fact_tenant` FOREIGN KEY (`tenant_id`, `fact_id`) REFERENCES `facts` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ks_knowledge_tenant` FOREIGN KEY (`tenant_id`, `knowledge_id`) REFERENCES `knowledges` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ks_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ks_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `knowledges`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `fact_id` bigint NOT NULL,
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `next_review_date` datetime NOT NULL,
  `last_review_date` datetime DEFAULT NULL,
  `total_repetition_count` int NOT NULL,
  `failure_count` int NOT NULL,
  `success_count` int NOT NULL,
  `srs_streak` int NOT NULL,
  `global_streak` int NOT NULL,
  `ease_factor` decimal(10,2) NOT NULL,
  `difficulty` double NOT NULL,
  `stability` double NOT NULL,
  `pending_revalidation` tinyint(1) NOT NULL DEFAULT '0',
  `revalidation_reason` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_knowledges_tenant_id` (`tenant_id`,`id`),
  UNIQUE KEY `uq_k_user_fact` (`tenant_id`,`user_id`,`fact_id`),
  KEY `fk_k_user` (`user_id`),
  KEY `idx_k_select` (`tenant_id`,`user_id`,`status`,`next_review_date`),
  KEY `fk_k_fact_tenant` (`tenant_id`,`fact_id`),
  KEY `idx_k_reval` (`tenant_id`,`user_id`,`pending_revalidation`,`next_review_date`),
  CONSTRAINT `fk_k_fact_tenant` FOREIGN KEY (`tenant_id`, `fact_id`) REFERENCES `facts` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_k_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_k_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_knowledges_status` CHECK ((`status` in (_utf8mb4'UNKNOWN',_utf8mb4'DISCOVERED',_utf8mb4'LEARNED',_utf8mb4'MASTERED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `leaderboard_stats`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leaderboard_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `xp` bigint NOT NULL,
  `total_answers` bigint NOT NULL,
  `correct_answers` bigint NOT NULL,
  `last_answer_at` datetime DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lb_tenant_user` (`tenant_id`,`user_id`),
  KEY `fk_lb_user` (`user_id`),
  KEY `idx_lb_tenant_xp` (`tenant_id`,`xp`),
  CONSTRAINT `fk_lb_tenant_org` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_orgs` (`tenant_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lb_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `org_policies`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `org_policies` (
  `tenant_id` bigint NOT NULL,
  `require_sso` tinyint(1) NOT NULL DEFAULT '0',
  `external_needs_approval` tinyint(1) NOT NULL DEFAULT '0',
  `link_default_max_uses` int NOT NULL DEFAULT '25',
  `link_default_ttl_days` int NOT NULL DEFAULT '14',
  `allowed_domains_json` json DEFAULT NULL,
  `blocked_domains_json` json DEFAULT NULL,
  PRIMARY KEY (`tenant_id`),
  CONSTRAINT `fk_policy_tenant_org` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_orgs` (`tenant_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `password_tokens`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token_hash` varchar(44) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime NOT NULL,
  `used_at` datetime DEFAULT NULL,
  `created_ip` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_agent` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_password_tokens_user` (`user_id`),
  KEY `idx_password_tokens_expires` (`expires_at`,`used_at`),
  CONSTRAINT `fk_password_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person_emails`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `person_emails` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `person_id` bigint NOT NULL,
  `email` varchar(320) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `kind` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_kind` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_primary` tinyint(1) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `verified_at` datetime DEFAULT NULL,
  `bounced_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pe_person_email` (`person_id`,`email`),
  KEY `idx_pe_tenant_person` (`tenant_id`,`person_id`),
  CONSTRAINT `fk_pe_person_tenant` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pe_tenant_org` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_orgs` (`tenant_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_person_emails_kind` CHECK ((`kind` in (_utf8mb4'WORK',_utf8mb4'PERSONAL',_utf8mb4'OTHER'))),
  CONSTRAINT `chk_person_emails_source_kind` CHECK ((`source_kind` in (_utf8mb4'IMPORT',_utf8mb4'MANUAL',_utf8mb4'SYNC')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person_source_contexts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `person_source_contexts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `person_source_id` bigint NOT NULL,
  `context_kind` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  `workspace_id` bigint DEFAULT NULL,
  `team_id` bigint DEFAULT NULL,
  `role_label` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `team_name_snapshot` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `workspace_name_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `organization_name_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payload_json` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_psc_source` (`person_source_id`),
  KEY `idx_psc_ws_team` (`workspace_id`,`team_id`),
  KEY `fk_psc_team` (`team_id`),
  CONSTRAINT `fk_psc_source` FOREIGN KEY (`person_source_id`) REFERENCES `person_sources` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_psc_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_psc_workspace` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person_sources`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `person_sources` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `person_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `source_kind` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_person_id` bigint DEFAULT NULL,
  `source_snapshot_json` json DEFAULT NULL,
  `source_snapshot_hash` varbinary(32) DEFAULT NULL,
  `copied_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_seen_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ps_person_source` (`person_id`,`source_kind`,`source_person_id`),
  KEY `idx_ps_person` (`person_id`),
  KEY `fk_ps_tenant` (`tenant_id`),
  CONSTRAINT `fk_ps_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_personals` (`tenant_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `persons`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `persons` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_persons_tenant_id` (`tenant_id`,`id`),
  KEY `idx_persons_tenant` (`tenant_id`),
  CONSTRAINT `fk_persons_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `photo_assignments`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `person_id` bigint NOT NULL,
  `scope_kind` enum('TENANT','WORKSPACE','TEAM') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `workspace_id` bigint DEFAULT NULL,
  `team_id` bigint DEFAULT NULL,
  `photo_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pa_scope` (`tenant_id`,`person_id`,`scope_kind`,`workspace_id`,`team_id`),
  KEY `idx_pa_tenant_person` (`tenant_id`,`person_id`),
  KEY `idx_pa_workspace` (`workspace_id`),
  KEY `idx_pa_team` (`team_id`),
  KEY `idx_pa_photo` (`photo_id`),
  KEY `idx_pa_tenant_photo` (`tenant_id`,`photo_id`),
  CONSTRAINT `fk_pa_person_tenant` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pa_photo_tenant` FOREIGN KEY (`tenant_id`, `photo_id`) REFERENCES `photos` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pa_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_pa_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pa_workspace` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `photo_reports`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `person_id` bigint NOT NULL,
  `reported_by` bigint NOT NULL,
  `reason_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reason_text` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pr_person_created` (`person_id`,`created_at`),
  KEY `idx_pr_reported_by_created` (`reported_by`,`created_at`),
  KEY `idx_pr_reason_created` (`reason_type`,`created_at`),
  KEY `idx_pr_tenant` (`tenant_id`),
  KEY `idx_pr_tenant_person` (`tenant_id`,`person_id`),
  CONSTRAINT `fk_pr_person_tenant` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pr_reported_by` FOREIGN KEY (`reported_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_pr_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pr_tenant_org` FOREIGN KEY (`tenant_id`) REFERENCES `tenant_orgs` (`tenant_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_photo_reports_reason_type` CHECK ((`reason_type` in (_utf8mb4'NOT_REPRESENTATIVE',_utf8mb4'BLURRY',_utf8mb4'FACE_NOT_VISIBLE',_utf8mb4'INAPPROPRIATE',_utf8mb4'PRIVACY',_utf8mb4'OTHER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `photos`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `storage_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `person_id` bigint NOT NULL,
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `submitted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `submitted_by` bigint DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `approved_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_photos_tenant_id` (`tenant_id`,`id`),
  KEY `idx_photos_person` (`person_id`),
  KEY `idx_photos_status` (`status`),
  KEY `idx_photos_approved_by` (`approved_by`),
  KEY `fk_photos_submitted_by` (`submitted_by`),
  KEY `idx_photos_tenant_person` (`tenant_id`,`person_id`),
  CONSTRAINT `fk_photos_approved_by` FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_photos_person_tenant` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_photos_submitted_by` FOREIGN KEY (`submitted_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_photos_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'APPROVED',_utf8mb4'REJECTED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `team_persons`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_persons` (
  `team_id` bigint NOT NULL,
  `workspace_id` bigint NOT NULL,
  `person_id` bigint NOT NULL,
  `role_label` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`team_id`,`person_id`),
  KEY `idx_tp_ws_person` (`workspace_id`,`person_id`),
  KEY `fk_tp_team_ws` (`workspace_id`,`team_id`),
  CONSTRAINT `fk_tp_team_ws` FOREIGN KEY (`workspace_id`, `team_id`) REFERENCES `teams` (`workspace_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teams`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teams` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `workspace_id` bigint NOT NULL,
  `parent_team_id` bigint DEFAULT NULL,
  `name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `display_order` int NOT NULL DEFAULT '0',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_team_ws_name` (`workspace_id`,`name`),
  UNIQUE KEY `uq_teams_ws_id` (`workspace_id`,`id`),
  KEY `idx_team_ws_parent` (`workspace_id`,`parent_team_id`),
  KEY `fk_team_parent` (`parent_team_id`),
  CONSTRAINT `fk_team_parent_ws` FOREIGN KEY (`workspace_id`, `parent_team_id`) REFERENCES `teams` (`workspace_id`, `id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_team_ws` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_memberships`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_memberships` (
  `user_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `display_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `person_id` bigint DEFAULT NULL,
  `person_link_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `can_pick_person` tinyint(1) NOT NULL,
  `can_create_person` tinyint(1) NOT NULL,
  `pick_requires_approval` tinyint(1) NOT NULL,
  `create_requires_approval` tinyint(1) NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `preferred_email_id` bigint DEFAULT NULL,
  PRIMARY KEY (`tenant_id`,`user_id`),
  KEY `idx_tm_user_id` (`user_id`),
  KEY `idx_tm_tenant_status_role` (`tenant_id`,`status`,`role`),
  KEY `fk_tm_pref_email` (`preferred_email_id`),
  CONSTRAINT `fk_tm_pref_email` FOREIGN KEY (`preferred_email_id`) REFERENCES `user_emails` (`id`),
  CONSTRAINT `fk_tm_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tm_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_tm_person_link_status` CHECK ((`person_link_status` in (_utf8mb4'NONE',_utf8mb4'PENDING',_utf8mb4'APPROVED',_utf8mb4'REJECTED'))),
  CONSTRAINT `chk_tm_role` CHECK ((`role` in (_utf8mb4'VIEWER',_utf8mb4'EDITOR',_utf8mb4'ADMIN',_utf8mb4'OWNER'))),
  CONSTRAINT `chk_tm_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'ACTIVE',_utf8mb4'SUSPENDED',_utf8mb4'LEFT')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_orgs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_orgs` (
  `tenant_id` bigint NOT NULL,
  `org_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `org_type` enum('EPHEMERAL','LONG_TERM','PUBLIC') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LONG_TERM',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`tenant_id`),
  CONSTRAINT `fk_tenant_org_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenant_personals`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_personals` (
  `tenant_id` bigint NOT NULL,
  `owner_user_id` bigint NOT NULL,
  PRIMARY KEY (`tenant_id`),
  UNIQUE KEY `uk_tenant_personal_owner` (`owner_user_id`),
  CONSTRAINT `fk_tenant_personal_owner` FOREIGN KEY (`owner_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tenant_personal_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tenants`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `kind` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenants_kind` (`kind`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_emails`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_emails` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `email` varchar(320) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_primary` tinyint(1) NOT NULL,
  `is_login_allowed` tinyint(1) NOT NULL,
  `is_recovery_allowed` tinyint(1) NOT NULL,
  `verified_at` datetime DEFAULT NULL,
  `added_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `recovery_eligible_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_emails_email` (`email`),
  KEY `ix_user_emails_user` (`user_id`),
  CONSTRAINT `fk_user_emails_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_identities`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_identities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `provider` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_subject` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `last_used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ui_user_provider` (`user_id`,`provider`),
  UNIQUE KEY `uq_ui_provider_subject` (`provider`,`provider_subject`),
  KEY `ix_ui_user` (`user_id`),
  KEY `ix_ui_provider` (`provider`),
  KEY `ix_ui_provider_subject` (`provider`,`provider_subject`),
  CONSTRAINT `fk_ui_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_user_identities_provider` CHECK ((`provider` in (_utf8mb4'LOCAL',_utf8mb4'GOOGLE',_utf8mb4'APPLE',_utf8mb4'GITHUB',_utf8mb4'OIDC')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_refresh_tokens`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token_hash` binary(32) NOT NULL,
  `family_id` binary(16) NOT NULL,
  `replaced_by_token_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `expires_at` datetime NOT NULL,
  `last_used_at` datetime DEFAULT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `revoke_reason` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `device_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `device_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ip_created` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ip_last_used` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_agent` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_urt_token_id` (`token_id`),
  UNIQUE KEY `uq_urt_token_hash` (`token_hash`),
  KEY `ix_urt_user` (`user_id`),
  KEY `ix_urt_user_expires` (`user_id`,`expires_at`),
  KEY `ix_urt_family` (`family_id`),
  KEY `ix_urt_user_device` (`user_id`,`device_id`),
  CONSTRAINT `fk_urt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_subscriptions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `person_id` bigint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_us_tenant_user_person` (`tenant_id`,`user_id`,`person_id`),
  KEY `idx_us_tenant_person` (`tenant_id`,`person_id`),
  KEY `fk_usn_user` (`user_id`),
  CONSTRAINT `fk_usn_person` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_usn_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usn_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` binary(16) NOT NULL,
  `display_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `srs_algorithm` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SM2',
  `roles` varchar(255) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `auth_version` int NOT NULL DEFAULT '0',
  `auth_updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_public_id` (`public_id`),
  KEY `ix_users_display_name` (`display_name`),
  CONSTRAINT `chk_users_srs_algorithm` CHECK ((`srs_algorithm` in (_utf8mb4'SM2',_utf8mb4'PFA',_utf8mb4'FSRS')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `workspace_attributes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workspace_attributes` (
  `workspace_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `attribute_id` bigint NOT NULL,
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1',
  `display_order_override` int DEFAULT NULL,
  `filter_override` tinyint(1) DEFAULT NULL,
  `sort_override` tinyint(1) DEFAULT NULL,
  `required_override` tinyint(1) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`workspace_id`,`attribute_id`),
  KEY `idx_wa_ws` (`workspace_id`),
  KEY `idx_wa_tenant_attr` (`tenant_id`,`attribute_id`),
  CONSTRAINT `fk_wa_attr_tenant` FOREIGN KEY (`tenant_id`, `attribute_id`) REFERENCES `attributes` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wa_workspace` FOREIGN KEY (`workspace_id`) REFERENCES `workspaces` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `workspace_members`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workspace_members` (
  `workspace_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `display_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `person_id` bigint DEFAULT NULL,
  `person_link_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NONE',
  `can_pick_person` tinyint(1) NOT NULL DEFAULT '0',
  `can_create_person` tinyint(1) NOT NULL DEFAULT '0',
  `pick_requires_approval` tinyint(1) NOT NULL DEFAULT '0',
  `create_requires_approval` tinyint(1) NOT NULL DEFAULT '0',
  `preferred_email_id` bigint DEFAULT NULL,
  PRIMARY KEY (`workspace_id`,`user_id`),
  UNIQUE KEY `uq_wm_workspace_person` (`workspace_id`,`person_id`),
  KEY `ix_wm_ws_display_name` (`workspace_id`,`display_name`),
  KEY `ix_wm_ws_role` (`workspace_id`,`role`),
  KEY `ix_wm_ws_status` (`workspace_id`,`status`),
  KEY `ix_wm_pref_email` (`preferred_email_id`),
  KEY `idx_wm_tenant_ws_status` (`tenant_id`,`workspace_id`,`status`),
  KEY `idx_wm_tenant_user` (`tenant_id`,`user_id`),
  KEY `fk_wm_person_tenant` (`tenant_id`,`person_id`),
  KEY `idx_wm_user` (`user_id`),
  CONSTRAINT `fk_wm_person_tenant` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_wm_pref_email` FOREIGN KEY (`preferred_email_id`) REFERENCES `user_emails` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_wm_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wm_user_tenant` FOREIGN KEY (`tenant_id`, `user_id`) REFERENCES `tenant_memberships` (`tenant_id`, `user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wm_workspace_tenant` FOREIGN KEY (`tenant_id`, `workspace_id`) REFERENCES `workspaces` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_wm_person_link_status` CHECK ((`person_link_status` in (_utf8mb4'NONE',_utf8mb4'PENDING',_utf8mb4'APPROVED',_utf8mb4'REJECTED'))),
  CONSTRAINT `chk_wm_role` CHECK ((`role` in (_utf8mb4'VIEWER',_utf8mb4'EDITOR',_utf8mb4'ADMIN',_utf8mb4'OWNER'))),
  CONSTRAINT `chk_wm_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'ACTIVE',_utf8mb4'SUSPENDED',_utf8mb4'LEFT')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `workspace_persons`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workspace_persons` (
  `workspace_id` bigint NOT NULL,
  `person_id` bigint NOT NULL,
  `tenant_id` bigint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `added_by` bigint DEFAULT NULL,
  PRIMARY KEY (`workspace_id`,`person_id`),
  KEY `idx_wp_person` (`person_id`),
  KEY `idx_wp_workspace` (`workspace_id`),
  KEY `fk_wp_added_by` (`added_by`),
  KEY `idx_wp_tenant_person` (`tenant_id`,`person_id`),
  KEY `idx_wp_tenant_ws` (`tenant_id`,`workspace_id`),
  CONSTRAINT `fk_wp_added_by` FOREIGN KEY (`added_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_wp_person_tenant` FOREIGN KEY (`tenant_id`, `person_id`) REFERENCES `persons` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wp_workspace_tenant` FOREIGN KEY (`tenant_id`, `workspace_id`) REFERENCES `workspaces` (`tenant_id`, `id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `workspaces`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workspaces` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ws_tenant_name` (`tenant_id`,`name`),
  KEY `idx_ws_tenant` (`tenant_id`),
  CONSTRAINT `fk_ws_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xp_events`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xp_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `event_id` binary(16) NOT NULL,
  `event_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_id` bigint DEFAULT NULL,
  `delta_xp` int NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_xp_event_source` (`source_type`,`source_id`),
  KEY `idx_xp_event_key` (`event_key`),
  KEY `fk_xp_user` (`user_id`),
  KEY `idx_xp_tenant_user_created` (`tenant_id`,`user_id`,`created_at`,`id`),
  KEY `idx_xp_tenant_created` (`tenant_id`,`created_at`,`id`),
  CONSTRAINT `fk_xp_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_xp_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'saymyname'
--


-- Restore session settings
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
