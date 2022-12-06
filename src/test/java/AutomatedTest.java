import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import static io.restassured.RestAssured.*;

    public class AutomatedTest {
        String phone = "626200626111";
        String password = "password";
        String latlong = "84567890";
        String device_token = "12345";
        int device_type = 2;
        String otp = "9672";
        String user_id = "";
        String auth_token = "";
        String sg_id = "";

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

            given().header("Content-Type", "application/json")
                    .body(bodyJson.toString())
                    .post("/api/v1/register")
                    .then().log().all()
                    .assertThat()
                    .statusCode(201)
                    .body("data.user.phone", Matchers.equalTo(phone));
        }

        @Test(priority = 2)
        public void Request_OTP() {
            JSONObject bodyJson = new JSONObject();

            bodyJson.put("phone", phone);

            String response = given().log().all()
                    .header("Content-Type", "application/json")
                    .body(bodyJson.toString()).when()
                    .post("/api/v1/register/otp/request")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data.user.phone", Matchers.equalTo(phone))
                    .extract().response().asString();

            JsonPath js = new JsonPath(response);
            String id = js.getString("id");
            user_id = id;
        }

        @Test(priority = 3)
        public void Match_OTP() {
            JSONObject bodyJson = new JSONObject();

            bodyJson.put("user_id", user_id);
            bodyJson.put("otp_code", otp);

            String response = given().log().all()
                    .header("Content-Type", "application/json")
                    .body(bodyJson.toString())
                    .post("/api/v1/register/otp/request")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data.user.token_type", Matchers.equalTo("bearer"))
                    .extract().response().asString();
            // JsonPath js = new JsonPath(response);
            // String token = js.getString("access_token");
            // auth_token = token;
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
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data.user.token_type", Matchers.equalTo("bearer"))
                    .extract().response().asString();

            JsonPath js = new JsonPath(response);
            String token = js.getString("access_token");
            auth_token = token;
        }

        @Test(priority = 5)
        public void Oauth_Credentials() {
            String response = given().log().all()
                    .param("access_token", auth_token)
                    .header("Content-Type", "application/json")
                    .get("/api/v1/oauth/credentials")
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .extract().response().asString();

            JsonPath js = new JsonPath(response);
            String id = js.getString("id");
            sg_id = id;
        }

        @Test(priority = 6)
        public void Update_Profile() {
            String name = "kiia";
            int gender = 1;
            String birthday = "1998/10/12";
            String hometown = "pati";
            String bio = "life must go on";

            JSONObject bodyJson = new JSONObject();

            bodyJson.put("name", name);
            bodyJson.put("gender", gender);
            bodyJson.put("birthday", birthday);
            bodyJson.put("hometown", hometown);
            bodyJson.put("bio", bio);

            String response = given().log().all()
                    .param("access_token", auth_token)
                    .header("Content-Type", "application/json")
                    .post("/api/v1/profile")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data.user.id", Matchers.equalTo(sg_id))
                    .body("data.user.name", Matchers.equalTo(name))
                    .body("data.user.gender", Matchers.equalTo(gender))
                    .body("data.user.birthday", Matchers.equalTo(birthday))
                    .body("data.user.hometown", Matchers.equalTo(hometown))
                    .body("data.user.bio", Matchers.equalTo(bio))
                    .extract().response().asString();
        }

        @Test(priority = 7)
        public void Profile_Edu() {
            String school_name = "banat";
            String graduation_time = "2016/06/01";

            JSONObject bodyJson = new JSONObject();

            bodyJson.put("school_name", school_name);
            bodyJson.put("graduation_time", graduation_time);

            String response = given().log().all()
                    .header ("Authorization", "Bearer " + auth_token)
                    .header("Content-Type", "application/json")
                    .post("/api/v1/profile/education")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data.user.school_name", Matchers.equalTo(school_name))
                    .body("data.user.graduation_time", Matchers.equalTo(graduation_time))
                    .extract().response().asString();
        }

        @Test(priority = 8)
        public void Profile_Career() {
            String position = "banat";
            String company_name = "";
            String starting_from = "2016/06/01";
            String ending_in = "2018/03/02";

            JSONObject bodyJson = new JSONObject();

            bodyJson.put("position", position);
            bodyJson.put("company_name", company_name);
            bodyJson.put("starting_from", starting_from);
            bodyJson.put("ending_in", ending_in);

            String response = given().log().all()
                    .header ("Authorization", "Bearer " + auth_token)
                    .header("Content-Type", "application/json")
                    .post("/api/v1/profile/career")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data.user.position", Matchers.equalTo(position))
                    .body("data.user.company_name", Matchers.equalTo(company_name))
                    .body("data.user.starting_from", Matchers.equalTo(starting_from))
                    .body("data.user.ending_in", Matchers.equalTo(ending_in))
                    .extract().response().asString();
        }

        @Test(priority = 9)
        public void Upload_Cover() {

        }

        @Test(priority = 10)
        public void Upload_Profile() {

        }

        @Test(priority = 11)
        public void Upload_Default() {
            // id nya pake yg atas
            String id = "id";

            JSONObject bodyJson = new JSONObject();

            bodyJson.put("id", id);

            String response = given().log().all()
                    .header ("Authorization", "Bearer " + auth_token)
                    .header("Content-Type", "application/json")
                    .post("/api/v1/uploads/profile/default")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data", Matchers.containsString(id))
                    .extract().response().asString();
        }

        // validate smua
        @Test(priority = 12)
        public void Profile_Me() {
            String response = given().log().all()
                    .header ("Authorization", "Bearer " + auth_token)
                    .header("Content-Type", "application/json")
                    .get("/api/v1/profile/me")
                    .then()
                    .assertThat()
                    .statusCode(200)
                    // belum fix
                    .body("data.user.id", Matchers.containsString("id"))
                    .extract().response().asString();
        }

        @Test(priority = 13)
        public void Message_Send() {
            String message = "Hi, There!!!";

            JSONObject bodyJson = new JSONObject();

            bodyJson.put("user_id", sg_id);
            bodyJson.put("message", message);

            String response = given().log().all()
                    .header ("Authorization", "Bearer " + auth_token)
                    .header("Content-Type", "application/json")
                    .post("/api/v1/message/send")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data", Matchers.containsString("Success"))
                    .extract().response().asString();
        }

        @Test(priority = 14)
        public void Message_Get() {
            String response = given().log().all()
                    .header ("Authorization", "Bearer " + auth_token)
                    .header("Content-Type", "application/json")
                    .get("/api/v1/message/" + user_id)
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .extract().response().asString();
        }

        @Test(priority = 15)
        public void Notification() {
            String grub_id = "1";
            String response = given().log().all()
                    .header ("Authorization", "Bearer " + auth_token)
                    .header("Content-Type", "application/json")
                    .post("/api/v1/notification/" + grub_id + "/" + auth_token)
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body(Matchers.equalTo("success"))
                    .extract().response().asString();
        }

        @Test(priority = 16)
        public void Delete_Profile() {
            String response = given().log().all()
                    .param("id", user_id)
                    .header ("Authorization", "Bearer " + auth_token)
                    .header("Content-Type", "application/json")
                    .post("/api/v1/uploads/profile")
                    .then()
                    .assertThat()
                    .statusCode(204)
                    .extract().response().asString();
        }

        @Test(priority = 17)
        public void Oauth_Revoke() {
            String response = given().log().all()
                    .param("access_token", auth_token)
                    .param("confirm", "1")
                    .header("Content-Type", "application/json")
                    .post("/api/v1/oauth/revoke")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .extract().response().asString();
        }

        @Test(priority = 18)
        public void Register_Remove() {
            JSONObject bodyJson = new JSONObject();

            bodyJson.put("phone", phone);

            String response = given().log().all()
                    .header("Content-Type", "application/json")
                    .post("/api/v1/register/remove")
                    .then()
                    .assertThat()
                    .statusCode(201)
                    .body("data", Matchers.containsString("Success"))
                    .body("data", Matchers.containsString(phone))
                    .extract().response().asString();
        }
}
