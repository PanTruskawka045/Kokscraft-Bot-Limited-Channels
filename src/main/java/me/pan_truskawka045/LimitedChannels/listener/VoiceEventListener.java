package me.pan_truskawka045.LimitedChannels.listener;

import me.pan_truskawka045.AnnotationCore.annotation.ContextParam;
import me.pan_truskawka045.AnnotationCore.annotation.event.EventHandler;
import me.pan_truskawka045.AnnotationCore.annotation.event.EventListener;
import me.pan_truskawka045.AnnotationCore.annotation.file.GlobalConfig;
import me.pan_truskawka045.AnnotationCore.event.Event;
import me.pan_truskawka045.LimitedChannels.model.ChannelConfig;
import me.pan_truskawka045.LimitedChannels.model.LimitedChannelsConfig;
import net.dv8tion.jda.api.entities.*;

import java.util.Comparator;
import java.util.List;

@EventListener
public class VoiceEventListener {

    @GlobalConfig("limited-channels")
    private LimitedChannelsConfig limitedChannelsConfig;

    @EventHandler(event = Event.GUILD_MEMBER_VOICE_JOIN)
    private void onVoiceJoin(
            @ContextParam AudioChannel channel,
            @ContextParam Member member,
            @ContextParam Guild guild
    ){
        if(!(channel instanceof VoiceChannel)) return;
        VoiceChannel voiceChannel = (VoiceChannel) channel;
        long parentCategoryIdLong = voiceChannel.getParentCategoryIdLong();
        ChannelConfig channelConfig = limitedChannelsConfig.getChannels().getOrDefault(parentCategoryIdLong, null);
        if(channelConfig == null) return;
        List<Member> members = voiceChannel.getMembers();

    }

    @EventHandler(event = Event.GUILD_MEMBER_VOICE_LEAVE)
    private void onVoiceLeave(
            @ContextParam AudioChannel channel,
            @ContextParam Member member,
            @ContextParam Guild guild
    ){

    }


    @EventHandler(event = Event.GUILD_MEMBER_VOICE_MOVE)
    private void onVoiceMove(
            @ContextParam AudioChannel left,
            @ContextParam AudioChannel joined,
            @ContextParam Member member,
            @ContextParam Guild guild
            ){
        onVoiceLeave(left, member, guild);
        onVoiceJoin(joined, member, guild);
    }

    private void sortChannels(Category category){
        List<VoiceChannel> channels = category.getVoiceChannels();
        List<VoiceChannel> emptyChannels = category.getVoiceChannels();
        channels.sort(Comparator.comparing(VoiceChannel::getId));
        channels.removeIf(channel -> {
            boolean empty = channel.getMembers().isEmpty();
            if(empty){
                emptyChannels.add(channel);
            }
            return empty;
        });
        if(emptyChannels.size() > 1){
            for (int i = 1; i < emptyChannels.size(); i++) {
                emptyChannels.get(i).delete().queue();
            }
        }
        if(!emptyChannels.isEmpty()){
            channels.add(emptyChannels.get(0));
        }
        ChannelConfig channelConfig = limitedChannelsConfig.getChannels().get(category.getIdLong());
        for (int i = 0; i < channels.size(); i++) {
            VoiceChannel channel = channels.get(i);
            channel.getManager().setPosition(i).setUserLimit(channelConfig.getLimit())
                    .setName(channelConfig.getName().replace("{{index}}", String.valueOf(i+1))).queue();
        }
    }
}
