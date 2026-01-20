-- Migration: Add step_state_json column to course_question_attempts
-- Purpose: Store intermediate state for multi-step quiz formats (HANGMAN, WORD_PUZZLE)
-- Author: Claude Code
-- Date: 2026-01-13

-- Add the step_state_json column
ALTER TABLE course_question_attempts
ADD COLUMN step_state_json LONGTEXT NULL
COMMENT 'Multi-step state (Hangman/WordPuzzle): tracks intermediate state between submissions (mask, attempts, feedback, etc.). Deserialized and merged into snapshot on read.';

-- Add index for queries filtering by format with non-null step state
-- (useful for monitoring active multi-step questions)
CREATE INDEX idx_cqh_step_state ON course_question_attempts(question_format, answered_at)
WHERE step_state_json IS NOT NULL;

-- Note: This is a nullable column because:
-- 1. Existing records don't have step state
-- 2. Single-shot formats (MCQ, TEXT_INPUT, etc.) don't need it
-- 3. Multi-step formats only populate it during intermediate steps
