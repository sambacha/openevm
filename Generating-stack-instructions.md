LLVM is a register-based modular compiler framework, and EVM is a stack-based virtual machine architecture. All LLVM's program analysis and optimization passes are all built for register based architecture. In order to make LLVM adapt to generate efficient EVM instruction code, we propose a codegen framework that is derived from the `WebAssembly` backend: 
1. for each EVM instruction, create a stack-based version and a register-based version. There is a one-to-one mapping relation implemented using `InstrMapping`.
2. pretend EVM platform is a register-based architecture, let the backend generate register-based EVM instructions and perform analysis and optimization as other LLVM backends.
3. right at the end of codegen (before printing assembly), do a `stackifier` pass that will map each reg-based instruction to a set of stack-based instruction. The backend will generate stack manipulation instructions (`PUSH`, `POP`, etc) as well.

  