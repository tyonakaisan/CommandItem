# CommandItem

## Example

```hocon:sample
# Rewrite if not empty
display-name="<!italic><bold>キンキンのモカ・モーラ<!italic><bold><gray>(試供品)"
# Rewrite if not empty
lore=[]
# Not readable & editable
raw-item-stack="H4sIAAAAAAAA/+NiYGBm4HZJLEkMSy0qzszPY2Dg+8/BwJSZwiCWm5mXmlyUmFZilZGfl1oZn5RfUpKTyszAmpxfmlfCwMDAyMXAlZyfWwCUzSsp5mAQRehILi0uyc+Nz0vMTWWYVK2UlJ+TomRVUlSaqqOUWlFSlKhkFY0qmpyfk1+kZKWUXpRYqaSjlFmSmJOZrGSVlphTDJQtAWoCSmq8WLnsyb7ZTyc3airVxuJS9bhp7ePmzVCycd3j5kWPm1Y/bt4NYjTvedy8UqmWmUEaw625+SmpOfEpwKAAeo2JgQEANmD2URsBAAA="
attributes {
    key="command_item:moca_cola"
    stackable=true
    placeable=true
    hide-cool-time-announce=false
    max-uses="1"
    cool-time="0"
    pick-commands {
        CONSUME="-1"
    }
}
commands {
    CONSUME=[
        {
            type=FROZEN
            commands=[
                "200"
            ]
            is-console=true
            repeat="1"
            period="1"
            delay="0"
            run-weight="1"
        },
        {
            type=MESSAGE
            commands=[
                "<b><aqua>キンキンだ！</b>"
            ]
            is-console=true
            repeat="1"
            period="1"
            delay="0"
            run-weight="1"
        }
    ]
}

