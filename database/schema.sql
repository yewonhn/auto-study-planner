-- FocusGrow Database Schema
-- Target DBMS: MySQL 8.x
-- Reference: docs/API_DB_SPEC.md

CREATE DATABASE IF NOT EXISTS focusgrow
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE focusgrow;

-- 기존 테이블과의 의존성을 고려하여 삭제 순서 지정
DROP TABLE IF EXISTS RECORD;
DROP TABLE IF EXISTS SCHEDULE;
DROP TABLE IF EXISTS CHARACTER;
DROP TABLE IF EXISTS GOAL;
DROP TABLE IF EXISTS USER;

-- 사용자
CREATE TABLE USER (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 목표
CREATE TABLE GOAL (
    goal_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_amount INT NOT NULL,
    unit VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (goal_id),
    KEY idx_goal_user_id (user_id),

    CONSTRAINT fk_goal_user
        FOREIGN KEY (user_id)
        REFERENCES USER (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_goal_total_amount
        CHECK (total_amount > 0),

    CONSTRAINT chk_goal_date
        CHECK (end_date >= start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 일일 일정
CREATE TABLE SCHEDULE (
    schedule_id BIGINT NOT NULL AUTO_INCREMENT,
    goal_id BIGINT NOT NULL,
    date DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    amount INT NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME NULL,

    PRIMARY KEY (schedule_id),
    KEY idx_schedule_goal_id (goal_id),
    KEY idx_schedule_date (date),
    UNIQUE KEY uk_schedule_goal_date (goal_id, date),

    CONSTRAINT fk_schedule_goal
        FOREIGN KEY (goal_id)
        REFERENCES GOAL (goal_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_schedule_amount
        CHECK (amount IS NULL OR amount >= 0),

    CONSTRAINT chk_schedule_time
        CHECK (
            start_time IS NULL
            OR end_time IS NULL
            OR end_time >= start_time
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 학습 기록
CREATE TABLE RECORD (
    record_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    completed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    study_time INT NULL,

    PRIMARY KEY (record_id),
    KEY idx_record_user_id (user_id),
    UNIQUE KEY uk_record_schedule_id (schedule_id),

    CONSTRAINT fk_record_user
        FOREIGN KEY (user_id)
        REFERENCES USER (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_record_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES SCHEDULE (schedule_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_record_study_time
        CHECK (study_time IS NULL OR study_time >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 캐릭터
CREATE TABLE CHARACTER (
    character_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL DEFAULT 'FocusGrow',
    level INT NOT NULL DEFAULT 1,
    experience INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (character_id),
    UNIQUE KEY uk_character_user_id (user_id),

    CONSTRAINT fk_character_user
        FOREIGN KEY (user_id)
        REFERENCES USER (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT chk_character_level
        CHECK (level >= 1),

    CONSTRAINT chk_character_experience
        CHECK (experience >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 초기 확인용
-- SELECT * FROM USER;
-- SELECT * FROM GOAL;
-- SELECT * FROM SCHEDULE;
-- SELECT * FROM RECORD;
-- SELECT * FROM CHARACTER;
