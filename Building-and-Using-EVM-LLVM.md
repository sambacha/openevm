The project compiles like other LLVM projects. The target's name is `EVM`, but since it is not yet finalized, you have to specify `-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM` when you compile it.

In short,  you can use the following to build the backend:
```
git clone git@github.com:etclabscore/evm_llvm.git
cd evm_llvm
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
In order to use the backend to generate EVM code, you have to specify `-mtriple=evm` when calling `llc`. An example is as follows:
```
./build/bin/llc -mtriple=evm -debug test.ll -o test.s
```

