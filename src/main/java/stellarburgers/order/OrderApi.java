package stellarburgers.order;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import stellarburgers.utils.BaseURI;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class OrderApi extends BaseURI {
    private static final String ORDER_URI = BASE_URI + "orders";
    private static final String GET_INGREDIENTS = BASE_URI + "ingredients";

    @Step("Получение данных об ингредиентах")
    public ValidatableResponse getIngredients() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(GET_INGREDIENTS)
                .then();
    }

    @Step("Создание заказа с авторизацией и ингредиентами")
    public ValidatableResponse createOrder(String accessToken, Order order){
        return given()
                .header("Authorization", accessToken)
                .spec(getReqSpec())
                .body(order)
                .when()
                .post(ORDER_URI)
                .then();
    }

    @Step("Создание заказа без авторизации")
    public ValidatableResponse createOrderWithoutAuth(List<String> ingredients){
        Map<String, Object> requestMap = Map.of("ingredients", ingredients);
        return given()
                .spec(getReqSpec())
                .body(requestMap)
                .when()
                .post(ORDER_URI)
                .then();
    }

    @Step("Создание заказа без ингредиентов")
    public ValidatableResponse createOrderWithoutIngred(String accessToken){
        return given()
                .header("Authorization", accessToken)
                .spec(getReqSpec())
                .body("")
                .when()
                .post(ORDER_URI)
                .then();
    }

    @Step("Получение заказов авторизованного пользователя")
    public ValidatableResponse getOrdersAuthUser(String accessToken){
        return given()
                .header("Authorization", accessToken)
                .spec(getReqSpec())
                .when()
                .get(ORDER_URI)
                .then();
    }

    @Step("Получение заказов неавторизованного пользователя")
    public ValidatableResponse getOrdersWithoutAuthUser(){
        return given()
                .spec(getReqSpec())
                .when()
                .get(ORDER_URI)
                .then();
    }
}
