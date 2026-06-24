package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Fetch user info from Google/GitHub
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Get provider (google or github)
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        // Extract basic info
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("name"); 
        
        if (username == null) {
            username = oAuth2User.getAttribute("login"); // Fallback for GitHub
        }

        // Handle private GitHub email
        if (email == null) {
            email = username + "@" + provider + ".com"; 
        }

        // Check DB existence
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            // Auto-register new social user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setProvider(provider.toUpperCase());
            newUser.setRole("member");
            newUser.setStatus("active");
            newUser.setPasswordHash(""); // No password needed
            
            userRepository.save(newUser);
        }

        return oAuth2User;
    }
}