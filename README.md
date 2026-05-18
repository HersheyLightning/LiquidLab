# Description
Liquid Lab is a fluid API to streamline the creation of custom fluids, as most fluid attributes are hardcoded. Some features include setting fog color, custom mixing behavior, and the creation of gaseous fluids that flow upwards. This mod exists because fluids on fabric have limited functionality and it would be useful to have a way to create a high quality custom fluid.

# Features

## Base Fluid Changes
Lots of utility methods added to the base `Fluid` class

**Fluid Mixing**

- Done through `onFluidMix`
- Immediately called when your fluid touches another fluid
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
- Enables the fluid to have special fog
- Params:
  - World the entity is in
  - Position of the cameara
  - Entity that is viewing the fog
- Returns:
  - Fog start in blocks
  - Fog End in Blocks
  - RGB color of the fog

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
3 fluid tags are added by the API:
- `liquid_lab:swimmable`
  - Defines if this fluid can be swum in
- `liquid_lab:drowns`
  - Defines if this fluid will drown entities submerged within it
- `liquid_lab:gaseous`
  - Simply marks the fluid as a gas, does not have any other functionality

## Gaseous Fluids
Additionally, the API also adds reversed fluids that flow upwards. Gas fluids are registered in the same way as regular fluids, except the block form is created with `GasFluidBlock` and the fluid form is created by extending `FlowableGasFluid`. To have the proper rendering for a gas, use `SimpleGasFluidRenderHandler`, rather than `SimpleFluidRenderHandler` in your client initializer.
