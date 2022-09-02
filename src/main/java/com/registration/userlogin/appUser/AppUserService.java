package com.registration.userlogin.appUser;

import com.registration.userlogin.registration.token.ConfirmationToken;
import com.registration.userlogin.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepo appUserRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    private final static String USER_NOT_FOUND_MESG = "user with email %s not found";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(
                String.format(USER_NOT_FOUND_MESG,email)));
    }

    public String SignUpUser(AppUser user){
        Boolean userExists = appUserRepo.findByEmail(user.getEmail()).isPresent();
        if(userExists){
            throw new IllegalStateException(
                    "email already taken"
            );
        }
        //encoding password
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        //setting the new password as the encoded password
        user.setPassword(encodedPassword);
        //saving the new encoded password in database
        appUserRepo.save(user);
        //generating a confirmation token when a user signsUp
         String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),user);
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        //TODO: send email
        return token;

    }

    public int enableAppUser(String email) {
        return appUserRepo.enableAppUser(email);
    }
}
