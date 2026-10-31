package com.focusgrow.backend.records;
import com.focusgrow.backend.schedule.Schedule;
import com.focusgrow.backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="RECORD") @Getter @NoArgsConstructor
public class StudyRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="record_id") private Long recordId;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
 @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="schedule_id",nullable=false,unique=true) private Schedule schedule;
 @Column(nullable=false) private LocalDateTime completedAt;
 private Integer studyTime;
 public StudyRecord(Schedule schedule,LocalDateTime now){
  this.schedule=schedule;this.user=schedule.getGoal().getUser();this.completedAt=now;
  // 원본 API에 시간 입력이 없으므로 추정하지 않고 null로 저장한다.
  this.studyTime=null;
 }
}
