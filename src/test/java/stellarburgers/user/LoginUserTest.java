package stellarburgers.user;

import io.qameta.allure.junit4.DisplayName;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;

public class LoginUserTest {
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
    @DisplayName("Логин под существующим пользователем")
    public void loginUser(){
        userApi.loginUser(user)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Логин с неверной почтой")
    public void loginUserWithIncorrectEmail(){
        user.setEmail("incorrectEmail");
        userApi.loginUser(user)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    public void loginUserWithIncorrectPassword(){
        user.setPassword("incorrectPassword");
        userApi.loginUser(user)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("email or password are incorrect"));
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
