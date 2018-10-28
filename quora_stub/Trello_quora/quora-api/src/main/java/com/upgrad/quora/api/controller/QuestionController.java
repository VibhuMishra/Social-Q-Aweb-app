package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
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
public class QuestionController {

    @Autowired
    private QuestionBusinessService questionBusinessService;



    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest,
                                                   @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

            UserEntity userEntity =  questionBusinessService.createQuestionValidation(authorization);
            final QuestionEntity questionEntity = new QuestionEntity();
            questionEntity.setUuid(UUID.randomUUID().toString());
            questionEntity.setContent(questionRequest.getContent());
            questionEntity.setDate(ZonedDateTime.now());
            questionEntity.setUser(userEntity);
            final QuestionEntity createdQuestionEntity = questionBusinessService.createQuestion(questionEntity);
            QuestionResponse userResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("QUESTION CREATED");
            return new ResponseEntity<QuestionResponse>(userResponse, HttpStatus.CREATED);


    }

    @RequestMapping(method = RequestMethod.GET, path = "/question/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionByUser(@PathVariable("userId") final String userUuid,
                                                           @RequestHeader("authorization") final String accessToken) throws  AuthorizationFailedException, UserNotFoundException {
        final List<QuestionEntity> quesEntity = questionBusinessService.getAllQuestionByUser(userUuid, accessToken);
        List<QuestionDetailsResponse> questionDetailsResponse = new ArrayList<QuestionDetailsResponse>();
        for(QuestionEntity questionEntity:quesEntity)
        {
            questionDetailsResponse.add(new QuestionDetailsResponse().id(questionEntity.getUuid())
                    .content(questionEntity.getContent()));
        }


        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion (@PathVariable("questionId") final String questionId,
                                                                        @RequestHeader("authorization") final String accessToken) throws AuthorizationFailedException, UserNotFoundException, InvalidQuestionException {

        UserEntity userEntity =  questionBusinessService.deleteQuestionValidation(questionId,accessToken);
        QuestionEntity questionEntity = questionBusinessService.getQuestionByQuestionId(questionId);

        final QuestionEntity deletedQuestionEntity = questionBusinessService.deleteQuestion(questionEntity);
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse().id(deletedQuestionEntity.getUuid()).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException{
        final List<QuestionEntity> quesEntity = questionBusinessService.getAllQuestion(authorization);
        List<QuestionDetailsResponse> questionDetailsResponse = new ArrayList<QuestionDetailsResponse>();
        for(QuestionEntity questionEntity:quesEntity)
        {
            questionDetailsResponse.add(new QuestionDetailsResponse().id(questionEntity.getUuid())
                    .content(questionEntity.getContent()));
        }


        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{uuid}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> editQuestion(final QuestionEditRequest questionEditRequest, @PathVariable("uuid") String uuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {

        questionBusinessService.validateQuestionEditRequest(uuid,authorization);

        questionBusinessService.updateQuestion(uuid,questionEditRequest.getContent());

        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(uuid).status("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }
}
