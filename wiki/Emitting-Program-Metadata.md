EVM LLVM provides a way to emit program's metadata for various of purposes. For examples, a symbol table that records the jump destinations can be emitted along with the generated binary. 

Developers can use this utility to emit more program information. 

## Existing implementation

When compiling a contract, a file named `EVMMeta.txt` will be generated along with the binary code. The file contains the function symbol table in the compiled program, along with the offset of each function. The metadata file can be used for various purposes, such as debugging, manual linking, analysis, and so on.

To specify a custom metadata file name if you do not want to use the `EVMMeta.txt` filename, option `-evm_md_file` can be used.

# Limitation

Existing implementation of EVM metadata emitting is limited to `MachineCode` module/level, which means that if there are any transformations at a higher level such as in the IR level, it will not be shown in the result.



 