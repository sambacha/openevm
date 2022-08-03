# Net SSTORE Gas Cost Module

This defines the gas cost calculation module for SSTORE with net gas metering.

## Imports

This gas cost module has access to the following information.

-   **Stack**: The EVM stack.
-   **Storage**: EVM storage of the current operating address.
-   **Original storage**: EVM storage state at the beginning of the current transaction.
-   **Gasometer gas left**: The current remaining gas value of the gasometer.

## Constants

-   `G_SSTORE_SET`: Gas cost for setting a storage value from zero to non-zero.
-   `G_SSTORE_RESET`: Gas cost for setting a storage value otherwise.
-   `G_SLOAD`: Gas cost for SLOAD operation and SSTORE when a value is unchanged.
-   `R_SSTORE_CLEAR`: Refund for setting a storage value from non-zero to zero.
-   `G_STIPEND`: Stipend paid for CALL opcode with value transfer.

## Calculations

Interpret stack item at index `0` as the index, and stack item at index `1` as the _new value_. Fetch from _storage_ at _index_ as the _current value_. Fetch from _original storage_ at _index_ as the _original value_.

### Gas Cost

-   If _gasometer gas left_ is less than or equal to `G_STIPEND`, return `G_STIPEND + 1`.
-   If _current value_ equals _new value_, return `G_SLOAD`.
-   If _current value_ does not equal _new_value_
    -   If _original value_ equals _current value_
        -   If _original value_ is zero, return `G_SSTORE_SET`.
        -   Otherwise, return `G_SSTORE_RESET`.
    -   Otherwise, return `SLOAD_GAS`.

### Gas Refund

-   If _original value_ equals _current value_, and _new value_ is zero, return `R_SSTORE_CLEAR`.
-   Otherwise, create a local variable `refund`.
    -   If _original value_ is not zero
        -   If _current value_ is zero, remove `R_SSTORE_CLEAR` from `refund`.
        -   Otherwise, if _new value_ is zero, add `R_SSTORE_CLEAR` to `refund`.
    -   If _original value_ equals _new value_
        -   If _original value_ is zero, add `G_SSTORE_SET - G_SLOAD` to `refund`.
        -   Otherwise, add `GSSTORE_RESET - G_SLOAD` to `refund`.
    -   Return `refund`.
