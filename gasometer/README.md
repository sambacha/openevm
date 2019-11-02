# Gasometer

This defines the gas cost calculation module for EVM.

## Import

The gasometer has access to the following information:

* **Stack**: The EVM's stack.
* **Memory effective length**: The effective length of memory, defined
  in EVM Core.
  
## Constants

* `G_MEMORY`: Used to calculate memory gas from memory effective
  length.

## Data Structures

The gasometer maintains:

* **Status**: Can be two values -- either "okay" or "error". Error
  indicates that an out of gas error has already happened.
* **Gas limit**: The current gas limit.
* **Used gas counter**: Unsigned counter for used gas.
* **Refund gas counter**: Signed counter for gas refund.

## Methods

### `gasometer.record_used(gas)`

Increase the used gas counter by the amount of `gas`. If the increment
leads to the condition that used gas counter is greater than gas
limit, set used gas counter to gas limit, and set status to error.

Returns okay if the status ended up being okay, otherwise return
error.

### `gasometer.record_refund(refund)`

Increase or decrease the refund gas counter, based on `refund`'s sign.

### `gasometer.total_used_gas()`

Calculate the total used gas of a gasometer. 

Calculate memory gas, with the formular `G_MEMORY * a + a * a // 512`,
where `a` is the memory effective length. Return memory gas plus used
gas counter.

### `gasometer.effective_used_gas()`

Calculate the effective used gas for a transaction, based on total
used gas and gas limit.

Calculate the effective refund gas. If refund gas counter is negative,
the effective refund gas is 0. Otherwise, cap the refund gas at half
of the total used gas.

Return total used gas minus effective refund gas.
