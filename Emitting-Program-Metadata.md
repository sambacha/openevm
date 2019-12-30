EVM LLVM provides a way to emit program's metadata for various of purposes. For examples, a symbol table that records the jump destinations can be emitted along with the generated binary. 

Developers can use this utility to emit more program information. 

## Existing implementation

When compiling a contract, a file named `EVMMeta.txt` will be generated along with the binary code. To specify a custom metadata file name, `-evm_md_file` can be used.

Existing implementation of EVM metadata emitting is limited to `MachineCode` module.

## Future improvement

Consider moving the file handle to `EVMTargetMachine` class so every pass can emit values to the file. 


 