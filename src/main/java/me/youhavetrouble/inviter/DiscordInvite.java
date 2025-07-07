package me.youhavetrouble.inviter;

public record DiscordInvite(
        String code,
        Long guildId,
        Long expiresAt
) {

    public DiscordInvite {
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }
        if (guildId == null || guildId <= 0) {
            throw new IllegalArgumentException("Guild ID must not be null nor be a negative number");
        }
    }

    /**
     * Checks if the invite is expired.
     * The invite is considered expired if the current time is more than 5 seconds before the expiration time.
     *
     * @return true if the invite is expired, false otherwise
     */
    public boolean isExpired() {
        return System.currentTimeMillis() + 5000 > expiresAt;
    }

}
