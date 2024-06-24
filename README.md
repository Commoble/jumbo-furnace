TODO add readme

more info here
https://www.curseforge.com/minecraft/mc-mods/jumbo-furnace

Custom jumbo recipe example:

```
{
	"type": "jumbofurnace:jumbo_smelting",
	"ingredients":
	[
		{ // neoforge "sized ingredient" format, can be item, tag, or custom ingredient type
			"tag": "minecraft:anvil",
			"count": 4
		}
	],
	"results": [ // standard mojang itemstack format, can have "count" or component data
		{"id": "gold_ingot"},
		{"id": "iron_ingot"},
		{"id": "copper_ingot"},
		{"id": "diamond"}
	],
	"cookingtime": 200,
	"experience": 0.1
}
```
