-- 별도 테스트 DB를 선택한 뒤 실행. 기존 데이터를 삭제하지 않는다.
-- USER, GOAL 테이블은 기존 팀 백엔드와 동일하게 먼저 준비한다.
CREATE TABLE IF NOT EXISTS `SCHEDULE` (
 schedule_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 goal_id BIGINT NOT NULL, `date` DATE NOT NULL,
 title VARCHAR(255) NOT NULL, amount INT NOT NULL,
 start_time TIME NULL, end_time TIME NULL,
 completed BOOLEAN NOT NULL DEFAULT FALSE, completed_at DATETIME NULL,
 UNIQUE KEY uk_schedule_goal_date(goal_id,`date`),
 CONSTRAINT fk_demo_schedule_goal FOREIGN KEY(goal_id) REFERENCES GOAL(goal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `RECORD` (
 record_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL, schedule_id BIGINT NOT NULL,
 completed_at DATETIME NOT NULL, study_time INT NULL,
 UNIQUE KEY uk_record_schedule(schedule_id),
 CONSTRAINT fk_demo_record_user FOREIGN KEY(user_id) REFERENCES USER(user_id),
 CONSTRAINT fk_demo_record_schedule FOREIGN KEY(schedule_id) REFERENCES `SCHEDULE`(schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
