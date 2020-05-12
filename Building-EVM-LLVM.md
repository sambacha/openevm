The project compiles like other LLVM projects. The target's name is `EVM`, but since it is not yet finalized, you have to specify `-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM` when you compile it.

In short,  you can use the following to build the backend:
```
git clone git@github.com:etclabscore/evm_llvm.git
cd evm_llvm
git checkout EVM
mkdir build && cd build
cmake -DLLVM_TARGETS_TO_BUILD=EVM -DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM ..
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

Prerequisite: You have to install `clang` and use it to generate LLVM IR first:
```
clang -S -emit-llvm test.c
```

This will generate a `test.ll` file which should be the LLVM IR equivalent of our `test.c` file. Then we can generate EVM binary or assembly from it. In order to use the backend to generate EVM assembly, you have to specify `-mtriple=evm` when calling `llc`. An example is as follows:
```
./build/bin/llc -mtriple=evm test.ll -o test.s
```

The generated `test.s` file contains the compiled EVM assembly code. Note that the generated code is the function body itself. In order to generate a complete smart contract source code we need to use a smart contract creator function, which we will talk about it in another page.

Notice that you can also get the binary code of the function body by emitting an object file:
```
./build/bin/llc -mtriple=evm -filetype=obj test.ll -o test.o
```

