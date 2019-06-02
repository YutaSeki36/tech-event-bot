package com.slackbot.studygroup.studygroupbot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "studygroup")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroup {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id = 0;

    @Column(name = "sg_id")
    private Integer sgId = 0;
}
