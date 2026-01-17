## 1.21.1-5.0.0.9
* Fix Jumbo Furnace not incrementing crafting stats when removing items from output slots

## 1.21.1-5.0.0.8
* Jumbo Furnaces no longer retain crafting remainders of recipe inputs, to match expected behavior of vanilla furnaces. Recipes that expect crafting remainders to be generated will need to implement them as a jumbo smelting recipe with multiple outputs.

## 1.21.1-5.0.0.7
* Merging furnace blocks into a jumbo furnace now permits any furnace in the c:player_workstations/furnaces block tag

## 1.21.1-5.0.0.6
* Fix jumbo smelting recipes missing textures in JEI

## 1.21.1-5.0.0.5
* When fuel items are consumed from the fuel inventory and produce crafting remainders which are also fuel items, the crafting remainders are now returned to the fuel inventory instead of the output inventory
* Now requires MC 1.21.1 / neoforge 21.1.58

## 1.21-5.0.0.4
* Made smelting bar animation less choppy

## 1.21-5.0.0.3
* Add EMI support

## 1.21-5.0.0.2
* Fixed advancements not loading

## 1.21-5.0.0.1
* Fixed jumbo smelting recipe for 27 furnaces -> 1 jumbo furnace not working due to broken ingredient tag. Closes #23

## 1.21-5.0.0.0
* Updated to MC 1.21 / Neoforge. This update is not compatible with old worlds.

### Player-Facing Changes
* Jumbo Smelting Recipes now allow producing more than one result itemstack (#1)
* Upgraded Jumbo Furnaces which are processing multiple recipes now process recipes independently instead of all at once; e.g. if you add raw chicken to a jumbo furnace which is already cooking chicken, it will start smelting the new chicken immediately, and the two chickens will finish cooking at different times.
* When cooking non-jumbo smelting recipes, jumbo furnace will use that recipe's actual cooking time; there is no longer a "global" cooking time for jumbo smelting (#21)
* Jumbo Furnace now consumes ingredients when it begins cooking a recipe instead of when it finishes. This may cause confusion, but was necessary to keep the previous three points from being too convoluted to implement, and resulted in a lot of bugs being fixed.
* Jumbo Furnace will now award a player all stored smelting experience when they take any item out of the output slots (instead of trying to store recipe experience in the slot that the recipe added a result item to).
* Crafting remainders from fuel and recipe inputs (such as empty buckets and bowls) are now added to the output slots when a recipe begins cooking. (#11)
* Jumbo Furnace is now in the Functional Blocks creative tab (instead of Building Blocks)

### Data Changes
* The `27 furnace -> 1 jumbo furnace` jumbo smelting recipe now uses the neoforge:player_workstations/furnaces tag for its furnace input.
* Jumbo Smelting recipes now allow multiple outputs. You can achieve this with a "results" field which specifies a list of itemstacks to create, or you can keep using the "result" field instead to specify a single output.
* Jumbo Smelting recipes now allow per-recipe cooking times as vanilla smelting recipes do. This is set by the "cookingtime" field in the recipe json and defaults to 200 if not specified. Jumbo Furnace will also respect the cooking times of standard smelting recipes.
* The serverconfig option to set the jumbo furnace's global cooking time has been removed as there is no longer a global jumbo furnace cook time.
* The jumbofurnace:tag_stack ingredient type has been removed. Jumbo furnace recipes now support neoforge "sized ingredients" instead, which allow a count of any ingredient type.
* Example recipe using the new fields:
```
{
	"type": "jumbofurnace:jumbo_smelting",
	"ingredients":
	[
		{
			"tag": "minecraft:anvil",
			"count": 4
		}
	],
	"results": [
		{"id": "gold_ingot"},
		{"id": "iron_ingot"},
		{"id": "copper_ingot"},
		{"id": "diamond"}
	],
	"cookingtime": 200,
	"experience": 0.1
}
```

### Bugfixes and other improvements
* Recipes should now always use the correct amount of fuel when processing multiple recipes at once (#14)
* Improved recipe caching, recipes are now filtered by input items before checking recipes (#19)
* Reloading world after changing the global cooking time config no longer causes the furnace to stop working because there is no longer a global cooking time config (#22)

## 1.20.1-4.0.0.5
* Fix "unknown recipe category" log warnings for jumbo smelting recipes

## 1.20.1-4.0.0.4
* Fix jumbo furnace creating the wrong number of output items

## 1.20.1-4.0.0.3
* Jumbo Furnace now stops furnacing when it runs out of output space

## 1.20.1-4.0.0.2
* Fix slot offsets in JEI

## 1.20.1-4.0.0.1
* Fix broken textures in JEI recipe viewer for jumbo smelting

## 1.20.1-4.0.0.0
* Updated Jumbo Furnace to Minecraft 1.20.1 (requires forge 47.0.3 or higher)

## 1.19.2-3.0.0.0
* Updated Jumbo Furnace to Minecraft 1.19.2 (requires forge 43.1.0 or higher)
* This is a save-compatibility-breaking update. Jumbo Furnaces in old worlds may lose their data or disappear entirely.
* Removed orthodimensional hyperfurnaces. Jumbo furnaces can now be upgraded with jumbo furnaces instead. This is still specified via the `jumbofurnace:multiprocessing_upgrade` item tag.
* Mods using forge's burn time api for their fuel items can now specify a burn type for the "jumbofurnace:jumbo_smelting" recipe type, which will take precedence over normal furnace fuel values when used as fuel for a jumbo furnace.
* Vanilla-furnace smelting recipes with cooking times longer than jumbo furnace's cooking cycle time (specified in Jumbo Furnace's serverconfig) can no longer be cooked in jumbo furnaces.
 