# CommandItem

## Example

```hocon:sample
# Specifies the command to be executed from console
by-console-commands {
    "RIGHT_CLICK"=[
        {
            action=COMMAND
            commands=[
                "minecraft:give <player> gold_ingot",
                "minecraft:give <player> iron_ingot"
            ]
            delay=0
            period=0
            repeat=0
        },
        {
            action=MESSAGE
            commands=[
                "<gold>iine!"
            ]
        },
        {
            action="BROAD_CAST"
            commands=[
                "<aqua>broadcast message!"
            ]
        }
    ]
}
# Specifies the command to be executed from player
by-player-commands {}
# ##### Experimental features ######
# ##### DO NOT EDIT THE SECTIONS "v", "==", "meta-type", "PublicBukkitValues" #####
# Some items may not convert correctly or may be missing data
# If such a thing happens, it cannot be saved file
item-stack {
    type=SHEARS
    v=3578
}
# ##### DO NOT EDIT THIS SECTION #####
key="command_item:test"
# The command can be executed as many times as specified in this field
# Entering a number below 0 disables this feature
max-uses=0
placeable=true
stackable=true


