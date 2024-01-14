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
            run-weight=1
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
# Item cool time
cool-time=0
# Item display name
display-name="<lang:item.minecraft.honeycomb>"
#                   ##### Experimental features ######
# ##### DO NOT EDIT THE SECTIONS "v", "==", "meta-type", "PublicBukkitValues" #####
# Some items may not convert correctly or may be missing data
# If such a thing happens, it cannot be loaded file
item-stack {
    type=HONEYCOMB
    v=3578
}
# ##### DO NOT EDIT THIS SECTION #####
key="command_item:test"
# Item lore
lore=[]
# The command can be executed as many times as specified in this field
# Entering a number below -1 disable this feature
max-uses=1
placeable=true
stackable=true
