package stellarburgers.user;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.order.Order;
import stellarburgers.order.OrderApi;

import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class CreateOrderTest {
    private final OrderApi orderApi = new OrderApi();
    private final UserApi userApi = new UserApi();
    private User user;

    @Before
    public void setUp(){
        String email = (RandomStringUtils.randomAlphabetic(10) + "@yandex.ru").toLowerCase();
        String password = RandomStringUtils.randomAlphabetic(8);
        String name = RandomStringUtils.randomAlphabetic(8);

        user = new User(email, password, name);
        userApi.createUser(user);
    }

    @Test
    @DisplayName("Проверка создания заказа с авторизацией и с ингредиентами")
    public void createOrderWithAuthAndIngredTest() {
        ValidatableResponse loginResponse = userApi
                .loginUser(user)
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        String token = loginResponse.extract().path("accessToken");
        ValidatableResponse ingredientsResponse = orderApi.getIngredients()
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));

        String firstIngredtId = ingredientsResponse.extract().path("data[0]._id");
        String secondIngredId = ingredientsResponse.extract().path("data[2]._id");
        String thirdIngredId = ingredientsResponse.extract().path("data[4]._id");
        List<String> ingredients = List.of(firstIngredtId, secondIngredId, thirdIngredId);
        Order order = new Order(ingredients);
        ValidatableResponse orderResponse = orderApi.createOrder(token, order)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));

        List<String> responseIngredients = orderResponse
                .extract()
                .path("order.ingredients._id");

        assertThat("Ингредиенты из запроса не совпадают с ингредиентами из ответа",
                responseIngredients, containsInAnyOrder(ingredients.toArray()));
    }

    @Test
    @DisplayName("Проверка создания заказа без авторизации")
    public void createOrderWithoutAuth(){
        ValidatableResponse ingredientsResponse = orderApi.getIngredients()
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));

        String firstIngredId = ingredientsResponse.extract().path("data[0]._id");
        String secondIngredId = ingredientsResponse.extract().path("data[2]._id");
        String thirdIngredId = ingredientsResponse.extract().path("data[4]._id");
        List<String> ingredients = List.of(firstIngredId, secondIngredId, thirdIngredId);
        orderApi
                .createOrderWithoutAuth(ingredients)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Проверка создания заказа без ингредиентов")
    public void createOrderWithoutIngred(){
        ValidatableResponse validatableResponse = userApi
                .loginUser(user)
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true));
        String token = validatableResponse
                .extract()
                .path("accessToken");
        orderApi
                .createOrderWithoutIngred(token)
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .body("success", equalTo(false))
                .and()
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Проверка создания заказа с неверным хешем ингредиентов")
    public void createOrderWithIncorrectIngred(){
        ValidatableResponse loginResponse = userApi
                .loginUser(user)
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true));

        String token = loginResponse.extract().path("accessToken");
        orderApi.getIngredients()
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", equalTo(true));

        String firstIngredId = "1234abc";
        String secondIngredId = "5678def";
        String thirdIngredId = "9101ghij";
        List<String> ingredients = List.of(firstIngredId, secondIngredId, thirdIngredId);
        Order order = new Order(ingredients);
        orderApi.createOrder(token, order)
                .assertThat()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
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
