The project compiles like other LLVM projects. The target's name is `EVM`, but since it is not yet finalized, you have to specify `-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM` when you compile it.

In short,  you can use the following to build the backend:
```
git clone git@github.com:etclabscore/evm_llvm.git
cd evm_llvm
git checkout EVM
mkdir build && cd build
cmake -DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM ..
make -j8
```

Let's try to use a simple C file to test our compiler:
```
unsigned x;
int abc(unsigned a, unsigned b, unsigned c) {
  if (c > 0) {
    return a + x;
  } else {
    return a + b;
  }
}
```
You have to generate LLVM IR first:
```
clang -S -emit-llvm test.c
```

This will generate 
In order to use the backend to generate EVM assembly, you have to specify `-mtriple=evm` when calling `llc`. An example is as follows:
```
./build/bin/llc -mtriple=evm -debug test.ll -o test.s
```

Note that the generated code is the function body itself. In order to generate a complete smart contract source code we need to use `lld`, which is still in development.

You can also get the binary code of the function body by outputting an ELF object file, and extract the object file's text section. First we do:
```
./build/bin/llc -mtriple=evm -filetype=obj test.ll -o test.o
```
This will dump an ELF format object file `test.o`, which contains the EVM bytecode in its text section. We need to extract the binary somehow:
```
./build/bin/llvm-objdump --triple=evm --disassemble -s test.o
```
You will see something like:
```
test.o:	file format ELF64-unknown


Disassembly of section .text:

0000000000000000 abc.1:
       0: 5b                           	JUMPDEST
       1: 60 40                        	PUSH1 	64
       3: 80 51                        	DUP1
       5: 60 40                        	PUSH1 	64
       7: 01 52                        	ADD
       9: 60 40                        	PUSH1 	64
       b: 51 90                        	MLOAD
       d: 91 52                        	SWAP2
       f: 60 80                        	PUSH1 	128
      11: 60 40                        	PUSH1 	64
      13: 51 90                        	MLOAD
      15: 01 90                        	ADD
      17: 52 60                        	MSTORE
      19: 40 51                        	BLOCKHASH
      1b: 51 60                        	MLOAD
      1d: 80 60                        	DUP1
      1f: 40 51                        	BLOCKHASH
      21: 90 01                        	SWAP1
      23: 51 01                        	MLOAD
      25: 60 40                        	PUSH1 	64
      27: 80 51                        	DUP1
      29: 60 40                        	PUSH1 	64
      2b: 03 52                        	SUB
      2d: 90 56                        	SWAP1
```
