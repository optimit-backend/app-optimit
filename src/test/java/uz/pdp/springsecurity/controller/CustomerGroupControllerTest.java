package uz.pdp.springsecurity.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.pdp.springsecurity.service.CustomerGroupService;

@WebMvcTest(CustomerGroupController.class)
class CustomerGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerGroupService customerGroupService;



    @Test
    void addCustomerGroup() {

    }
}