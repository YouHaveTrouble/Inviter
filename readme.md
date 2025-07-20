# Inviter

Discord bot that allows users to set up personalized invite links for their servers and have rotating invite codes.


# Technical information

## Get invite

path: `/invite/{discordGuildId}`
method: `GET`

behavior: Requesting this endpoint without headers will return bodyless redirect (307) response that directs to the
discord invite link if successful. Error screens will be shown if there's an error. You can use `Accept` header to get
the invite link in different formats for automation purposes.

### Accepted headers

#### `Accept`

- `application/json` - Returns status 200 along with json object with `url` key that contains full discord invite link.
If response is not 200, json will contain `error` key with error message.
- `text/plain` - Returns status 200 along with plain text containing full discord invite link. Error responses will not have body.


# Self-hosting

There's Dockerfile and docker-compose.yml in the repository, I'm sure you can figure it out.
