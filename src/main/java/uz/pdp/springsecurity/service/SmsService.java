package uz.pdp.springsecurity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.*;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.SmsDto;
import uz.pdp.springsecurity.repository.CustomerRepository;
import uz.pdp.springsecurity.repository.ShablonRepository;
import uz.pdp.springsecurity.repository.TokenRepository;
import uz.pdp.springsecurity.repository.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SmsService {


    private static final String userEmail = "dexqonchlik@gmail.com";
    private final static String userSecret = "aJizjDBiquoHydCkOj2bf7Yw0lNV1GUTeYJeGnWJ";
    private static final String GET_TOKEN = "https://notify.eskiz.uz/api/auth/login";

    private final ShablonRepository shablonRepository;

    private final TokenRepository tokenRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;


    public ApiResponse add(SmsDto smsDto) {

        List<String> phoneNumberList = new ArrayList<>();
        if (smsDto.getKey().equals("CUSTOMERS")) {
            List<UUID> list = smsDto.getIdList();
            for (UUID id : list) {
                Optional<Customer> optionalCustomer = customerRepository.findById(id);
                if (optionalCustomer.isPresent()) {
                    Customer customer = optionalCustomer.get();
                    phoneNumberList.add(customer.getPhoneNumber());
                }
            }
        } else if (smsDto.getKey().equals("EMPLOYEE")) {
            List<UUID> list = smsDto.getIdList();
            for (UUID id : list) {
                Optional<User> optionalUser = userRepository.findById(id);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    phoneNumberList.add(user.getPhoneNumber());
                }
            }
        }
        String smsToken = null;
        List<Token> all = tokenRepository.findAll();
        for (Token token : all) {
            smsToken = token.getToken();
            break;
        }
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        String message = "";
        if (smsDto.getMessage() != null) {
            message = smsDto.getMessage();
        } else {
            Optional<Shablon> optionalShablon =
                    shablonRepository.findById(smsDto.getShablonId());
            if (optionalShablon.isEmpty()) {
                message = smsDto.getMessage();
            } else {
                message = optionalShablon.get().getMessage();
            }
        }
        if (!phoneNumberList.isEmpty()) {
            String sendMessage = "";
            for (String phoneNumber : phoneNumberList) {
                if (smsDto.getKey().equals("CUSTOMERS")) {
                    Optional<Customer> optionalCustomer = customerRepository.findByPhoneNumber(phoneNumber);
                    if (optionalCustomer.isPresent()) {
                        Customer customer = optionalCustomer.get();
                        int start = message.indexOf("{");
                        int end = message.indexOf("}");
                        if (start >= 0 && end > 0) {
                            String substring = message.substring(0, start - 1);
                            substring = substring + " " + customer.getName() + " ";
                            String substring1 = message.substring(end + 1);
                            substring = substring + substring1;
                            sendMessage = substring;
                        }else {
                            sendMessage = message;
                        }
                    }
                } else if (smsDto.getKey().equals("EMPLOYEE")) {
                    Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();
                        int start = message.indexOf("{");
                        int end = message.indexOf("}");
                        if (start >= 0 && end > 0) {
                            String substring = message.substring(0, start - 1);
                            substring = substring + " " + user.getFirstName() + " " + user.getFirstName() + " ";
                            String substring1 = message.substring(end + 1);
                            substring = substring + substring1;
                            sendMessage = substring;
                        } else {
                            sendMessage = message;
                        }
                    }
                }

                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("mobile_phone", phoneNumber)
                        .addFormDataPart("message", sendMessage)
                        .addFormDataPart("from", "4546")
                        .addFormDataPart("callback_url", "http://0000.uz/test.php")
                        .build();

                if (smsToken == null) {
                    smsToken = getGetToken();
                }

                Request request = new Request.Builder()
                        .url("https://notify.eskiz.uz/api/message/sms/send")
                        .method("POST", body)
                        .header("Authorization", "Bearer " + smsToken)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    ResponseBody responseBody = response.body();
                    assert responseBody != null;
                    String string = responseBody.string();
                    String ERROR_INVALID_TOKEN = "{\"status\":\"token-invalid\",";
                    if (string.startsWith(ERROR_INVALID_TOKEN)) {
                        getGetToken();
                        add(smsDto);
                    }
                } catch (Exception e) {
                    return new ApiResponse("Iltimos keyinroq qaytadan o'rinib ko'ring", false);
                }
            }
        }
        return new ApiResponse("successfully send", true);
    }


    public String getGetToken() {
        ObjectMapper objectMapper = new ObjectMapper();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("email", userEmail)
                .addFormDataPart("password", userSecret)
                .build();
        Request request = new Request.Builder()
                .url(GET_TOKEN)
                .method("POST", body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            assert responseBody != null;
            String string = responseBody.string();
            SmsToken smsToken = objectMapper.readValue(string, SmsToken.class);
            Token build = Token.builder().token(smsToken.getData().getToken()).build();

            Token token = new Token();
            List<Token> all = tokenRepository.findAll();
            if (all.isEmpty()) {
                token = tokenRepository.save(build);
            } else {
                tokenRepository.deleteAll();
                token = tokenRepository.save(build);
            }
            System.out.println(token.getToken());
            return token.getToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
