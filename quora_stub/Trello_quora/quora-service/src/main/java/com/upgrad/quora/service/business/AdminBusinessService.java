package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class AdminBusinessService {

    @Autowired
    private UserDao userDao;

    public void userDeleteValidation(String userId, String  userAccessToken) throws AuthorizationFailedException, UserNotFoundException {
       UserAuthEntity userAuthEntity = userDao.getUserAuthToken(userAccessToken);
       UserEntity userEntity = userDao.getUser(userId);
       if(userAuthEntity == null)
       {
           throw new AuthorizationFailedException("ATHR-001","User has not signed in");
       }
       else {
           if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(userAuthEntity.getExpiresAt())){
               throw new AuthorizationFailedException("ATHR-002","User is signed out");
           }else if(userEntity == null){
               throw new UserNotFoundException("USR-001","User with entered uuid to be deleted does not exist");
           }else if (!userEntity.getRole().equalsIgnoreCase("admin"))
           {
                throw new AuthorizationFailedException("ATHR-003","Unauthorized Access, Entered user is not an admin");
           }
       }
    }

    public void deleteUser(String userAccessToken){
        UserAuthEntity uae = userDao.getUserAuthToken(userAccessToken);
        UserEntity ue = userDao.getUser(uae.getUuid());
        userDao.deleteUser(ue);
    }

}
