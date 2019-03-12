
## Difficulties
LLVM is a register-based modular compiler framework, and EVM is a stack-based virtual machine architecture. All LLVM's program analysis and optimization passes are all built for register based architecture. 

## Solution
In order to make LLVM adapt to generate efficient EVM instruction code, we propose a codegen framework that is derived from the `WebAssembly` backend: 
1. for each EVM instruction, create a stack-based version and a register-based version. There is a one-to-one mapping relation implemented using `InstrMapping`.
2. pretend EVM platform is a register-based architecture, let the backend generate register-based EVM instructions and perform analysis and optimization as other LLVM backends.
3. right at the end of codegen (before printing assembly), do a `stackifier` pass that will map each reg-based instruction to a set of stack-based instruction. The backend will generate stack manipulation instructions (`PUSH`, `POP`, etc) as well.

## Register-based Instructions
If we consider the stack space as a register file address space, we can see that:
* the instruction set are operating solely on registers
* `PUSH` and `POP` instructions can load/remove immediate numbers to/from registers.
* `MSTORE` and `MLOAD` are used for interacting with memory address space.
* each register operands become invalid after a use. In order to make a register be able to be used in more than one instruction, the operand has to be rematerialized for a second instruction by:
    * using `PUSH` (if is immediate)
    * duplicating using `DUP`, or
    * use `MSTORE` and `MLOAD` to spill it to memory
* Storage space manipulation and other operations such as `SHA3`, `PC`, etc can be implemented as intrinsic calls in the backend.
