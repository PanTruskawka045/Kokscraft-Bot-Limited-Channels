package me.pan_truskawka045.LimitedChannels.model;

import lombok.Getter;
import me.pan_truskawka045.AnnotationCore.annotation.file.Entity;
import me.pan_truskawka045.AnnotationCore.annotation.file.Field;

import java.util.Map;

@Entity
@Getter
public class LimitedChannelsConfig {


    @Field
    private Map<Long, ChannelConfig> channels;

}
