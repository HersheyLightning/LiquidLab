# Description
Liquid Lab is a fluid API to streamline the creation of custom fluids on fabric, because nearly every fluid attribute is hardcoded. Some features include setting fog color, data driven mixing behavior, and custom gaseous fluids that flow upwards. This API exists to make high quality fluids and not just reskinned water or lava.

# Features

## Base Fluid Changes
Lots of utility methods added to the base `Fluid` class

**Entity Splashing**

- Done through `onEntityEntry`
- Called when an entity first touches the fluid
- Params:
  - World the entity is in
  - Entity that touched the fluid

**Entity Interactions**

- Done through `onEntityCollision`
- Called while an entity is within the fluid
- Params:
  - World the entity is in
  - Block Position of the fluid
  - Entity that is within the fluid

**Entity Viscosity**

- Done through `getFluidMotion`
- Used for the fluid's push force and viscosity
- Params:
  - World the entity is in
- Returns a `FluidMotion` object storing:
  - XYZ velocity multiplier for an entity
  - Push force of the fluid
  - Movement speed for an entity actively moving

**Item Viscosity**

- Done through `getItemMotion`
- Used for an item entity's buoyancy and viscosity
- Params:
  - World the entity is in
- Returns a `FluidMotion` object storing:
  - XYZ velocity multiplier for an item
  - Maximum vertical velocity of an item
  - How fast an item rises in the fluid

**Fog**

- Done through `getFogProperties`
- Enables the fluid to have special fog
- Params:
  - World the entity is in
  - Position of the cameara
  - Entity that is viewing the fog
- Returns a `FogProperties` object storing:
  - Fog start in blocks
  - Fog End in Blocks
  - RGB color of the fog

**Rain Particles**

- Done through `getRainParticle`
- Gives the fluid unique splash particles when rain falls onto it
- Returns:
  - The particle to use when the fluid is rained on

**Bucket Sounds**

- Done through `getBucketEmptySound`
- Enables the fluid to not only have a custom filling sound, but also a custom emptying sound
- Returns:
  - The sound to make when a bucket is emptied

**Pathfinding**

- Done through `canPathfindThrough`
- Allows a fluid to directly identify weather something should try to pathfind through it
- Params:
  - The type of navigation an entity has
- Returns:
  - If it is valid to pathfind through this fluid

## Fluid Tags
7 fluid tags are added by the API:
- `liquid_lab:enhanced_mobility`
  - Defines that this fluid is swimmable and has sprint swimming
- `liquid_lab:swimmable`
  - Defines if this fluid can be swum in
- `liquid_lab:drowns`
  - Defines if this fluid will drown entities submerged within it
- `liquid_lab:evaporates`
  - Defines if the fluid cannot be placed in the nether
- `liquid_lab:fall_damage_reducing`
  - Makes the fluid affect fall damage the same way lava does
- `liquid_lab:fall_damage_resetting`
  - Makes the fluid affect fall damage in the same way as water
- `liquid_lab:gaseous`
  - Simply marks the fluid as a gas, does not have any other functionality

## Gaseous Fluids
Additionally, the API also adds reversed fluids that flow upwards. Gaseous fluids are registered in the same way as regular fluids, except the block form is created with `GasFluidBlock` and the fluid form is created by extending `FlowableGasFluid`. For rendering, use `SimpleGasFluidRenderHandler`, rather than `SimpleFluidRenderHandler` in your client initializer.

## Fluid Mixing
A simple yet flexable system is provided for creating data driven fluid mixing to allow for compatibility between mods.

**Folder Structure**

To add a fluid mix, first create a folder named `fluid_mixing` in the namespace folder. Files here should be JSON files named as a valid fluid tag for that namespace to fuction properly.

**File Structure**

- `replace` Optional. Weather or not this file replaces all of the rules for this fluid
- `rules` A list of rules for this fluid to follow
  - `type` Defines when the rule applies. Can be either `on_update`, which applies the rule when the fluid is updated by nearby blocks or `on_flow`, which applies the rule when another fluid flows into this fluid.
  - `is_still` Optional. Weather this fluid must be a source or flowing block. If not defined, any form of the fluid is accepted
  - `smoke` Optional. Weather or not this interaction creates smoke particles
  - `predicate` Block predicate that must pass for the interaction to succeed
  - `transform` A block state provider to place the resulting block of the interaction

**Block Predicates**

- Added `liquid_lab:touching_fluids`
  - Passes when an adjacent fluid matches the `fluids` parameter and if it that fluid could flow into the block
- Added `liquid_lab:adjacent_blocks`
  - Passes when an adjacent block matches the predicate's `blocks` paramenter

## Utility
Some simple utility methods are added by the API to make implementing certain aspects of fluids simpler.

**Fluid Utils**

Contains methods related to fluid interactions, such as `splashEntity` for use in `onEntityEntry` to make custom splashing effects with ease.

**Particle Utils**

Contains methods related to registering block leak particles. The class allows for registering dripping, falling, and landing particles independently or all at once for fluids and gasses.
