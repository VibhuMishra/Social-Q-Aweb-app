package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    private AnswerBusinessService answerBusinessService;

    @Autowired
    private QuestionBusinessService questionBusinessService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createQuestion(final AnswerRequest answerRequest,
                                                         @PathVariable("questionId") final String questionId,
                                                           @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException, InvalidQuestionException {

        UserEntity userEntity =  answerBusinessService.createAnswerValidation(authorization);
        QuestionEntity questionEntity = answerBusinessService.getAllQuestionByQuestionID(questionId,authorization);
        final AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setUser(userEntity);
        answerEntity.setQuestion(questionEntity);
        final AnswerEntity createdAnswerEntity = answerBusinessService.createAnswer(answerEntity);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswerEntity.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);


    }

    @RequestMapping(method = RequestMethod.GET, path = "answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion (@PathVariable("questionId") final String questionId,
                                                                                @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException, InvalidQuestionException {
        UserEntity userEntity =  answerBusinessService.createAnswerValidation(authorization);
        QuestionEntity questionEntity = answerBusinessService.getAllQuestionByQuestionID(questionId,authorization);
        final List<AnswerEntity> ansEntity = answerBusinessService.getAllAnswersToQuestion(questionId, authorization);
        List<AnswerDetailsResponse> answerDetailsResponse = new ArrayList<AnswerDetailsResponse>();
        for(AnswerEntity answerEntity:ansEntity)
        {
            answerDetailsResponse.add(new AnswerDetailsResponse().id(answerEntity.getUuid())
                    .questionContent(answerEntity.getQuestion().getContent()).answerContent(answerEntity.getAns()));
        }


        return new ResponseEntity<List<AnswerDetailsResponse>>(answerDetailsResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer (@PathVariable("answerId") final String answerId,
                                                              @RequestHeader("authorization") final String accessToken) throws AuthorizationFailedException, UserNotFoundException, InvalidQuestionException, AnswerNotFoundException {

        UserEntity userEntity =  answerBusinessService.deleteAnswerValidation(answerId,accessToken);
        AnswerEntity answerEntity = answerBusinessService.getAnswerByAnswerId(answerId);

        final AnswerEntity deletedAnswerEntity = answerBusinessService.deleteAnswer(answerEntity);
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(deletedAnswerEntity.getUuid()).status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{uuid}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> editQuestion(final AnswerEditRequest answerEditRequest, @PathVariable("uuid") String uuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        answerBusinessService.validateAnswerEditRequest(uuid, authorization);
        answerBusinessService.updateAnswer(uuid,answerEditRequest.getContent());
        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(uuid).status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }
}
