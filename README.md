# Fast Trading

Adds a button to the villager trading GUI to repeat the current trade until it's no longer available (again).

## Configuration

This mod can be configured in-game using Mod Menu or by editing the configuration file at `config/fasttrading.json`.

| Config name           | Description                                 | Options                                                  | Default value          |
| --------------------- | ------------------------------------------- | -------------------------------------------------------- | ---------------------- |
| Ticks Between Actions | Delay between moving items                  | Any number greater than `0.025`                          | `1`                    |
| Autofill Behavior     | Use Vanilla autofill or match items exactly | `Vanilla`, `Strict`                                      | `Vanilla`              |
| Speed Trade Block     | When to disallow trading                    | `Result is Damageable`, `Result is Unstackable`, `Allow` | `Result is Damageable` |
| Stop on Price Change  | Stop trading whenever the price changes     | `On Increase`, `On Change`, `Never`                      | `On Increase`          |
| Stop on New Offers    | Stop when new trade offers appear           | `Yes`, `No`                                              | `No`                   |

This is an updated fork of [SpeedTrading](https://github.com/ModsByLeo/SpeedTrading) by [ModsByLeo](https://github.com/ModsByLeo). Full credit goes to them for creating it.
