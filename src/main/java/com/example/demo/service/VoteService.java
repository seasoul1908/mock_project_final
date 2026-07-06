package com.example.demo.service;

public interface VoteService {

    void voteQuestion(long questionId, long userId, String voteType);

    void voteAnswer(long answerId, long userId, String voteType);
}
