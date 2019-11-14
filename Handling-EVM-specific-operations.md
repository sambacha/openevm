Ethereum Virtual Machine specific operations, such as accessing storage, retrieve block information, etc, are through EVM specific instructions. Solidity language automatically generates necessary EVM-specific instructions under the hood so as to hide the details from Solidity developers. However, as a compiler backend, the input to EVM LLVM is LLVM IR format, which is unable to hold any language specific semantics that is higher than the C language level. So it is up to compiler frontends to lower language specific semantics onto LLVM IR level. 

Intrinsic functions are used to represent EVM-specific semantics in the input LLVM IR. Intrinsic functions are usually higher level representations of architecture-specific instructions. In EVM LLVM, we allow users to leverage EVM-specific instructions that are used to interact with the chain or storage by exposing those EVM instructions in the form of intrinsic functions.

Intrinsics are defined [here](https://github.com/etclabscore/evm_llvm/blob/6271ae12899b6b9a2bfbcb3a690ec4b5e8652cfa/include/llvm/IR/IntrinsicsEVM.td#L14). And here are examples on [how to leverage intrinsics](https://github.com/etclabscore/evm_llvm/blob/6271ae12899b6b9a2bfbcb3a690ec4b5e8652cfa/test/CodeGen/EVM/intrinsics.ll#L1)



