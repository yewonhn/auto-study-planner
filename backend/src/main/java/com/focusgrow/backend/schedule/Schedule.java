package com.focusgrow.backend.schedule;
import com.focusgrow.backend.goal.Goal;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
@Entity @Table(name="SCHEDULE", uniqueConstraints=@UniqueConstraint(columnNames={"goal_id","date"}))
@Getter @NoArgsConstructor
public class Schedule {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="schedule_id") private Long scheduleId;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="goal_id",nullable=false) private Goal goal;
 @Column(name="date",nullable=false) private LocalDate date;
 @Column(nullable=false,length=255) private String title;
 @Column(nullable=false) private Integer amount;
 private LocalTime startTime;
 private LocalTime endTime;
 @Column(nullable=false) private boolean completed;
 private LocalDateTime completedAt;
 public Schedule(Goal goal,LocalDate date,int amount) {
  this.goal=goal;this.date=date;this.amount=amount;this.title=goal.getTitle();
 }
 public void complete(LocalDateTime now){completed=true;completedAt=now;}
 public void rename(String title){this.title=title;}
}
