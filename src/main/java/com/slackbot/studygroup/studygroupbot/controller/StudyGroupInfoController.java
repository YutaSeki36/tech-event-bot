package com.slackbot.studygroup.studygroupbot.controller;

import com.slackbot.studygroup.studygroupbot.domain.ConnpassResponse;
import com.slackbot.studygroup.studygroupbot.service.ConpassServise;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyGroupInfoController {

    private final ConpassServise conpassServise;


    @RequestMapping("/study")
    public ConnpassResponse getConnpass(){
        return conpassServise.postConnpassInfo();
    }
}
