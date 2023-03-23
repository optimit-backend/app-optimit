package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Value("${stripe.apikey}")
    String strapiKey;

    @RequestMapping
    public String index(){
        return "hello --->>>>  " + strapiKey;
    }

}
