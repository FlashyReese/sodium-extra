# Protected gameplay fog

Protected Gameplay Fog controls fog that can affect gameplay:

- Blindness
- Darkness
- Lava
- Powder snow
- Water

These controls are intentionally stricter than Sodium Extra's normal atmospheric fog
settings.

## Singleplayer and LAN

Protected Gameplay Fog can apply in private singleplayer worlds. It can also apply in
LAN worlds when cheats are enabled for other players.

## Multiplayer servers

On normal multiplayer servers, protected gameplay fog stays vanilla by default.

To use it on a multiplayer server:

1. Install Sodium Extra.
2. Install [Greenlight](https://modrinth.com/mod/greenlight).
3. Join a server that authorizes Sodium Extra's protected gameplay fog feature.
4. Enable Protected Gameplay Fog in Sodium Extra and choose your local fog values.

If [Greenlight](https://modrinth.com/mod/greenlight) is not installed, or the server
does not authorize the feature, Sodium Extra fails closed and protected gameplay fog
stays vanilla.

## Server limits

Server authorization does not force fog changes on. It only defines what the client may
do after the player opts in locally.

A server can:

- Allow or deny each protected fog type independently.
- Set the maximum allowed fog distance in blocks.
- Decide whether `Off` is allowed for each fog type.

If your local setting goes past the server's policy, Sodium Extra clamps it to the
server's limit. If the server does not allow `Off`, Sodium Extra uses the server's
maximum allowed distance instead.

This is an authorization signal for honest clients, not an anti-cheat system.
