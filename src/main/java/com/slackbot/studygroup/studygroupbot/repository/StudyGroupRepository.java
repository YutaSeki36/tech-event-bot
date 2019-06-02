package com.slackbot.studygroup.studygroupbot.repository;

import com.slackbot.studygroup.studygroupbot.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer> {
}
