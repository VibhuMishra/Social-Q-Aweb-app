package com.upgrad.quora.service.entity;

import org.hibernate.annotations.OnDelete;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name="question")
@NamedQueries(
        {

               @NamedQuery(name = "allQuestionsByUser", query = "select q from QuestionEntity q where q.user_id =:userId"),
                @NamedQuery(name = "allQuestions", query = "select q from QuestionEntity q "),
                @NamedQuery(name = "allQuestionsByQuestionId", query = "select q from QuestionEntity q where q.uuid =:questionId")
        }
)
public class QuestionEntity implements Serializable{
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "UUID")
    @Size(max = 200)
    private String uuid;

    @Column(name = "content")
    @Size(max = 500)
    private String content;

    @Column(name = "date")
    private ZonedDateTime date;


    @Column(name = "USER_ID",updatable = false,insertable = false)
    private int user_id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }


    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

}
