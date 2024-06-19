## 1.20.4-5.0.0.0
* Updated to MC 1.20.4 / Neoforge. This update is not compatible with old worlds.

### Player-Facing Changes
* Jumbo Smelting Recipes now allow producing more than one result itemstack
* Upgraded Jumbo Furnaces which are processing multiple recipes now process recipes independently instead of all at once; e.g. if you add raw chicken to a jumbo furnace which is already cooking chicken, it will start smelting the new chicken immediately, and the two chickens will finish cooking at different times. This fixes a bug where adding recipe ingredients at the end of a smelt cycle would cause them to consume less fuel than intended.
* Jumbo Furnace now consumes ingredients when it begins cooking a recipe instead of when it finishes. This may cause confusion, but was necessary to keep the previous two points from being too convoluted to implement, and resulted in a lot of bugs being fixed.
* Jumbo Furnace will now award a player all stored smelting experience when they take any item out of the output slots (instead of trying to store recipe experience in the slot that the recipe added a result item to).
* Jumbo Furnace is now in the Functional Blocks creative tab (instead of Building Blocks)

### Data Changes
* The `27 furnace -> 1 jumbo furnace` jumbo smelting recipe now uses the forge:furnaces tag for the inputs.
* Jumbo Smelting recipes now allow multiple outputs. You can achieve this with a "results" field which specifies a list of itemstacks to create, or you can keep using the "result" field instead to specify a single output.
* Jumbo Smelting recipes now allow per-recipe cooking times as vanilla smelting recipes do. This is set by the "cookingtime" field in the recipe json and defaults to 200 if not specified. Jumbo Furnace will also respect the cooking times of standard smelting recipes.
* The serverconfig option to set the jumbo furnace's global cooking time has been removed as there is no longer a global jumbo furnace cook time.
* Example recipe using the new fields:
```
{
	"type": "jumbofurnace:jumbo_smelting",
	"ingredients":
	[
		{
			"item": "jumbofurnace:jumbo_furnace"
		}
	],
	"results": [
		{"item": "gold_ingot"},
		{"item": "iron_ingot"},
		{"item": "copper_ingot"},
		{"item": "diamond"}
	],
	"cookingtime": 200,
	"experience": 0.1
}
```

### Bugfixes

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
 