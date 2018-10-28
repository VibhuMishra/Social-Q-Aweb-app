package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnswerBusinessService {

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

    public UserEntity createAnswerValidation(final String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);
        QuestionEntity questionEntity = new QuestionEntity();
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
    public AnswerEntity createAnswer(final AnswerEntity answerEntity) {

        return answerDao.createAnswer(answerEntity);

    }

    public QuestionEntity getAllQuestionByQuestionID(final String questionId, final String accessToken) throws AuthorizationFailedException, UserNotFoundException, InvalidQuestionException {
        QuestionEntity questionEntity = questionDao.getAllQuestionByQuestionID(questionId);
        if(questionEntity == null)
        {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        return questionEntity;
    }

    public List<AnswerEntity> getAllAnswersToQuestion(final String questionId, final String accessToken) throws AuthorizationFailedException, UserNotFoundException, InvalidQuestionException {
        QuestionEntity questionEntity = questionDao.getAllQuestionByQuestionID(questionId);

        List<AnswerEntity> answerEntityList = answerDao.getAllAnswersToQuestion(questionEntity.getId());

        return answerEntityList;
    }

    public UserEntity deleteAnswerValidation(final String answerId,final String authorization) throws AuthorizationFailedException, InvalidQuestionException, AnswerNotFoundException {
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
                AnswerEntity answerEntity = getAnswerByAnswerId(answerId);
                if((userEntity.getId() != (answerEntity.getUser().getId())) || (!userEntity.getRole().equalsIgnoreCase("admin")))
                {
                    throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
                }
                return  userEntity;
            }

        }
    }

    public AnswerEntity getAnswerByAnswerId(final String answerId) throws InvalidQuestionException, AnswerNotFoundException {
        AnswerEntity answerEntity = answerDao.getAnswerByAnswerId(answerId);
        if(answerEntity == null)
        {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        return answerEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final AnswerEntity answerEntity) {

        return answerDao.deleteAnswer(answerEntity);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateAnswer(String uuid, String content ) {
        AnswerEntity ae = answerDao.getAnswerByAnswerId(uuid);
        ae.setAns(content);

        answerDao.updateAnswer(ae);
    }

    public AnswerEntity validateAnswerEditRequest(final String uuid, final String accessToken) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        AnswerEntity answerEntity = answerDao.getAnswerByAnswerId(uuid);
        if(userAuthEntity == null){
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }else{
            UserEntity userEntity = userDao.getUser(userAuthEntity.getUuid());

            if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLogoutAt().isBefore(userAuthEntity.getExpiresAt()))
            {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit and answer");
            }
            else if (answerEntity == null){
                throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
            }
            else if((userEntity.getId() != (answerEntity.getUser().getId())) || (!userEntity.getRole().equalsIgnoreCase("admin")))
            {
                throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
            }else{
                return answerEntity;
            }
        }
    }

}
