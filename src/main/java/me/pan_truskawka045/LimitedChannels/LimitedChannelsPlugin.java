package me.pan_truskawka045.LimitedChannels;

import me.pan_truskawka045.AnnotationCore.annotation.Bean;
import me.pan_truskawka045.AnnotationCore.annotation.ComponentScan;
import me.pan_truskawka045.AnnotationCore.annotation.file.GlobalConfig;
import me.pan_truskawka045.AnnotationCore.annotation.plugin.Plugin;
import me.pan_truskawka045.AnnotationCore.annotation.scheduler.Scheduled;
import me.pan_truskawka045.LimitedChannels.model.LimitedChannelsConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

@Plugin(name = "Limited-Channels", author = "pan_truskawka045")
@ComponentScan
public class LimitedChannelsPlugin {

    @GlobalConfig("limited-channels")
    private LimitedChannelsConfig limitedChannelsConfig;

    @Scheduled(delay = 30)
    private void tick(@Bean Guild guild){
        limitedChannelsConfig.getChannels().forEach((categoryId, config) -> {
            Category category = guild.getCategoryById(categoryId);
            if (category == null) return;
            long count = category.getVoiceChannels().stream().filter(voiceChannel -> voiceChannel.getMembers().isEmpty()).count();
            if(count == 0){
                category.createVoiceChannel(config.getName()).setUserlimit(config.getLimit()).complete();
                return;
            }
            if(count > 1){
                category.getVoiceChannels().stream().filter(voiceChannel -> voiceChannel.getMembers().isEmpty()).skip(1).forEach(voiceChannel -> voiceChannel.delete().queue());
            }
        });
    }

}
