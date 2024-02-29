package scripts.features.join

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class JoinHandler extends ListenerAdapter {

    static final Long memberRoleId = 1149057359524663297L

    @Override
    void onGuildMemberJoin(GuildMemberJoinEvent event) {
        event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(memberRoleId)).queue()
    }

}
