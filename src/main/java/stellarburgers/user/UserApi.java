package stellarburgers.user;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.given;
import stellarburgers.utils.BaseURI;

import static io.restassured.path.json.JsonPath.given;

public class UserApi extends BaseURI {

    private static final String USER_URI = BASE_URI + "auth/";

    @Step("Создание пользователя")
    public ValidatableResponse createUser(User user) {
        return RestAssured.given()
                .spec(getReqSpec())
                .body(user)
                .post(USER_URI + "register")
                .then();
    }

    @Step("Авторизация пользователя")
    public ValidatableResponse loginUser(User user) {
        return RestAssured.given()
                .spec(getReqSpec())
                .body(user)
                .post(USER_URI + "login")
                .then();
    }

    @Step ("Получение токена")
    private String getToken(User user){
        ValidatableResponse loginResponse = given()
                .spec(getReqSpec())
                .body(user)
                .when()
                .post(USER_URI + "login")
                .then()
                .assertThat()
                .statusCode(200);

        String token = loginResponse.extract().path("accessToken");
        if (token == null) {
            throw new IllegalArgumentException("Token is null");
        }
        return token;
    }

    @Step ("Удаление пользователя")
    public ValidatableResponse deleteUser(User user){
        return given()
                .spec(getReqSpec())
                .header("accessToken", user.getAccessToken())
                .when()
                .delete(USER_URI +"user")
                .then();
    }

    @Step ("Изменение почты пользователя с авторизацией")
    public ValidatableResponse editEmailWithAuth(User user, String newEmail){
        String token = getToken(user);
        user.setEmail(newEmail);
        return given()
                .spec(getReqSpec())
                .header("Authorization", token)
                .body(user)
                .when()
                .patch(USER_URI +"user")
                .then();
    }

    @Step ("Изменение имени пользователя с авторизацией")
    public ValidatableResponse editNameWithAuth(User user, String newName) {
        String token = getToken(user);
        user.setName(newName);
        return given()
                .spec(getReqSpec())
                .header("Authorization", token)
                .body(user)
                .when()
                .patch(USER_URI + "user")
                .then();
    }

    @Step ("Изменение почты пользователя без авторизации")
    public ValidatableResponse editEmailWithoutAuth(User user, String newEmail){
        user.setEmail(newEmail);
        return given()
                .spec(getReqSpec())
                .body(user)
                .when()
                .patch(USER_URI + "user")
                .then();
    }

    @Step ("Изменение имени пользователя без авторизации")
    public ValidatableResponse editNameWithoutAuth(User user, String newName){
        user.setName(newName);
        return given()
                .spec(getReqSpec())
                .body(user)
                .when()
                .patch(USER_URI + "user")
                .then();
    }
}
