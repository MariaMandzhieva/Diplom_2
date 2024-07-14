package stellarburgers.user;


import io.qameta.allure.junit4.DisplayName;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateUserTest {
    private User user;
    private UserApi userApi;

    @Before
    public void setUp(){
        userApi = new UserApi();

        String email = (RandomStringUtils.randomAlphabetic(10) + "@yandex.ru").toLowerCase();
        String password = RandomStringUtils.randomAlphabetic(8);
        String name = RandomStringUtils.randomAlphabetic(8);

        user = new User(email, password, name);
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void createUser(){
        userApi.createUser(user)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void createAlreadyExistedUser(){
        userApi.createUser(user);

        userApi.createUser(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .body("success", equalTo(false));
    }

    @Test
    @DisplayName("Создание пользователя без почты")
    public void createUserWithoutEmail(){
        user.setEmail(null);
        userApi.createUser(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    public void createUserWithoutName(){
        user.setName(null);
        userApi.createUser(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    public void createUserWithoutPassword(){
        user.setPassword(null);
        userApi.createUser(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("Email, password and name are required fields"));
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
