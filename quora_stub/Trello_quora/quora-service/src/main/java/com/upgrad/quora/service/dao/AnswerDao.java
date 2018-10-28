package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDao {
    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity createAnswer(AnswerEntity answerEntity) {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    public List<AnswerEntity> getAllAnswersToQuestion(final long questionId) {
        try {
            return entityManager.createNamedQuery("allAnswersToQuestion", AnswerEntity.class).setParameter("questionId", questionId).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public AnswerEntity getAnswerByAnswerId(final String answerId) {
        try {
            return entityManager.createNamedQuery("answerByAnswerId", AnswerEntity.class).setParameter("answerId", answerId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public AnswerEntity deleteAnswer(AnswerEntity answerEntity) {
        entityManager.remove(answerEntity);
        return answerEntity;
    }


    public void updateAnswer(final AnswerEntity qe) {
        //entityManager.persist(qe);
        entityManager.merge(qe);
    }
}

