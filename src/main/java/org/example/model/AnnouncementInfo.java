package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementInfo{
    Long id;
    String text;
    List<String> videoIds;
    List<String> audioIds;
    List<String> imgIds;
}
