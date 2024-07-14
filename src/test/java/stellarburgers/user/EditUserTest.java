package stellarburgers.user;

import io.qameta.allure.junit4.DisplayName;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;

public class EditUserTest {
    private User user;
    private UserApi userApi;

    @Before
    public void setUp() {
        userApi = new UserApi();

        String email = (RandomStringUtils.randomAlphabetic(10) + "@yandex.ru").toLowerCase();
        String password = RandomStringUtils.randomAlphabetic(8);
        String name = RandomStringUtils.randomAlphabetic(8);

        user = new User(email, password, name);
        userApi.createUser(user);
    }

    @Test
    @DisplayName("Изменение почты пользователя с авторизацией")
    public void editUsersEmailWithAuth(){
        String newEmail = (RandomStringUtils.randomAlphabetic(10) + "@yandex.ru").toLowerCase();
        userApi
                .editEmailWithAuth(user, newEmail)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true))
                .and()
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @DisplayName("Изменение имени пользователя с авторизацией")
    public void editUsersNameWithAuth(){
        String newName = RandomStringUtils.randomAlphabetic(8);
        userApi
                .editNameWithAuth(user, newName)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true))
                .and()
                .body("user.name", equalTo(newName));
    }

    @Test
    @DisplayName("Изменение почты пользователя без авторизации")
    public void editUsersEmailWithoutAuth(){
        String newEmail = (RandomStringUtils.randomAlphabetic(10) + "@yandex.ru").toLowerCase();
        userApi
                .editEmailWithoutAuth(user, newEmail)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Изменение имени пользователя без авторизации")
    public void editUsersNameWithoutAuth(){
        String newName = RandomStringUtils.randomAlphabetic(8);
        userApi
                .editNameWithoutAuth(user, newName)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown(){
        String token = userApi.loginUser(user)
                .extract().body().path("accessToken");
        user.setAccessToken(token);

        if (user.getAccessToken() != null) {
            userApi.deleteUser(user);
        }
    }
}
