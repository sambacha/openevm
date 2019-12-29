# Functionalities



# Optimizations
## Support more than 16 local variables
EVM can only support retrieval of an element up to depth of 16 from the stack top using instructions `SWAP1` to `SWAP16` -- resulting a limitation in Solidity compiler that can only support 16 local variables. At this moment, EVM LLVM will also face a `stack too deep` issue if the variables in a single basic block is more than 16.

But in LLVM we can totally work around this issue, and do a much better job. With dataflow analysis and register allocation algorithm, we can have near-optimal variable assignment (on the stack or on memory stack) in linear time.

## Instruction scheduling
Arranging the order of the opcodes in EVM binary is critical to its performance. Instructions has to be arranged so that we have minimal stack manipulation over head (the opcodes that does not do actual computation, but rather, reorder stack operands' relative position to the top of stack). 

EVM LLVM backend is designed in such a way that a scheduler before register allocation can be implemented to reduce the stack operation overhead. 

## Improve EVM calling conventions
When calling a subroutine, The return address is the first argument and resides at top of stack. This is non-optimal because the return address will definitely not be used until the very end of the subroutine, and taking up a visible slot is expensive. We can re-arrange the return address to be at the end of argument so it will not have to be reached until we want to return from subroutine.

## Re-materialization of constants
usual small constants should not stay in stack --- they should be rematerialized whenever it is needed.



