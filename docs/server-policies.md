# Server policies

Sodium Extra keeps gameplay-affecting fog controls disabled on normal multiplayer
servers unless the server explicitly authorizes them. Authorization is handled through
[Greenlight](https://modrinth.com/mod/greenlight) policies in a server resource pack.

Clients must have [Greenlight](https://modrinth.com/mod/greenlight) installed for these
policies to be read. Sodium Extra bundles the
[Greenlight](https://modrinth.com/mod/greenlight) API so missing it fails closed:
protected gameplay fog stays unavailable on normal multiplayer servers.

No server-side mod is required.

## Setup

1. Ask players to install Sodium Extra and [Greenlight](https://modrinth.com/mod/greenlight).
2. Put the policy file in your server resource pack at:

```text
assets/sodium-extra/client_features/v1/protected_gameplay_fog.json
```

3. Host the pack as your server resource pack.
4. Optionally set `require-resource-pack=true` in `server.properties` if your
   server wants to require the pack.

[Greenlight](https://modrinth.com/mod/greenlight) trusts selected resource packs that
Minecraft marks as coming from the current server. Local packs, world packs, built-in
packs, and files copied into the client by hand do not grant policies. If a player
declines, removes, or fails to load an optional server pack, protected gameplay fog
stays denied/vanilla.

## Example pack

A minimal lava-fog-only pack is available at
[`docs/examples/sodium-extra-lava-fog-policy-pack.zip`](examples/sodium-extra-lava-fog-policy-pack.zip).
Its source files live under
[`docs/examples/lava-fog-policy-pack/`](examples/lava-fog-policy-pack/).

That pack authorizes only lava fog, allows the client to turn lava fog off, and leaves
blindness, darkness, powder snow, and water fog vanilla.

## Policy file

```json
{
  "protocol_version": 1,
  "feature": "sodium-extra:protected_gameplay_fog",
  "enabled": true,
  "settings_version": 1,
  "settings": {
    "blindness": {
      "enabled": false
    },
    "darkness": {
      "enabled": false
    },
    "lava": {
      "enabled": true,
      "max_distance_blocks": 256,
      "allow_off": true
    },
    "powder_snow": {
      "enabled": false
    },
    "water": {
      "enabled": false
    }
  }
}
```

## Envelope schema

- `protocol_version`: Required. Must be `1`.
- `feature`: Required. Must be `sodium-extra:protected_gameplay_fog`.
- `enabled`: Optional, default `false`. If `false`, the policy denies the feature.
- `settings_version`: Optional, default `1`. Sodium Extra currently supports `1`.
- `settings`: Optional object. Missing fog rules deny that fog type.

Malformed policy files fail closed. Unsupported `settings_version` values deny the
feature.

## Protected fog rules

Each rule can appear under one of these keys:

- `blindness`
- `darkness`
- `lava`
- `powder_snow`
- `water`

Rule fields:

- `enabled`: Optional, default `false`. If `false`, that fog type stays vanilla.
- `max_distance_blocks`: Optional, default `0`. Largest distance the client may use,
  clamped by Sodium Extra to `0..256`.
- `allow_off`: Optional, default `false`. Whether the client may disable that fog type.

The policy only authorizes and bounds the player's own Sodium Extra settings. It never
turns protected fog changes on by itself. Players still need to enable Protected
Gameplay Fog and choose local values.

## Clamp behavior

- Local value `0` always means vanilla.
- Positive local values are clamped to `max_distance_blocks`.
- Local `Off` is honored only when `allow_off` is `true`.
- If `allow_off` is `false`, local `Off` becomes `max_distance_blocks`; if that maximum
  is `0`, the result is vanilla.

Private singleplayer worlds and LAN worlds with cheats enabled continue to allow the
player's local protected fog settings without a
[Greenlight](https://modrinth.com/mod/greenlight) policy.

This is an authorization signal for honest clients, not an anti-cheat system.
