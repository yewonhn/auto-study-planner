package com.focusgrow.backend;
import com.focusgrow.backend.goal.*;
import com.focusgrow.backend.user.*;
import com.focusgrow.backend.schedule.*;
import com.focusgrow.backend.records.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@org.springframework.test.context.ActiveProfiles("demo")
@SpringBootTest @Import(PlanningIntegrationTest.FixedTime.class)
class PlanningApiTest {
 @Autowired WebApplicationContext context;
 @Autowired StudyRecordRepository records;
 @Autowired ScheduleRepository schedules;
 @Autowired GoalRepository goals;
 @Autowired UserRepository users;
 MockMvc http;
 String user;
 @BeforeEach void setup() throws Exception {
  records.deleteAll();schedules.deleteAll();goals.deleteAll();users.deleteAll();
  http=MockMvcBuilders.webAppContextSetup(context).build();
  var result=http.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
   .content("{\"email\":\""+UUID.randomUUID()+"@test.local\",\"password\":\"TestOnly1234!\",\"nickname\":\"tester\"}"))
   .andExpect(status().isCreated()).andReturn();
  user=String.valueOf(new tools.jackson.databind.json.JsonMapper().readTree(result.getResponse().getContentAsString()).get("userId").asLong());
 }
 long create() throws Exception {
  var result=http.perform(post("/api/goals").header("X-User-Id",user).contentType(MediaType.APPLICATION_JSON)
   .content("{\"title\":\"read\",\"startDate\":\"2026-10-27\",\"endDate\":\"2026-10-29\",\"totalAmount\":10,\"unit\":\"page\"}"))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.dailyAmount").value(4)).andReturn();
  return new tools.jackson.databind.json.JsonMapper().readTree(result.getResponse().getContentAsString()).get("goalId").asLong();
 }
 @Test void createReadCompleteReplanDeleteFlow() throws Exception {
  long id=create();
  var today=http.perform(get("/api/schedules/today").header("X-User-Id",user))
   .andExpect(status().isOk()).andExpect(jsonPath("$.schedules[0].amount").value(4))
   .andExpect(jsonPath("$.schedules[0].completed").value(false)).andReturn();
  long sid=new tools.jackson.databind.json.JsonMapper().readTree(today.getResponse().getContentAsString()).get("schedules").get(0).get("scheduleId").asLong();
  http.perform(patch("/api/schedules/"+sid+"/complete").header("X-User-Id",user))
   .andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(true)).andExpect(jsonPath("$.completedAt").exists());
  http.perform(patch("/api/schedules/"+sid+"/complete").header("X-User-Id",user)).andExpect(status().isConflict());
  http.perform(get("/api/goals/"+id).header("X-User-Id",user)).andExpect(status().isOk())
   .andExpect(jsonPath("$.progress").value(40)).andExpect(jsonPath("$.completedAmount").value(4));
  http.perform(get("/api/goals").header("X-User-Id",user)).andExpect(jsonPath("$[0].progress").value(40));
  http.perform(get("/api/records").header("X-User-Id",user)).andExpect(status().isOk())
   .andExpect(jsonPath("$.records[0].completedCount").value(1)).andExpect(jsonPath("$.records[0].totalCount").value(1));
  http.perform(patch("/api/goals/"+id).header("X-User-Id",user).contentType(MediaType.APPLICATION_JSON)
   .content("{\"endDate\":\"2026-10-31\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.completedAmount").value(4));
  http.perform(post("/api/schedules/preview/"+id).header("X-User-Id",user))
   .andExpect(status().isOk()).andExpect(jsonPath("$.plan.remainingAmount").value(6))
   .andExpect(jsonPath("$.plan.availableDayCount").value(4));
  http.perform(post("/api/schedules/replan/"+id).header("X-User-Id",user)).andExpect(status().isOk());
  http.perform(get("/api/records/monthly?year=2026&month=10").header("X-User-Id",user)).andExpect(status().isOk()).andExpect(jsonPath("$.records[0].completionRate").value(100));
  http.perform(delete("/api/goals/"+id).header("X-User-Id",user)).andExpect(status().isOk());
  http.perform(get("/api/schedules/today").header("X-User-Id",user)).andExpect(jsonPath("$.schedules").isEmpty());
  http.perform(get("/api/records").header("X-User-Id",user)).andExpect(jsonPath("$.records").isEmpty());
  Assertions.assertEquals(0,records.count());Assertions.assertEquals(0,schedules.count());
 }
 @Test void missingHeaderIsBadRequest() throws Exception {
  http.perform(get("/api/schedules/today")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("BAD_REQUEST"));
 }
 @Test void anotherUserIsNotFound() throws Exception {
  long id=create();
  http.perform(get("/api/goals/"+id).header("X-User-Id","999999")).andExpect(status().isNotFound());
 }
 @Test void invalidMonthIsBadRequest() throws Exception {
  http.perform(get("/api/records/monthly?year=2026&month=13").header("X-User-Id",user)).andExpect(status().isBadRequest());
 }
 @Test void missingMonthIsBadRequest() throws Exception {
  http.perform(get("/api/records/monthly?year=2026").header("X-User-Id",user)).andExpect(status().isBadRequest());
 }
 @Test void invalidGoalDoesNotPersist() throws Exception {
  http.perform(post("/api/goals").header("X-User-Id",user).contentType(MediaType.APPLICATION_JSON).content("{}"))
   .andExpect(status().isBadRequest());
  Assertions.assertEquals(0,goals.count());Assertions.assertEquals(0,schedules.count());
 }
}
