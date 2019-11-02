# Net SSTORE Gas Cost Module

This defines the gas cost calculation module for SSTORE with net gas metering.

## Imports

This gas cost module has access to the following information.

* **Stack**: The EVM stack.
* **Storage**: EVM storage of the current operating address.
* **Original storage**: EVM storage state at the beginning of the
  current transaction.
* **Gasometer gas left**: The current remaining gas value of the
  gasometer.

## Constants

* `G_SSTORE_SET`: Gas cost for setting a storage value from zero to non-zero.
* `G_SSTORE_RESET`: Gas cost for setting a storage value otherwise.
* `G_SLOAD`: Gas cost for SLOAD operation and SSTORE when a value is unchanged.
* `R_SSTORE_CLEAR`: Refund for setting a storage value from non-zero
  to zero.
* `G_STIPEND`: Stipend paid for CALL opcode with value transfer.

## Calculations

Interpret stack item at index `0` as the index, and stack item at
index `1` as the *new value*. Fetch from *storage* at *index* as the
*current value*. Fetch from *original storage* at *index* as the
*original value*.

### Gas Cost

* If *gasometer gas left* is less than or equal to `G_STIPEND`, return
  `G_STIPEND + 1`.
* If *current value* equals *new value*, return `G_SLOAD`.
* If *current value* does not equal *new_value*
  * If *original value* equals *current value*
    * If *original value* is zero, return `G_SSTORE_SET`.
    * Otherwise, return `G_SSTORE_RESET`.
  * Otherwise, return `SLOAD_GAS`.
  
### Gas Refund

* If *original value* equals *current value*, and *new value* is zero,
  return `R_SSTORE_CLEAR`.
* Otherwise, create a local variable `refund`.
  * If *original value* is not zero
    * If *current value* is zero, remove `R_SSTORE_CLEAR` from
      `refund`.
    * Otherwise, if *new value* is zero, add `R_SSTORE_CLEAR` to
      `refund`.
  * If *original value* equals *new value*
    * If *original value* is zero, add `G_SSTORE_SET - G_SLOAD` to
      `refund`.
    * Otherwise, add `GSSTORE_RESET - G_SLOAD` to `refund`.
  * Return `refund`.
      
  
