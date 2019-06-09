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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnpassServise {

    private final ConnpassRepository connpassRepository;

    private final StudyGroupRepository studyGroupRepository;

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Value("${slack.endpoint}")
    private String endpoint;


    /**
     * connpassから勉強会情報を取得してslackに投稿するメソッド
     *
     * @return 取得した勉強会情報(絞り込み前)
     */
    public ConnpassResponse postConnpassInfo() {

        List<ConnpassResponse.Events> events = new ArrayList<>();

        List<StudyGroup> studyGroups = studyGroupRepository.findAll();

        // 既にslackに投稿された勉強会idを取得
        Map<Integer, List<StudyGroup>> addedStudyGroupIds =
                studyGroups.stream()
                        .collect(Collectors.groupingBy(StudyGroup::getSgId));


        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // 現在より15分前を定義
        calendar.add(Calendar.MINUTE, -1000);

        // コンパスAPIで勉強会情報を取得して，streamで最新情報のみに絞り込む
        connpassRepository.getConnpassResponse().getEvents()
                .stream()
                .filter(e -> calendar.getTime().before(getDate(e.getUpdated_at())) && now.before(getDate(e.getStarted_at())))
                .forEach(events::add);

        for (ConnpassResponse.Events e : events) {
            try {

                // 新しい勉強会情報か更新された勉強会情報かを判別
                if (addedStudyGroupIds.containsKey(e.getEvent_id())) {

                    if (checkEventUpdatedTime(now, addedStudyGroupIds.get(e.getEvent_id()).get(0).getUpdatedAt())) {
                        postToSlack(createTextData(e, "勉強会の情報が更新されたよ :two_hearts:"));

                        // 投稿されたイベントのupdated_timeを更新
                        studyGroupRepository.save(StudyGroup.builder()
                                .id(addedStudyGroupIds.get(e.getEvent_id()).get(0).getId())
                                .sgId(e.getEvent_id())
                                .build());
                    }

                } else {

                    StudyGroup studyGroup = StudyGroup.builder().sgId(e.getEvent_id()).build();
                    studyGroupRepository.save(studyGroup);

                    postToSlack(createTextData(e, "新しい勉強会の情報だよ :heart:"));
                }
            } catch (Exception err) {

            }
        }

        return connpassRepository.getConnpassResponse();
    }

    /**
     * 文字列を日付情報に変換して返すメソッド
     *
     * @param eventDate
     * @return
     */
    private Date getDate(String eventDate) {

        Date date = null;
        try {
            date = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    /**
     * イベントの更新時間を見て，二日以内に更新されていなければ真を返す．
     * 二日以内に更新されていたら偽を返すため，通知が行われない．
     *
     * @param now
     * @param eventUpdatedTime
     * @return
     */
    private boolean checkEventUpdatedTime(Date now, Date eventUpdatedTime) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        calendar.add(Calendar.DAY_OF_WEEK, -2);

        if (calendar.getTime().after(eventUpdatedTime)) return true;

        return false;
    }

    /**
     * slackに投稿するテキストデータの作成を行うメソッド
     * 更新された勉強会か，新しく作成された勉強会かで文言が変わる
     *
     * @param events
     * @param headingText
     * @return
     * @throws JsonProcessingException
     */
    private String createTextData(ConnpassResponse.Events events, String headingText) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(SlackPostObject.builder()
                .channel("#bot_test")
                .username("宇垣美里")
                .text(headingText
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

    /**
     * slackへの投稿を行うメソッド
     *
     * @param payload
     * @throws IOException
     */
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
