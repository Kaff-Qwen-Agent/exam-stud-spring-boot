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

    private String loginAs(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private String login() throws Exception {
        return loginAs("admin", "admin123");
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        assertThat(loginAs("user", "password")).isNotBlank();
        assertThat(login()).isNotBlank();
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/group"))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentRoleCanViewStudentsOnly() throws Exception {
        String token = loginAs("student", "student123");
        mockMvc.perform(get("/api/student").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/group").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        // Валидное тело: проверяем именно роль, а не @Valid (он срабатывает раньше @PreAuthorize)
        mockMvc.perform(post("/api/student")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomerZachetki\":950001,\"shifrGruppyi\":1,\"familiya\":\"X\",\"imya\":\"Y\","
                                + "\"otchestvo\":\"Z\",\"gorod\":\"V\",\"adres\":\"A\",\"tel\":\"T\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherRoleCanViewGroupsOnly() throws Exception {
        String token = loginAs("teacher", "teacher123");
        mockMvc.perform(get("/api/group").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/student").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shifr\":900,\"nazvanie\":\"NoAccess\",\"dataFormir\":\"2024-09-01\",\"kodPlana\":1}"))
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
    void studentsFilteredByIdRangeWithPagination() throws Exception {
        String auth = "Bearer " + login();
        mockMvc.perform(get("/api/student?from=10122&to=10123").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nomerZachetki").value(10122))
                .andExpect(jsonPath("$.content[1].nomerZachetki").value(10123))
                .andExpect(jsonPath("$.totalElements").value(2));
        // Размер страницы и сортировка: Sidorov идёт после Petrov по алфавиту
        mockMvc.perform(get("/api/student?from=10122&to=10123&size=1&sort=familiya,desc").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].familiya").value("Sidorov"))
                .andExpect(jsonPath("$.totalElements").value(2));
        mockMvc.perform(get("/api/student?from=200&to=100").header("Authorization", auth))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/student?sort=noSushchestvujushcheePole").header("Authorization", auth))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateGroupNameReturns409() throws Exception {
        String auth = "Bearer " + login();
        // 'It-1' — сид-группа
        mockMvc.perform(post("/api/group")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shifr\":900,\"nazvanie\":\"It-1\",\"dataFormir\":\"2024-09-01\",\"kodPlana\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("Группа с названием 'It-1' уже существует"));
        // Переименование другой группы в существующее имя тоже конфликт
        mockMvc.perform(put("/api/group/3")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shifr\":3,\"nazvanie\":\"It-1\",\"dataFormir\":\"2013-09-15\",\"kodPlana\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void duplicateStudentIdReturns409() throws Exception {
        mockMvc.perform(post("/api/student")
                        .header("Authorization", "Bearer " + login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomerZachetki\":10122,\"shifrGruppyi\":2,\"familiya\":\"X\",\"imya\":\"Y\","
                                + "\"otchestvo\":\"Z\",\"gorod\":\"V\",\"adres\":\"A\",\"tel\":\"T\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("Студент с номером зачетки 10122 уже существует"));
    }

    @Test
    void apiDocsExposedWithoutAuth() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/group/statistics']").exists())
                .andExpect(jsonPath("$.paths['/api/student']").exists());
    }

    @Test
    void groupStatusStatisticsCounts() throws Exception {
        mockMvc.perform(get("/api/group/statistics").header("Authorization", "Bearer " + login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].nazvanie").value("It-1"))
                .andExpect(jsonPath("$[0].statusCounts.graduate").value(1))
                .andExpect(jsonPath("$[0].statusCounts.enrolled").value(1))
                .andExpect(jsonPath("$[1].statusCounts['academic leave']").value(1));
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

        // Убираем за собой, чтобы состояние БД оставалось сид-детерминированным
        mockMvc.perform(delete("/api/group/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
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
