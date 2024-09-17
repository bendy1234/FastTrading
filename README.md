# Fast Trading
Adds a button to the villager trading GUI to repeat the current trade until it's no longer available (again). 

## Configuration
This mod can be configured in-game using Mod Menu or by editing the configuration file at `config/fasttrading.json`.

### Config Options
- **ticksBetweenActions**: Number of ticks between actions (Default: `1`, Min: `0.025`).
- **autofillBehavior**: How to autofill the trade (Default: `DEFAULT`, Options: `DEFAULT`, `STRICT`).
- **tradeBlockBehavior**: When to block speed trading (Default: `DAMAGEABLE`, Options: `DAMAGEABLE`, `UNSTACKABLE`, `DISABLED`).

This is an updated fork of [SpeedTrading](https://github.com/ModsByLeo/SpeedTrading) by [ModsByLeo](https://github.com/ModsByLeo). Full credit goes to them for creating it.
