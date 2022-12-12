package Automation;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import static io.restassured.RestAssured.*;
import java.io.File;

public class AutomatedTest {
    public String phone = "629671989421";
    public String password = "password";
    public String latlong = "84567890";
    public String device_token = "12345";
    public int device_type = 2;
    public String otp = "9672";
    public String user_id = "";
    public String auth_token = "";
    public String sg_id = "";
    public String cover_id = "";
    public String profil_id = "";

    @BeforeMethod
    public void setup() {
        RestAssured.baseURI = "http://pretest-qa.dcidev.id";
    }

    @Test(priority = 1)
    public void Register_Valid() {
        String country = "indonesia";
        JSONObject bodyJson = new JSONObject();

        bodyJson.put("phone", phone);
        bodyJson.put("password", password);
        bodyJson.put("country", country);
        bodyJson.put("latlong", latlong);
        bodyJson.put("device_token", device_token);
        bodyJson.put("device_type", device_type);

        given().log().all()
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/register")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user.phone", Matchers.equalTo(phone))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/register.json"));
    }

    @Test(priority = 2)
    public void Request_OTP() {
        JSONObject bodyJson = new JSONObject();

        bodyJson.put("phone", phone);

        String response = given().log().all()
                .header("Content-Type", "application/json")
                .body(bodyJson.toString()).when()
                .post("/api/v1/register/otp/request")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user.phone", Matchers.equalTo(phone))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/request_otp.json"))
                .extract().response().asString();

        JsonPath js = new JsonPath(response);
        String id = js.getString("data.user.id");
        user_id = id;
        System.out.println(user_id);
    }

    @Test(priority = 3)
    public void Match_OTP() {

        System.out.println(user_id);
        JSONObject bodyJson = new JSONObject();

        bodyJson.put("user_id", user_id);
        bodyJson.put("otp_code", otp);

        given().log().all()
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/register/otp/match")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user.token_type", Matchers.equalTo("bearer"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/match_otp.json"));
    }

    @Test(priority = 4)
    public void Oauth_Login() {
        JSONObject bodyJson = new JSONObject();

        bodyJson.put("phone", phone);
        bodyJson.put("password", password);
        bodyJson.put("latlong", latlong);
        bodyJson.put("device_token", device_token);
        bodyJson.put("device_type", device_type);

        String response = given().log().all()
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/oauth/sign_in")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user.token_type", Matchers.equalTo("bearer"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/login.json"))
                .extract().response().asString();

        JsonPath js = new JsonPath(response);
        String token = js.getString("data.user.access_token");
        auth_token = token;
    }

    @Test(priority = 5)
    public void Oauth_Credentials() {
        String response = given().log().all()
                .param("access_token", auth_token)
                .header("Content-Type", "application/json")
                .get("/api/v1/oauth/credentials")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/credentials.json"))
                .extract().response().asString();

        JsonPath js = new JsonPath(response);
        String id = js.getString("data.user.id");
        sg_id = id;
    }

    @Test(priority = 6)
    public void Update_Profile() {
        String name = "kiia";
        int gender = 1;
        String birthday = "1998-10-12";
        String hometown = "pati";
        String bio = "life must go on";
        String gendertype;

        JSONObject bodyJson = new JSONObject();

        bodyJson.put("name", name);
        bodyJson.put("gender", gender);
        bodyJson.put("birthday", birthday);
        bodyJson.put("hometown", hometown);
        bodyJson.put("bio", bio);

        if (gender == 1) {
            gendertype = "female";
        }

        else {
            gendertype = "male";
        }

        given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/profile")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user.id", Matchers.equalTo(sg_id))
                .body("data.user.name", Matchers.equalTo(name))
                .body("data.user.gender", Matchers.equalTo(gendertype))
                .body("data.user.birthday", Matchers.equalTo(birthday))
                .body("data.user.hometown", Matchers.equalTo(hometown))
                .body("data.user.bio", Matchers.equalTo(bio))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/update_profile.json"));
    }

    @Test(priority = 7)
    public void Profile_Edu() {
        String school_name = "banat";
        String graduation_time = "2016-06-12";

        JSONObject bodyJson = new JSONObject();

        bodyJson.put("school_name", school_name);
        bodyJson.put("graduation_time", graduation_time);

        given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/profile/education")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user.education.school_name", Matchers.equalTo(school_name))
                .body("data.user.education.graduation_time", Matchers.equalTo(graduation_time))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/profile_edu.json"));
        ;
    }

    @Test(priority = 8)
    public void Profile_Career() {
        String position = "quality assurance";
        String company_name = "privy";
        String starting_from = "2016-06-10";
        String ending_in = "2019-06-10";

        JSONObject bodyJson = new JSONObject();

        bodyJson.put("position", position);
        bodyJson.put("company_name", company_name);
        bodyJson.put("starting_from", starting_from);
        bodyJson.put("ending_in", ending_in);

        given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/profile/career")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user.career.company_name", Matchers.equalTo(company_name))
                .body("data.user.career.starting_from", Matchers.equalTo(starting_from))
                .body("data.user.career.ending_in", Matchers.equalTo(ending_in))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/profile_career.json"));
    }

    @Test(priority = 9)
    public void Upload_Cover() {
        String response = given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Content-Type", "multipart/form-data")
                .multiPart("image", new File(System.getProperty("user.dir") + "/src/image1.jpg"))
                .post("/api/v1/uploads/cover")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user_picture.id", Matchers.notNullValue())
                .body("data.user_picture.cover_picture.url", Matchers.notNullValue())
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/upload_cover.json"))
                .extract().response().asString();

        JsonPath js = new JsonPath(response);
        String pro_id = js.getString("data.user_picture.id");
        cover_id = pro_id;
        System.out.println(cover_id);
    }

    @Test(priority = 10)
    public void Upload_Profile() {
        String response = given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Content-Type", "multipart/form-data")
                .multiPart("image", new File(System.getProperty("user.dir") + "/src/profile.jpg"))
                .post("/api/v1/uploads/profile")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data.user_picture.id", Matchers.notNullValue())
                .body("data.user_picture.picture.url", Matchers.notNullValue())
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/upload_profile.json"))
                .extract().response().asString();

        JsonPath js = new JsonPath(response);
        String pro_id = js.getString("data.user_picture.id");
        profil_id = pro_id;
        System.out.println(profil_id);
    }

    @Test(priority = 11)
    public void Upload_Default() {
        String default_id = profil_id;

        JSONObject bodyJson = new JSONObject();

        bodyJson.put("id", default_id);

        given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/uploads/profile/default")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data", Matchers.containsString(default_id))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/upload_default.json"));
    }

    @Test(priority = 12)
    public void Profile_Me() {
        given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Content-Type", "application/json")
                .get("/api/v1/profile/me")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("data.user.id", Matchers.equalTo(sg_id))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/profile_me.json"));
    }

    @Test(priority = 13)
    public void Message_Send() {
        String message = "Hi, There!!!";

        JSONObject bodyJson = new JSONObject();

        bodyJson.put("user_id", sg_id);
        bodyJson.put("message", message);

        given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/message/send")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data", Matchers.containsString("Success"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/send_message.json"));
    }

    @Test(priority = 14)
    public void Message_Get() {
        given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .header("Content-Type", "application/json")
                .get("/api/v1/message/" + user_id)
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/get_message.json"));
        ;
    }

    @Test(priority = 15)
    public void Notification() {
        String grub_id = "1";
        given().log().all()
                .header("Authorization", "Bearer " + auth_token)
                .post("/api/v1/notification/" + grub_id + "/" + auth_token)
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/notification.json"));
    }

    @Test(priority = 16)
    public void Delete_Profile() {
        given().log().all()
                .param("id", user_id)
                .header("Authorization", "Bearer " + auth_token)
                .header("Content-Type", "application/json")
                .post("/api/v1/uploads/profile")
                .then().log().all()
                .assertThat()
                // here i put 500 as expected status code, because the server is still error
                // .statusCode(500)
                // if the server was running successfully, the expected status code must be 204
                .statusCode(204);

    }

    @Test(priority = 17)
    public void Oauth_Revoke() {
        given().log().all()
                .param("access_token", auth_token)
                .param("confirm", "1")
                .header("Content-Type", "application/json")
                .post("/api/v1/oauth/revoke")
                .then().log().all()
                .assertThat()
                // here i put 500 as expected status code, because the server is still error
                // .statusCode(500)
                // if the server was running successfully, the expected status code must be 201
                .statusCode(201);
    }

    @Test(priority = 18)
    public void Register_Remove() {
        JSONObject bodyJson = new JSONObject();

        bodyJson.put("phone", phone);

        given().log().all()
                .header("Content-Type", "application/json")
                .body(bodyJson.toString())
                .post("/api/v1/register/remove")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("data", Matchers.equalTo("Success remove user " + phone))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schema/remove_register.json"));
    }
}
