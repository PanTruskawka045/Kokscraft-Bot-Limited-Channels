package me.pan_truskawka045.LimitedChannels.listener;

import me.pan_truskawka045.AnnotationCore.annotation.ContextParam;
import me.pan_truskawka045.AnnotationCore.annotation.event.EventHandler;
import me.pan_truskawka045.AnnotationCore.annotation.event.EventListener;
import me.pan_truskawka045.AnnotationCore.annotation.file.GlobalConfig;
import me.pan_truskawka045.AnnotationCore.event.Event;
import me.pan_truskawka045.LimitedChannels.model.ChannelConfig;
import me.pan_truskawka045.LimitedChannels.model.LimitedChannelsConfig;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventListener
public class VoiceEventListener {

    @GlobalConfig("limited-channels")
    private LimitedChannelsConfig limitedChannelsConfig;

    @EventHandler(event = Event.GUILD_MEMBER_VOICE_JOIN)
    private void onVoiceJoin(
            @ContextParam AudioChannel channel,
            @ContextParam Member member
    ) {
        if (!(channel instanceof VoiceChannel)) return;
        VoiceChannel voiceChannel = (VoiceChannel) channel;
        if (voiceChannel.getMembers().stream().anyMatch(m1 -> m1.getIdLong() != member.getIdLong())) return;
        long parentCategoryIdLong = voiceChannel.getParentCategoryIdLong();
        ChannelConfig channelConfig = limitedChannelsConfig.getChannels().getOrDefault(parentCategoryIdLong, null);
        if (channelConfig == null) return;
        if (voiceChannel.getParentCategory() == null) return;
        sortChannels(voiceChannel.getParentCategory());
    }

    @EventHandler(event = Event.GUILD_MEMBER_VOICE_LEAVE)
    private void onVoiceLeave(
            @ContextParam AudioChannel channel
    ) {
        if (!(channel instanceof VoiceChannel)) return;
        VoiceChannel voiceChannel = (VoiceChannel) channel;
        if (!voiceChannel.getMembers().isEmpty()) return;
        long parentCategoryIdLong = voiceChannel.getParentCategoryIdLong();
        ChannelConfig channelConfig = limitedChannelsConfig.getChannels().getOrDefault(parentCategoryIdLong, null);
        if (channelConfig == null) return;
        if (voiceChannel.getParentCategory() == null) return;
        sortChannels(voiceChannel.getParentCategory());
    }


    @EventHandler(event = Event.GUILD_MEMBER_VOICE_MOVE)
    private void onVoiceMove(
            @ContextParam(name = "left") AudioChannel left,
            @ContextParam(name = "joined") AudioChannel joined,
            @ContextParam Member member
    ) {
        if (left instanceof VoiceChannel && joined instanceof VoiceChannel) {
            VoiceChannel leftVoiceChannel = (VoiceChannel) left;
            VoiceChannel joinedVoiceChannel = (VoiceChannel) joined;
            if (leftVoiceChannel.getParentCategory() != null && leftVoiceChannel.getParentCategoryIdLong() == joinedVoiceChannel.getParentCategoryIdLong()) {
                sortChannels(leftVoiceChannel.getParentCategory());
                return;
            }
        }
        onVoiceLeave(left);
        onVoiceJoin(joined, member);
    }

    private void sortChannels(Category category) {
        List<VoiceChannel> channels = new ArrayList<>(category.getVoiceChannels());
        List<VoiceChannel> emptyChannels = new ArrayList<>();
        channels.sort(Comparator.comparing(VoiceChannel::getPosition));
        channels.removeIf(channel -> {
            boolean empty = channel.getMembers().isEmpty();
            if (empty) {
                emptyChannels.add(channel);
            }
            return empty;
        });
        VoiceChannel emptyChannel = null;
        ChannelConfig channelConfig = limitedChannelsConfig.getChannels().get(category.getIdLong());
        if(channelConfig == null) return;
        if (!emptyChannels.isEmpty()) {
            VoiceChannel voiceChannel = category.getVoiceChannels().stream().max(Comparator.comparing(VoiceChannel::getPosition)).orElse(null);
            if (voiceChannel != null && voiceChannel.getMembers().isEmpty()) {
                emptyChannel = voiceChannel;

            }

            for (VoiceChannel channel : emptyChannels) {
                if (emptyChannel == null || channel.getIdLong() != emptyChannel.getIdLong()) {
                    channel.delete().queue();
                }
            }
        }

        if (emptyChannel != null) {
            int maxPosition = 0;
            for (VoiceChannel channel : channels) {
                if (emptyChannel.getIdLong() == channel.getIdLong()) continue;
                if (channel.getPosition() > maxPosition) {
                    maxPosition = channel.getPosition();
                }
            }

        } else {
            category.createVoiceChannel(channelConfig.getName()).setUserlimit(channelConfig.getLimit()).complete();
        }
    }
}
