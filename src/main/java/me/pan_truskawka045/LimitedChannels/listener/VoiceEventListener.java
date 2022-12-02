package me.pan_truskawka045.LimitedChannels.listener;

import me.pan_truskawka045.AnnotationCore.annotation.ContextParam;
import me.pan_truskawka045.AnnotationCore.annotation.event.EventHandler;
import me.pan_truskawka045.AnnotationCore.annotation.event.EventListener;
import me.pan_truskawka045.AnnotationCore.annotation.file.GlobalConfig;
import me.pan_truskawka045.AnnotationCore.event.Event;
import me.pan_truskawka045.LimitedChannels.model.ChannelConfig;
import me.pan_truskawka045.LimitedChannels.model.LimitedChannelsConfig;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

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
            @ContextParam AudioChannel channel,
            @ContextParam Member member
    ) {
        channel.getMembers().forEach(m1 -> System.out.println(m1.getEffectiveName()));
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
        onVoiceLeave(left, member);
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
        if (emptyChannels.isEmpty()) {
//            VoiceChannel channel = category.createVoiceChannel(channelConfig.getName()).complete();
//            System.out.println(channel.getPosition());
//            channels.add(channel);
//            emptyChannels.add(channel);
//        } else if (emptyChannels.size() == 1) {
//            emptyChannel = emptyChannels.get(0);
//        } else {
//            for (int i = 0; i < emptyChannels.size()-1; i++) {
//                emptyChannels.get(i).delete().queue();
//            }
//            emptyChannel = emptyChannels.get(emptyChannels.size()-1);
//        }
        } else {
            //TODO Zrobić to jakoś normalnie
            for (int i = 0; i < emptyChannels.size(); i++) {
                emptyChannels.get(i).delete().queue();
            }
        }

//        for (int i = 0; i < channels.size(); i++) {
//            VoiceChannel channel = channels.get(i);
//            channel.getManager().setPosition(i + 1).setUserLimit(channelConfig.getLimit())
//                    .setName(channelConfig.getName().replace("{{index}}", String.valueOf(i + 1))).queue();
//        }
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
