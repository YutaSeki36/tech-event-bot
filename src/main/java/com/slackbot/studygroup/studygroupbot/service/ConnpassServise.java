package com.slackbot.studygroup.studygroupbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slackbot.studygroup.studygroupbot.domain.StudyGroup;
import com.slackbot.studygroup.studygroupbot.repository.ConnpassRepository;
import com.slackbot.studygroup.studygroupbot.domain.ConnpassResponse;
import com.slackbot.studygroup.studygroupbot.domain.SlackPostObject;
import com.slackbot.studygroup.studygroupbot.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnpassServise {

    private final ConnpassRepository connpassRepository;

    private final StudyGroupRepository studyGroupRepository;

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Value("${slack.endpoint}")
    private String endpoint;


    public ConnpassResponse postConnpassInfo() {

        List<ConnpassResponse.Events> events = new ArrayList<>();

        // 既にslackに投稿された勉強会idを取得
        List<Integer> addedStudygroupIds =
                studyGroupRepository.findAll().stream()
                        .map(e -> e.getSgId())
                        .collect(Collectors.toList());

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // 現在より15分前を定義
        calendar.add(Calendar.MINUTE, -10);

        // streamで最新情報のみに絞り込み
        connpassRepository.getConnpassResponse().getEvents()
                .stream()
                .filter(e -> calendar.getTime().before(getDate(e.getUpdated_at())) && now.before(getDate(e.getStarted_at())))
                .forEach(events::add);

        for (ConnpassResponse.Events e : events) {
            try {

                // 新しい勉強会情報か更新された勉強会情報かを判別
                if (addedStudygroupIds.contains(e.getEvent_id())) {

                    postToSlack(createTextData(e, "勉強の情報が更新されたよ"));
                } else {

                    StudyGroup studyGroup = StudyGroup.builder().sgId(e.getEvent_id()).build();
                    studyGroupRepository.save(studyGroup);

                    postToSlack(createTextData(e, "新しい勉強の情報だよ"));
                }
            } catch (Exception err) {

            }
        }

        return connpassRepository.getConnpassResponse();
    }

    private Date getDate(String eventDate) {
        Date date = null;
        try {
            date = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    private String createTextData(ConnpassResponse.Events events, String headingText) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(SlackPostObject.builder()
                .channel("#event-info")
                .username("宇垣美里")
                .text( headingText + " :heart: "
                        + LINE_SEPARATOR
                        + "タイトル: " + events.getTitle()
                        + LINE_SEPARATOR
                        + "内容: " + events.getCatch1()
                        + LINE_SEPARATOR
                        + "開催日: " + events.getStarted_at()
                        + LINE_SEPARATOR
                        + "会場: " + events.getPlace()
                        + LINE_SEPARATOR
                        + "開催場所: " + events.getAddress()
                        + LINE_SEPARATOR
                        + events.getEvent_url())
                .icon_emoji(":ugaki1:")
                .build());

        return json;
    }

    private void postToSlack(String payload) throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(endpoint);

        StringEntity entity = new StringEntity(payload, "UTF-8");

        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json; charset=shift_jis");
        client.execute(httpPost);
        client.close();
    }
}
