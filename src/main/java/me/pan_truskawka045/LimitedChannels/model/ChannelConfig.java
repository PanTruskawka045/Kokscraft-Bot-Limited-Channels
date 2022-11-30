package me.pan_truskawka045.LimitedChannels.model;

import lombok.Getter;
import me.pan_truskawka045.AnnotationCore.annotation.file.Entity;
import me.pan_truskawka045.AnnotationCore.annotation.file.Field;

@Getter
@Entity
public class ChannelConfig {

    @Field
    private int limit;

    @Field
    private String name;
}
