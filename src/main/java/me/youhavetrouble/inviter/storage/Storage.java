package me.youhavetrouble.inviter.storage;

import me.youhavetrouble.inviter.DiscordInvite;
import net.dv8tion.jda.api.entities.Invite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Storage {

    @Nullable DiscordInvite getInvite(long guildId);

    /**
     * Saves the invite to the storage and returns the saved invite.
     * @param invite JDA invite object to save
     * @return the saved DiscordInvite object
     */
    @NotNull DiscordInvite saveInvite(Invite invite);

}
