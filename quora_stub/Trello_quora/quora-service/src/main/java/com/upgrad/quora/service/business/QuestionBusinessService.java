package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Service
public class QuestionBusinessService {


    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserDao userDao;




    public List<QuestionEntity> getAllQuestionByUser(final String uuid,final String accessToken) throws AuthorizationFailedException, UserNotFoundException {
        UserEntity userEntity = userDao.getUser(uuid);
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        if(userAuthEntity == null)
        {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else
        {
            if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(userAuthEntity.getExpiresAt()) )
            {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");

            }
            if(userEntity == null)
            {
                throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");

            }
            questionDao.getAllQuestionByUser(userAuthEntity.getUser().getId());
        }
        List<QuestionEntity> questionEntity = questionDao.getAllQuestionByUser(userEntity.getId());
        return questionEntity;
    }

    public List<QuestionEntity> getAllQuestion(final String authorization) throws AuthorizationFailedException{
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);

        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else{
            UserEntity userEntity = userDao.getUser(userAuthEntity.getUuid());
            if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(userAuthEntity.getExpiresAt()) )
            {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");

            }
            List<QuestionEntity> questionEntity = questionDao.getAllQuestions();
            return questionEntity;
        }
    }

    public void getUserDetails(final String authorization) throws AuthorizationFailedException{
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);

        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else{

            if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(userAuthEntity.getExpiresAt()) )
            {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");

            }


        }
    }

    public UserEntity createQuestionValidation(final String authorization) throws AuthorizationFailedException{
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);

        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else{

              if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(userAuthEntity.getExpiresAt()) )
              {
                  throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");

              }
              else
              {
                  UserEntity userEntity = userDao.getUser(userAuthEntity.getUuid());
                  return  userEntity;
              }

        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity) {

        return questionDao.createQuestion(questionEntity);

    }

    public QuestionEntity getQuestionByQuestionId(final String questionId) throws InvalidQuestionException {
        QuestionEntity questionEntity = questionDao.getAllQuestionByQuestionID(questionId);
        if(questionEntity == null)
        {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        return questionEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(final QuestionEntity questionEntity) {

        return questionDao.deleteQuestion(questionEntity);

    }

    public UserEntity deleteQuestionValidation(final String questionId,final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);

        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else{

            if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(userAuthEntity.getExpiresAt()) )
            {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");

            }
            else
            {
                UserEntity userEntity = userDao.getUser(userAuthEntity.getUuid());
                QuestionEntity questionEntity = getQuestionByQuestionId(questionId);
                if((!userEntity.getId().equals(questionEntity.getUser_id())) && (!userEntity.getRole().equalsIgnoreCase("admin")))
                {
                    throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
                }
                return  userEntity;
            }

        }
    }


    public void updateQuestion(String uuid, String  content ) {
        QuestionEntity qe = questionDao.getAllQuestionByQuestionID(uuid);
        qe.setContent(content);
        //qe.setDate(new java.util.Date());
        questionDao.updateQuestion(qe);
    }

    public QuestionEntity validateQuestionEditRequest(final String uuid, final String accessToken) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        QuestionEntity questionEntity = questionDao.getAllQuestionByQuestionID(uuid);
        if(userAuthEntity == null){
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }else{
            if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(userAuthEntity.getExpiresAt()))
            {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
            }
            else if (questionEntity == null){
                throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
            }
            else if((userAuthEntity.getUser().getId() != (questionEntity.getUser().getId()))){
                throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the question");
            }else{
                return questionEntity;
            }
        }
    }
}
