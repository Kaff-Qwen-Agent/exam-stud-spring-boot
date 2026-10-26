package by.vstu.vsrpp.exam.studgroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// Отдельная in-memory БД, чтобы сид-данные не конфликтовали с контекстом
// StudGroupApplicationTests (иначе повторные INSERT дают нарушение уникальности)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:gr-it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private String authGet(String url) throws Exception {
        return mockMvc.perform(get(url).header("Authorization", "Bearer " + login()))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        assertThat(login()).isNotBlank();
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/group"))
                .andExpect(status().isForbidden());
    }

    @Test
    void seededGroupsAreReturned() throws Exception {
        mockMvc.perform(get("/api/group").header("Authorization", "Bearer " + login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].nazvanie").value("It-1"));
    }

    @Test
    void groupByIdReturnsStudents() throws Exception {
        mockMvc.perform(get("/api/group/1").header("Authorization", "Bearer " + login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nazvanie").value("It-1"))
                .andExpect(jsonPath("$.students.length()").value(2));
    }

    @Test
    void missingGroupReturns400() throws Exception {
        mockMvc.perform(get("/api/group/9999").header("Authorization", "Bearer " + login()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void groupLifecycleCreateUpdateDelete() throws Exception {
        String token = login();
        String body = "{\"shifr\":100,\"nazvanie\":\"Test-G\",\"dataFormir\":\"2024-09-01\",\"kodPlana\":5}";

        MvcResult created = mockMvc.perform(post("/api/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nazvanie").value("Test-G"))
                .andReturn();
        int id = ((Integer) com.jayway.jsonpath.JsonPath.read(
                created.getResponse().getContentAsString(), "$.shifr")).intValue();

        mockMvc.perform(put("/api/group/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shifr\":100,\"nazvanie\":\"Test-G2\",\"dataFormir\":\"2024-09-01\",\"kodPlana\":6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nazvanie").value("Test-G2"))
                .andExpect(jsonPath("$.kodPlana").value(6));

        mockMvc.perform(delete("/api/group/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/group/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStudentsRejectsMoreThanThree() throws Exception {
        String token = login();

        MvcResult created = mockMvc.perform(post("/api/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shifr\":101,\"nazvanie\":\"Val-G\",\"dataFormir\":\"2024-09-01\",\"kodPlana\":5}"))
                .andExpect(status().isCreated())
                .andReturn();
        int id = ((Integer) com.jayway.jsonpath.JsonPath.read(
                created.getResponse().getContentAsString(), "$.shifr")).intValue();

        String student = "{\"nomerZachetki\":901001,\"familiya\":\"Ivanov\",\"imya\":\"Ivan\","
                + "\"otchestvo\":\"Ivanovich\",\"gorod\":\"Vitebsk\",\"adres\":\"st. 1\",\"tel\":\"+375290000001\"}";

        mockMvc.perform(post("/api/group/addstudents/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"students\":[" + student + "," + student.replace("901001", "901002")
                                + "," + student.replace("901001", "901003") + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students.length()").value(3));

        mockMvc.perform(post("/api/group/addstudents/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"students\":[" + student.replace("901001", "901004") + "]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void studentCrudRoundtrip() throws Exception {
        String token = login();
        String createBody = "{\"nomerZachetki\":902001,\"shifrGruppyi\":2,\"familiya\":\"Petrov\",\"imya\":\"Petr\","
                + "\"otchestvo\":\"Petrovich\",\"gorod\":\"Vitebsk\",\"adres\":\"st. 2\",\"tel\":\"+375290000002\"}";
        String updateBody = "{\"nomerZachetki\":902001,\"shifrGruppyi\":4,\"familiya\":\"Sidorov\",\"imya\":\"Anna\","
                + "\"otchestvo\":\"Petrovich\",\"gorod\":\"Minsk\",\"adres\":\"st. 3\",\"tel\":\"+375290000003\"}";

        mockMvc.perform(post("/api/student")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.familiya").value("Petrov"));

        mockMvc.perform(put("/api/student/902001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.familiya").value("Sidorov"));

        mockMvc.perform(delete("/api/student/902001").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/student/902001").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidGroupRequestReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/group")
                        .header("Authorization", "Bearer " + login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shifr\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));
    }
}
