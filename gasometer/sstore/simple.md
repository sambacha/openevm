# Simple SSTORE Gas Cost Module

This defines the gas cost calculation module for SSTORE without net
gas metering.

## Imports

This gas cost module has access to the following information.

* **Stack**: The EVM stack.
* **Storage**: EVM storage of the current operating address.

## Constants

* `G_SSTORE_SET`: Gas cost for setting a storage value from zero to non-zero.
* `G_SSTORE_RESET`: Gas cost for setting a storage value otherwise.
* `R_SSTORE_CLEAR`: Refund for setting a storage value from non-zero
  to zero.
  
## Calculations

Interpret stack item at index `0` as the index, and stack item at
index `1` as the *new value*. Fetch from *storage* at *index* as the
*current value*.

### Gas Cost

If *current value* is zero, and *new value* is not zero, return
`G_SSTORE_SET`. Otherwise, return `G_SSTORE_RESET`.

### Refund

If *current value* is not zero, and *new value* is zero, return
`R_SSTORE_CLEAR`. Otherwise, return `0`.
