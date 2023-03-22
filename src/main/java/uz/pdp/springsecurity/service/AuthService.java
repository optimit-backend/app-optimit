package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.repository.RoleRepository;
import uz.pdp.springsecurity.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class  AuthService implements UserDetailsService {
    @Autowired
   private final UserRepository userRepository;

//    @Autowired
//    PasswordEncoder passwordEncoder;
//
//    @Autowired
//    RoleRepository roleRepository;




    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("USER NOT FOUND"));
    }
}
