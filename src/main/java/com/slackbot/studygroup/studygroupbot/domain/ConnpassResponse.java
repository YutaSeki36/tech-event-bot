package com.slackbot.studygroup.studygroupbot.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnpassResponse {

    private int results_returned;

    private int results_available;

    private List<Events> events;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Events{

        private int event_id;

        private String title;

        // catchは予約語のため変数名にできない
        @JsonProperty("catch")
        private String catch1;

        private String description;

        private String event_url;

        private String address;

        private String place;

        private String started_at;

        private String updated_at;
    }
}
