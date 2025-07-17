package com.danialrekhman.userservice.security;

import com.danialrekhman.userservice.model.User;
import com.danialrekhman.userservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if(!userRepository.existsByEmail(email) || email == null || email.isBlank())
            throw new UsernameNotFoundException("User not found");
        User user = userRepository.findByEmail(email);
        return new MyUserDetails(user);
    }
}
