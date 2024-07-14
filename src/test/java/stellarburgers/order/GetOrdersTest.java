package stellarburgers.order;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.user.User;
import stellarburgers.user.UserApi;

import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;

public class GetOrdersTest {
    private final OrderApi orderApi = new OrderApi();
    private final UserApi userApi = new UserApi();
    private User user;

    @Before
    public void setUp() {
        String email = (RandomStringUtils.randomAlphabetic(10) + "@yandex.ru").toLowerCase();
        String password = RandomStringUtils.randomAlphabetic(8);
        String name = RandomStringUtils.randomAlphabetic(8);

        user = new User(email, password, name);
        userApi.createUser(user);
    }

    @Test
    @DisplayName("Проверка получения заказов авторизованного пользователя")
    public void getOrdersAuthUser(){
        ValidatableResponse validatableResponse = userApi
                .loginUser(user)
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true));
        String token = validatableResponse
                .extract()
                .path("accessToken");
        ValidatableResponse ingredientsResponse = orderApi.getIngredients()
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true));
        String firstIngredId = ingredientsResponse.extract().path("data[0]._id");
        String secondIngredId = ingredientsResponse.extract().path("data[2]._id");
        String thirdIngredId = ingredientsResponse.extract().path("data[4]._id");
        List<String> ingredients = List.of(firstIngredId, secondIngredId, thirdIngredId);
        Order order = new Order(ingredients);
        orderApi
                .createOrder(token, order);
        orderApi
                .getOrdersAuthUser(token)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Проверка получения заказов не авторизованного пользователя")
    public void getOrderWithoutAuthUser(){
        orderApi
                .getOrdersWithoutAuthUser()
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
