package com.focusgrow.backend;
import com.focusgrow.backend.goal.*;
import com.focusgrow.backend.goal.dto.GoalUpdateRequest;
import com.focusgrow.backend.user.*;
import com.focusgrow.backend.schedule.*;
import com.focusgrow.backend.records.*;
import com.focusgrow.backend.common.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
@org.springframework.test.context.ActiveProfiles("demo")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
class PlanningIntegrationTest {
 @TestConfiguration static class FixedTime {
  @Bean @Primary Clock fixedClock(){return Clock.fixed(Instant.parse("2026-10-27T03:00:00Z"),ZoneId.of("Asia/Seoul"));}
 }
 @Autowired GoalService goals;
 @Autowired GoalRepository goalRepo;
 @Autowired ScheduleService service;
 @Autowired ScheduleRepository schedules;
 @Autowired StudyRecordRepository records;
 @Autowired UserRepository users;
 Long owner;
 LocalDate today=LocalDate.of(2026,10,27);
 @BeforeEach void setup(){
  records.deleteAll();schedules.deleteAll();goalRepo.deleteAll();users.deleteAll();
  owner=users.save(new User(UUID.randomUUID()+"@test.local","hash","tester")).getUserId();
 }
 Goal create(int amount,int days){return goals.createGoal(owner,"책 읽기","",today,today.plusDays(days-1),amount,"page");}
 List<Schedule> rows(Goal g){return schedules.findByGoal_GoalIdOrderByDateAsc(g.getGoalId());}
 GoalUpdateRequest patch(String field,Object value){var r=new GoalUpdateRequest();ReflectionTestUtils.setField(r,field,value);return r;}
 @Test void tenPagesThreeDays(){var g=create(10,3);assertEquals(List.of(4,3,3),rows(g).stream().map(Schedule::getAmount).toList());}
 @Test void sixHundredSixtyOneDays(){var g=create(600,61);assertEquals(61,rows(g).size());assertEquals(600,rows(g).stream().mapToInt(Schedule::getAmount).sum());assertEquals(51,rows(g).stream().filter(s->s.getAmount()==10).count());}
 @Test void fewerPagesThanDays(){var g=create(2,5);assertEquals(List.of(1,1),rows(g).stream().map(Schedule::getAmount).toList());}
 @Test void todayIsStoredAndReadOnly(){var g=create(10,3);assertEquals(1,service.today(owner).size());service.today(owner);assertEquals(3,rows(g).size());}
 @Test void completionPersistsRecordAndProgress(){var g=create(10,3);service.complete(owner,rows(g).getFirst().getScheduleId());assertEquals(4,goals.completedAmount(g.getGoalId()));assertEquals(1,records.count());assertNull(records.findAll().getFirst().getStudyTime());}
 @Test void duplicateCompletionIsConflict(){var g=create(10,3);long id=rows(g).getFirst().getScheduleId();service.complete(owner,id);assertThrows(ConflictException.class,()->service.complete(owner,id));assertEquals(1,records.count());}
 @Test void lastScheduleCompletesGoal(){var g=create(1,1);service.complete(owner,rows(g).getFirst().getScheduleId());assertEquals("COMPLETED",goalRepo.findById(g.getGoalId()).orElseThrow().getStatus());}
 @Test void anotherUserCannotComplete(){var g=create(1,1);assertThrows(NotFoundException.class,()->service.complete(owner+999,rows(g).getFirst().getScheduleId()));assertEquals(0,records.count());}
 @Test void futureCompletionRejected(){var g=create(10,3);assertThrows(ConflictException.class,()->service.complete(owner,rows(g).getLast().getScheduleId()));assertEquals(0,records.count());}
 @Test void extendingDeadlineKeepsCompletedRows(){var g=create(10,3);long id=rows(g).getFirst().getScheduleId();service.complete(owner,id);goals.updateGoal(owner,g.getGoalId(),patch("endDate",today.plusDays(4)));var all=rows(g);assertTrue(schedules.findById(id).orElseThrow().isCompleted());assertEquals(10,all.stream().mapToInt(Schedule::getAmount).sum());assertEquals(1,records.count());assertEquals(List.of(2,2,1,1),all.stream().filter(s->!s.isCompleted()).map(Schedule::getAmount).toList());}
 @Test void impossibleChangeRollsBackGoalAndSchedules(){var g=create(10,3);service.complete(owner,rows(g).getFirst().getScheduleId());assertThrows(IllegalArgumentException.class,()->goals.updateGoal(owner,g.getGoalId(),patch("endDate",today.minusDays(1))));assertEquals(today.plusDays(2),goalRepo.findById(g.getGoalId()).orElseThrow().getEndDate());assertEquals(3,rows(g).size());}
 @Test void lowerThanCompletedRejected(){var g=create(10,3);service.complete(owner,rows(g).getFirst().getScheduleId());assertThrows(IllegalArgumentException.class,()->goals.updateGoal(owner,g.getGoalId(),patch("totalAmount",3)));assertEquals(10,goalRepo.findById(g.getGoalId()).orElseThrow().getTotalAmount());}
 @Test void explicitReplanMovesOverdueWork(){var g=goals.createGoal(owner,"과거 포함","",today.minusDays(2),today.plusDays(2),10,"page");goals.replan(owner,g.getGoalId());assertEquals(List.of(4,3,3),rows(g).stream().map(Schedule::getAmount).toList());assertEquals(today,rows(g).getFirst().getDate());}
 @Test void deleteRemovesChildRows(){var g=create(10,3);service.complete(owner,rows(g).getFirst().getScheduleId());goals.deleteGoal(owner,g.getGoalId());assertEquals(0,records.count());assertEquals(0,schedules.count());assertEquals(0,goalRepo.count());}
 @Test void invalidCreateDoesNotPersist(){assertThrows(IllegalArgumentException.class,()->create(0,3));assertEquals(0,goalRepo.count());assertEquals(0,schedules.count());}
 @Test void tooLongPeriodRejected(){assertThrows(IllegalArgumentException.class,()->create(10,3651));assertEquals(0,goalRepo.count());}
 @Test void renamePreservesIds(){var g=create(10,3);var ids=rows(g).stream().map(Schedule::getScheduleId).toList();goals.updateGoal(owner,g.getGoalId(),patch("title","새 이름"));assertEquals(ids,rows(g).stream().map(Schedule::getScheduleId).toList());assertTrue(rows(g).stream().allMatch(s->s.getTitle().equals("새 이름")));}
 @Test void concurrentCompletionCreatesOnlyOneRecord() throws Exception {
  var g=create(1,1);long id=rows(g).getFirst().getScheduleId();var gate=new CountDownLatch(1);
  try(var pool=Executors.newFixedThreadPool(2)){
   Callable<String> job=()->{gate.await();try{service.complete(owner,id);return "ok";}catch(ConflictException e){return "conflict";}};
   var a=pool.submit(job);var b=pool.submit(job);gate.countDown();
   assertEquals(Set.of("ok","conflict"),Set.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS)));
  }
  assertEquals(1,records.count());
 }
}
