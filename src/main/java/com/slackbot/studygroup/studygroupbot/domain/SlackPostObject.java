package com.slackbot.studygroup.studygroupbot.domain;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class SlackPostObject {

    private String channel;

    private String username;

    private String text;

    private String catch1;

    protected String icon_emoji;

}
