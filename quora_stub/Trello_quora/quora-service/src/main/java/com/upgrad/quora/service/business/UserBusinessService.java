package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;


    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticate(final String username, final String password) throws AuthenticationFailedException {

        UserEntity userEntity = userDao.getUserByUsername(username);
        System.out.println("Testing "+userEntity.getUserName());
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATHR-001", "This username does not exist");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthEntity userAuthToken = new UserAuthEntity();
            userAuthToken.setUser(userEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthToken.setLoginAt(now);
            userAuthToken.setExpiresAt(expiresAt);


            userDao.createAuthToken(userAuthToken);
            userDao.updateUser(userEntity);


            return userAuthToken;
        } else {
            throw new AuthenticationFailedException("ATHR-002", "Password Failed");
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) {
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);
    }

    public void verifyUserExistance(UserEntity userEntity) throws SignUpRestrictedException {
        UserEntity userEmail = userDao.getUserByEmail(userEntity.getEmail());
        UserEntity user = userDao.getUserByUsername(userEntity.getUserName());
        if (null != userEmail && userEntity.getEmail().equals(userEmail.getEmail())) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        } else if (null != user && userEntity.getUserName().equals(user.getUserName())) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }

    }

    public UserAuthEntity userSignOutValidation(String accessTokenFromRequest) throws SignOutRestrictedException {


        UserAuthEntity userAuthToken = userDao.updateLoggedAtTime(accessTokenFromRequest);
        if(userAuthToken == null){

            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }
        else
        {
            return  userAuthToken;
        }

    }


}
