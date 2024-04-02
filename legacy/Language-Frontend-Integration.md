## EVM target specific changes

### Frontend is expected to emit 256bit values LLVM IR

The EVM architecture is the only 256-bit machine out there in the market, and so far it have not yet received support from LLVM community. We added 256-bit and 160-bit support in the LLVM IR level.

In order to utilize 256-bit and 160-bit operands, developers are expected to emit `i256` and `i160` data types in their IR code generation. Include the `evm_llvm`'s header files in `include/llvm` folders so that these two pre-defined data types can be properly generated.

### Frontend needs to generate compatible LLVM IR

Notice that development of this backend is based on LLVM 10, which is released in March 2020. We also have a LLVM 8 branch just to support those who creates their frontends in LLVM 8.

We could do back porting to other lower versions such as LLVM 9 at the request of developers for better stability or compatibility. Please let me know if you have such needs.
