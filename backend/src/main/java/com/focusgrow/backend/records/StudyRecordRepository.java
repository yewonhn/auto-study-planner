package com.focusgrow.backend.records;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudyRecordRepository extends JpaRepository<StudyRecord,Long> {
 List<StudyRecord> findByUser_UserId(Long userId);
 List<StudyRecord> findBySchedule_Goal_GoalId(Long goalId);
}
