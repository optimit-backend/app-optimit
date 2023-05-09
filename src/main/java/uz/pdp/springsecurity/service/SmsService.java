package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.pdp.springsecurity.entity.SmsToken;
import uz.pdp.springsecurity.entity.Token;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.SmsDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class SmsService {


    private final RestTemplate restTemplate;
    private static final String userEmail = "samandarshodmonov1998@gmail.com";
    private final static String userSecret = "TaEotaLMYaUqqGTgTrzjGnmIFo7whQfaf82vMU8o";
    private static final String GET_TOKEN = "https://notify.eskiz.uz/api/auth/login";
    public ApiResponse add(SmsDto smsDto) {

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("mobile_phone", smsDto.getMobilePhone())
                .addFormDataPart("message", smsDto.getMessage())
                .addFormDataPart("from", "4546")
                .addFormDataPart("callback_url", "http://0000.uz/test.php")
                .build();
        Request request = new Request.Builder()
                .url("https://notify.eskiz.uz/api/message/sms/send")
                .method("POST", body)
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjM5NDcsInJvbGUiOm52bGwsImRhdGEiOnsiaWQiOjM5NDcsIm5hbWUiOiJTaG9kbW9ub3YgU2FtYW5kYXIgU2FsaW1qb24gbydnJ2xpIiwiZW1haWwiOiJzYW1hbmRhcnNob2Rtb25vdjE5OThAZ21haWwuY29tIiwicm9sZSI6bnVsbCwiYXBpX3Rva2VuIjpudWxsLCJzdGF0dXMiOiJhY3RpdmUiLCJzbXNfYXBpX2xvZ2luIjoiZXNraXoyIiwic21zX2FwaV9wYXNzd29yZCI6ImUkJGsheiIsInV6X3ByaWNlIjo1MCwidWNlbGxfcHJpY2UiOjExNSwidGVzdF91Y2VsbF9wcmljZSI6bnVsbCwiYmFsYW5jZSI6NDI1MCwiaXNfdmlwIjowLCJob3N0Ijoic2VydmVyMSIsImNyZWF0ZWRfYXQiOiIyMDIzLTA1LTAyVDEyOjU4OjQ5LjAwMDAwMFoiLCJ1cGRhdGVkX2F0IjoiMjAyMy0wNS0wM1QxNTo0NjowNi4wMDAwMDBaIiwid2hpdGVsaXN0IjpudWxsfSwiaWF0IjoxNjgzNjM3ODA2LCJleHAiOjE2ODYyMjk4MDZ9.b1wGbFIgjYpqWxaNoGN0nhWNbMcl9A7pMSbZJoaDodg")
                .build();
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            assert responseBody != null;
            String string = responseBody.string();
            String ERROR_INVALID_TOKEN = "{\"status\":\"token-invalid\",";
            if (string.startsWith(ERROR_INVALID_TOKEN)) {
                return new ApiResponse("token eskirgan", false);
            }
            String ERROR_PHONE_NUMBER = "{\"status\":\"error\",\"message\":{\"mobile_phone\":[\"The mobile phone must be 12 digits.\"]}}";
            if (string.equals(ERROR_PHONE_NUMBER)) {
                return new ApiResponse("error 12 talik raqam kiriting");
            }
            return new ApiResponse("successfully send", true, response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    public String getToken() {
//        try {
//            Map<String, String> requestBody = new HashMap<>();
//            requestBody.put("email", userEmail);
//            requestBody.put("password", userSecret);
//            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody);
//            ResponseEntity<String> response = restTemplate.postForEntity(GET_TOKEN, request, String.class);
//            String body = response.getBody();
//            SmsToken smsToken = objectMapper.readValue(body, SmsToken.class);
//            Token build = Token.builder().token(smsToken.getData().getToken()).build();
//            Token token = new Token();
//            List<Token> all = tokenRepository.findAll();
//            if (all.isEmpty()) {
//                token = tokenRepository.save(build);
//            } else {
//                token.setId( all.get(0).getId());
//                token.setToken(build.getToken());
//                token = tokenRepository.save(token);
//            }
//            return token.getToken();
//        } catch (Exception e) {
//            throw new SmsServiceBroken(CAN_NOT_TAKE_SMS_SENDING_SERVICE_TOKEN);
//        }
//    }
}
