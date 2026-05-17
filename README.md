# Description
Liquid Lab is a fluid API to streamline the creation of custom fluids. You can set fog attributes, what happens when your fluids mix with other fluids, and create gaseous fluids that flow upwards. This mod exists to have an easy way to create a high quality custom fluid, that is easily customizable.

# Features

## Base Fluid Changes
Lots of utility methods added to the `Fluid` / `FlowableFluid` class

**Fluid Mixing**

- Done through `onFluidMix`
- Called when your fluid immediately touches another fluid
- Params:
  - World the fluid is in
  - Block Positon of the calling fluid
  - Side the fluid was touched from
  - Fluid state that was interacted with 

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
- Returns:
  - XYZ velocity multiplier for an entity
  - Push force of the fluid
  - Movement speed for an entity actively moving

**Item Viscosity**

- Done through `getItemMotion`
- Used for an item entity's buoyancy and viscosity
- Params:
  - World the entity is in
- Returns:
  - XYZ velocity multiplier for an item
  - Maximum vertical velocity of an item
  - How fast an item rises in the fluid

**Fog**

- Done through `getFogProperties`
- Enables the fluid to have fogginess
- Params:
  - World the viewing entity is in
  - Position of the cameara
  - Entity that would view the fog
- Returns:
  - Fog start in blocks
  - Fog End in Blocks
  - RGB color of the fog

**Bucket Sounds**

- Done through `getBucketEmptySound`
- Enables the fluid to not only have a custom filling sound, but also a custom emptying sound
- Returns:
  - The sound to make when a bucket is filled or emptied

**Pathfinding**

- Done through `canPathfindThrough`
- Allows a fluid to directly identify weather something should try to pathfind through it
- Params:
  - The type of navigation an entity has
- Returns:
  - Weather or not pathfinding is valid through this fluid

## Fluid Tags
3 fluid tags are added by the API:
- `liquid_lab:swimmable`
  - Defines if this fluid can be swum in
- `liquid_lab:drowns`
  - Defines if this fluid will drown entities submerged within it
- `liquid_lab:gaseous`
  - Marks the fluid as a gas, does not have any functionality

## Gas Fluids
In addition to adding ways to create custom fluids easier, this API also adds reversed fluids. Gas fluids are created the same way as regular fluids, except the block is registered by using `GasFluidBlock` and the fluid is created by extending `FlowableGasFluid`. Rendering a gas fluid is done through an instance of `SimpleGasFluidRenderHandler`, rather than `SimpleFluidRenderHandler`.
